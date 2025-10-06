const WebSocket = require('ws');
const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors());

// Create HTTP server
const server = require('http').createServer(app);

// Create WebSocket server
const wss = new WebSocket.Server({ 
    server,
    path: '/tracking'
});

// Store connected clients
const clients = new Set();

// Mock driver data
let driverData = {
    id: 'DRV001',
    name: 'Ahmad Supriadi',
    phone: '+6281234567890',
    rating: 4.8,
    vehicle: {
        plate: 'B 1234 ABC',
        type: 'Box Truck',
        color: 'Merah'
    },
    currentLocation: {
        lat: -6.2088,
        lng: 106.8456
    },
    destination: {
        lat: -6.2146,
        lng: 106.8451
    },
    status: 'in_transit',
    eta: '5 menit'
};

// Mock route points (from current location to destination)
const routePoints = [
    { lat: -6.2088, lng: 106.8456 },
    { lat: -6.2095, lng: 106.8458 },
    { lat: -6.2102, lng: 106.8460 },
    { lat: -6.2109, lng: 106.8462 },
    { lat: -6.2116, lng: 106.8464 },
    { lat: -6.2123, lng: 106.8466 },
    { lat: -6.2130, lng: 106.8468 },
    { lat: -6.2137, lng: 106.8470 },
    { lat: -6.2144, lng: 106.8472 },
    { lat: -6.2146, lng: 106.8451 }
];

let currentRouteIndex = 0;

// WebSocket connection handling
wss.on('connection', function connection(ws, req) {
    console.log('Client connected from:', req.socket.remoteAddress);
    clients.add(ws);
    
    // Send initial data
    sendInitialData(ws);
    
    // Send route data
    sendRouteData(ws);
    
    ws.on('message', function incoming(message) {
        try {
            const data = JSON.parse(message);
            console.log('Received:', data);
            
            // Handle different message types
            switch(data.type) {
                case 'ping':
                    ws.send(JSON.stringify({ type: 'pong', timestamp: Date.now() }));
                    break;
                case 'request_update':
                    sendLocationUpdate(ws);
                    break;
                default:
                    console.log('Unknown message type:', data.type);
            }
        } catch (error) {
            console.error('Error parsing message:', error);
        }
    });
    
    ws.on('close', function() {
        console.log('Client disconnected');
        clients.delete(ws);
    });
    
    ws.on('error', function(error) {
        console.error('WebSocket error:', error);
        clients.delete(ws);
    });
});

// Send initial driver data
function sendInitialData(ws) {
    const initialData = {
        type: 'driver_info',
        data: {
            driver_name: driverData.name,
            driver_phone: driverData.phone,
            driver_rating: driverData.rating,
            vehicle_info: `${driverData.vehicle.plate}\n${driverData.vehicle.color} - ${driverData.vehicle.type}`,
            status: driverData.status,
            eta: driverData.eta
        }
    };
    
    ws.send(JSON.stringify(initialData));
}

// Send route data
function sendRouteData(ws) {
    const routeData = {
        type: 'route_update',
        route: routePoints
    };
    
    ws.send(JSON.stringify(routeData));
}

// Send location update
function sendLocationUpdate(ws) {
    const locationData = {
        type: 'location_update',
        latitude: driverData.currentLocation.lat,
        longitude: driverData.currentLocation.lng,
        driver_name: driverData.name,
        eta: driverData.eta,
        vehicle_info: `${driverData.vehicle.plate}\n${driverData.vehicle.color} - ${driverData.vehicle.type}`,
        status: driverData.status,
        timestamp: Date.now()
    };
    
    ws.send(JSON.stringify(locationData));
}

// Send status update
function sendStatusUpdate(ws, status, message) {
    const statusData = {
        type: 'status_update',
        status: status,
        message: message,
        timestamp: Date.now()
    };
    
    ws.send(JSON.stringify(statusData));
}

// Simulate driver movement along route
function simulateDriverMovement() {
    if (currentRouteIndex < routePoints.length - 1) {
        // Move to next point
        currentRouteIndex++;
        driverData.currentLocation = routePoints[currentRouteIndex];
        
        // Calculate remaining ETA
        const remainingPoints = routePoints.length - currentRouteIndex - 1;
        const estimatedTime = Math.max(1, remainingPoints * 2); // 2 minutes per point
        driverData.eta = `${estimatedTime} menit`;
        
        // Update status based on progress
        if (currentRouteIndex === 0) {
            driverData.status = 'picked_up';
        } else if (currentRouteIndex < routePoints.length - 1) {
            driverData.status = 'in_transit';
        } else {
            driverData.status = 'delivered';
            driverData.eta = 'Tiba';
        }
        
        // Send updates to all connected clients
        clients.forEach(client => {
            if (client.readyState === WebSocket.OPEN) {
                sendLocationUpdate(client);
                
                // Send status update if status changed
                if (currentRouteIndex === 0) {
                    sendStatusUpdate(client, 'picked_up', 'Driver telah mengambil paket');
                } else if (currentRouteIndex === routePoints.length - 1) {
                    sendStatusUpdate(client, 'delivered', 'Paket telah sampai tujuan');
                }
            }
        });
        
        console.log(`Driver moved to point ${currentRouteIndex + 1}/${routePoints.length}`);
    } else {
        console.log('Driver reached destination');
        // Reset for demo purposes
        setTimeout(() => {
            currentRouteIndex = 0;
            driverData.currentLocation = routePoints[0];
            driverData.status = 'in_transit';
            driverData.eta = '5 menit';
        }, 10000); // Reset after 10 seconds
    }
}

// Start simulation
setInterval(simulateDriverMovement, 5000); // Move every 5 seconds

// HTTP endpoints for testing
app.get('/', (req, res) => {
    res.json({
        message: 'Shipping Tracking WebSocket Server',
        status: 'running',
        connected_clients: clients.size,
        driver_data: driverData
    });
});

app.get('/api/driver', (req, res) => {
    res.json(driverData);
});

app.get('/api/route', (req, res) => {
    res.json({
        route: routePoints,
        current_index: currentRouteIndex
    });
});

// Start server
const PORT = process.env.PORT || 8080;
server.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 WebSocket server running on port ${PORT}`);
    console.log(`📡 WebSocket endpoint: ws://localhost:${PORT}/tracking`);
    console.log(`🌐 HTTP endpoint: http://localhost:${PORT}`);
    console.log(`📱 For Android app, use: ws://YOUR_IP:${PORT}/tracking`);
});

// Graceful shutdown
process.on('SIGINT', () => {
    console.log('\n🛑 Shutting down server...');
    server.close(() => {
        console.log('✅ Server closed');
        process.exit(0);
    });
});

