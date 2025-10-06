# Shipping Tracking dengan OpenStreetMap dan WebSocket

## Deskripsi
Fitur tracking pengiriman yang menampilkan peta dengan mobil box yang berjalan sesuai rute menggunakan OpenStreetMap dan WebSocket untuk real-time tracking.

## Fitur Utama
- **Peta Real-time**: Menggunakan OpenStreetMap untuk menampilkan peta
- **Tracking Mobil Box**: Menampilkan posisi mobil box yang bergerak sesuai rute
- **WebSocket Connection**: Koneksi real-time untuk update posisi driver
- **Informasi Driver**: Menampilkan detail driver, rating, dan informasi kendaraan
- **ETA (Estimated Time of Arrival)**: Estimasi waktu kedatangan
- **Action Buttons**: Tombol untuk telepon dan chat dengan driver

## Komponen yang Digunakan

### Layout (activity_shipping.xml)
- `MapView`: Peta OpenStreetMap
- `MaterialCardView`: Card informasi driver di bagian bawah
- `ProgressBar`: Indikator loading
- `TextView`: Status koneksi dan informasi rute

### Activity (ShippingTrackingActivity.kt)
- **WebSocket Client**: Menggunakan OkHttp WebSocket
- **Map Controller**: Mengontrol peta dan marker
- **Location Updates**: Update posisi driver secara real-time
- **Route Rendering**: Menampilkan rute dengan polyline

## Dependencies yang Ditambahkan
```kotlin
// OpenStreetMap
implementation("org.osmdroid:osmdroid-android:6.1.17")

// WebSocket
implementation("com.squareup.okhttp3:okhttp:4.12.0")
```

## Permissions yang Diperlukan
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

## Cara Penggunaan

### 1. Setup WebSocket Server
Pastikan WebSocket server Anda mengirim data dalam format JSON:

```json
{
  "type": "location_update",
  "latitude": -6.2088,
  "longitude": 106.8456,
  "driver_name": "Ahmad Supriadi",
  "eta": "5 menit",
  "vehicle_info": "B 1234 ABC\nMerah - Box Truck"
}
```

### 2. Update WebSocket URL
Ubah URL WebSocket di `ShippingTrackingActivity.kt`:
```kotlin
private const val WEBSOCKET_URL = "ws://your-server.com:8080/tracking"
```

### 3. Jalankan Activity
```kotlin
val intent = Intent(this, ShippingTrackingActivity::class.java)
startActivity(intent)
```

## Format Data WebSocket

### Location Update
```json
{
  "type": "location_update",
  "latitude": -6.2088,
  "longitude": 106.8456,
  "driver_name": "Ahmad Supriadi",
  "eta": "5 menit",
  "vehicle_info": "B 1234 ABC\nMerah - Box Truck"
}
```

### Route Update
```json
{
  "type": "route_update",
  "route": [
    {"lat": -6.2088, "lng": 106.8456},
    {"lat": -6.2090, "lng": 106.8460},
    {"lat": -6.2095, "lng": 106.8465}
  ]
}
```

### Status Update
```json
{
  "type": "status_update",
  "status": "in_transit",
  "message": "Driver sedang dalam perjalanan"
}
```

## Customization

### Mengubah Warna Rute
```kotlin
routePolyline = Polyline().apply {
    setPoints(route)
    color = Color.GREEN  // Ubah warna rute
    width = 8f          // Ubah ketebalan garis
}
```

### Mengubah Marker Driver
```kotlin
driverMarker?.icon = ContextCompat.getDrawable(this, R.drawable.your_custom_icon)
```

### Mengubah Zoom Level
```kotlin
mapController.setZoom(15.0)  // Ubah level zoom
```

## Troubleshooting

### 1. Peta Tidak Muncul
- Pastikan internet connection aktif
- Check permission INTERNET dan ACCESS_NETWORK_STATE

### 2. WebSocket Tidak Terhubung
- Pastikan URL WebSocket benar
- Check server WebSocket berjalan
- Pastikan network security config mengizinkan cleartext traffic

### 3. Location Permission
- Pastikan user memberikan permission ACCESS_FINE_LOCATION
- Handle permission request dengan proper

## Testing
Untuk testing tanpa WebSocket server, Anda bisa menggunakan mock data:

```kotlin
// Simulate location updates
private fun simulateLocationUpdates() {
    val handler = Handler(Looper.getMainLooper())
    val runnable = object : Runnable {
        override fun run() {
            // Mock location data
            val mockLat = -6.2088 + (Math.random() - 0.5) * 0.01
            val mockLng = 106.8456 + (Math.random() - 0.5) * 0.01
            
            updateDriverLocation(mockLat, mockLng)
            handler.postDelayed(this, 3000) // Update setiap 3 detik
        }
    }
    handler.post(runnable)
}
```

## Catatan Penting
- Pastikan WebSocket server mendukung CORS jika diperlukan
- Gunakan HTTPS/WSS untuk production
- Implement proper error handling untuk network issues
- Consider battery optimization untuk real-time updates
