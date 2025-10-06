package com.example.appprojek

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.TimeUnit
import okhttp3.*
import org.json.JSONObject
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class ShippingTrackingActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController
    private lateinit var progressBar: View
    private lateinit var connectionStatus: View
    private lateinit var routeInfoOverlay: View
    private lateinit var driverInfoCard: View

    // WebSocket
    private lateinit var webSocket: WebSocket
    private lateinit var client: OkHttpClient

    // Map markers and polylines
    private var driverMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var routePolyline: Polyline? = null

    // Location data
    private var driverLocation: GeoPoint? = null
    private var destinationLocation: GeoPoint? = null
    private var routePoints: MutableList<GeoPoint> = mutableListOf()

    companion object {
        private const val TAG = "ShippingTracking"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        // Ganti dengan IP address komputer Anda yang menjalankan server
        private const val WEBSOCKET_URL = "ws:// 172.21.200.72/tracking"
        // Fallback untuk testing tanpa server
        private const val USE_MOCK_DATA = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shipping)

        // Setup toolbar with back button navigation to home
        setupToolbar()

        // Initialize OSMDroid
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        initializeViews()
        setupMap()
        checkPermissions()

        if (USE_MOCK_DATA) {
            setupMockData()
        } else {
            setupWebSocket()
        }

        setupClickListeners()
    }

    private fun initializeViews() {
        mapView = findViewById(R.id.mapView)
        progressBar = findViewById(R.id.progressBar)
        connectionStatus = findViewById(R.id.connectionStatus)
        routeInfoOverlay = findViewById(R.id.routeInfoOverlay)
        driverInfoCard = findViewById(R.id.driverInfoCard)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar.setNavigationOnClickListener {
            // Navigate back to MainActivity with HomeFragment
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupMap() {
        // Configure map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapController = mapView.controller

        // Set initial location (Jakarta)
        val defaultLocation = GeoPoint(-6.2088, 106.8456)
        mapController.setZoom(15.0)
        mapController.setCenter(defaultLocation)

        // Hide progress bar when map is ready
        mapView.addMapListener(
                object : org.osmdroid.events.MapListener {
                    override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                        return false
                    }

                    override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
                        return false
                    }
                }
        )

        // Simulate map loaded
        mapView.postDelayed({ progressBar.visibility = View.GONE }, 2000)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun setupWebSocket() {
        client = OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build()

        val request = Request.Builder().url(WEBSOCKET_URL).build()

        webSocket =
                client.newWebSocket(
                        request,
                        object : WebSocketListener() {
                            override fun onOpen(webSocket: WebSocket, response: Response) {
                                runOnUiThread {
                                    connectionStatus.findViewById<android.widget.TextView>(
                                                    R.id.connectionStatus
                                            )
                                            .text = "Terhubung"
                                    connectionStatus.setBackgroundColor(
                                            ContextCompat.getColor(
                                                    this@ShippingTrackingActivity,
                                                    R.color.success_color
                                            )
                                    )
                                    Log.d(TAG, "WebSocket connected")
                                }
                            }

                            override fun onMessage(webSocket: WebSocket, text: String) {
                                runOnUiThread { handleWebSocketMessage(text) }
                            }

                            override fun onFailure(
                                    webSocket: WebSocket,
                                    t: Throwable,
                                    response: Response?
                            ) {
                                runOnUiThread {
                                    connectionStatus.findViewById<android.widget.TextView>(
                                                    R.id.connectionStatus
                                            )
                                            .text = "Terputus"
                                    connectionStatus.setBackgroundColor(
                                            ContextCompat.getColor(
                                                    this@ShippingTrackingActivity,
                                                    R.color.error_color
                                            )
                                    )
                                    Log.e(TAG, "WebSocket failed", t)

                                    // Retry connection after 5 seconds
                                    webSocket.close(1000, "Retrying...")
                                    mapView.postDelayed({ setupWebSocket() }, 5000)
                                }
                            }

                            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                                Log.d(TAG, "WebSocket closed: $code - $reason")
                            }
                        }
                )
    }

    private fun handleWebSocketMessage(rawMessage: String) {
        try {
            val json = JSONObject(rawMessage)
            val type = json.getString("type")

            when (type) {
                "location_update" -> {
                    val lat = json.getDouble("latitude")
                    val lng = json.getDouble("longitude")
                    val driverName = json.getString("driver_name")
                    val eta = json.getString("eta")
                    val vehicleInfo = json.getString("vehicle_info")

                    updateDriverLocation(lat, lng)
                    updateDriverInfo(driverName, eta, vehicleInfo)
                }
                "route_update" -> {
                    val routeArray = json.getJSONArray("route")
                    val route = mutableListOf<GeoPoint>()

                    for (i in 0 until routeArray.length()) {
                        val point = routeArray.getJSONObject(i)
                        val lat = point.getDouble("lat")
                        val lng = point.getDouble("lng")
                        route.add(GeoPoint(lat, lng))
                    }

                    updateRoute(route)
                }
                "status_update" -> {
                    val status = json.getString("status")
                    val statusMessage = json.getString("message")
                    updateStatus(status, statusMessage)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing WebSocket message", e)
        }
    }

    private fun updateDriverLocation(lat: Double, lng: Double) {
        val newLocation = GeoPoint(lat, lng)
        driverLocation = newLocation

        // Update or create driver marker
        if (driverMarker == null) {
            driverMarker = Marker(mapView)
            driverMarker?.icon = ContextCompat.getDrawable(this, R.drawable.ic_truck_moving)
            driverMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(driverMarker)
        }

        driverMarker?.position = newLocation
        driverMarker?.title = "Driver Location"

        // Center map on driver location
        mapController.animateTo(newLocation)

        // Update route info
        updateRouteInfo()

        mapView.invalidate()
    }

    private fun updateRoute(route: List<GeoPoint>) {
        routePoints.clear()
        routePoints.addAll(route)

        // Remove existing route
        routePolyline?.let { mapView.overlays.remove(it) }

        // Create new route polyline
        routePolyline =
                Polyline().apply {
                    setPoints(route)
                    outlinePaint.color = Color.GREEN
                    outlinePaint.strokeWidth = 8f
                }

        mapView.overlays.add(routePolyline)

        // Add destination marker
        if (route.isNotEmpty()) {
            destinationLocation = route.last()

            if (destinationMarker == null) {
                destinationMarker = Marker(mapView)
                destinationMarker?.icon =
                        ContextCompat.getDrawable(this, android.R.drawable.ic_menu_mylocation)
                destinationMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                mapView.overlays.add(destinationMarker)
            }

            destinationMarker?.position = destinationLocation
            destinationMarker?.title = "Destination"
        }

        mapView.invalidate()
    }

    private fun updateDriverInfo(driverName: String, eta: String, vehicleInfo: String) {
        findViewById<android.widget.TextView>(R.id.driverName).text = driverName
        findViewById<android.widget.TextView>(R.id.etaText).text = eta
        findViewById<android.widget.TextView>(R.id.vehicleInfo).text = vehicleInfo
    }

    private fun updateStatus(status: String, statusMessage: String) {
        findViewById<android.widget.TextView>(R.id.statusText).text = statusMessage

        // Update status icon based on status
        val statusIcon = findViewById<android.widget.ImageView>(R.id.statusIcon)
        when (status) {
            "picked_up" -> {
                statusIcon.setImageResource(R.drawable.ic_truck_moving)
                statusIcon.setColorFilter(ContextCompat.getColor(this, R.color.success_color))
            }
            "in_transit" -> {
                statusIcon.setImageResource(R.drawable.ic_truck_moving)
                statusIcon.setColorFilter(ContextCompat.getColor(this, R.color.primary_color))
            }
            "delivered" -> {
                statusIcon.setImageResource(R.drawable.ic_truck_moving)
                statusIcon.setColorFilter(ContextCompat.getColor(this, R.color.success_color))
            }
        }
    }

    private fun updateRouteInfo() {
        driverLocation?.let { driver ->
            destinationLocation?.let { dest ->
                val distance = driver.distanceToAsDouble(dest)
                val distanceKm = String.format("%.1f", distance / 1000)

                findViewById<android.widget.TextView>(R.id.routeDistance).text =
                        "Jarak: $distanceKm km"

                // Estimate time (assuming average speed of 30 km/h)
                val estimatedTime = (distance / 1000 / 30 * 60).toInt()
                findViewById<android.widget.TextView>(R.id.routeTime).text =
                        "Estimasi: $estimatedTime menit"
            }
        }
    }

    private fun setupClickListeners() {
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCallDriver)
                .setOnClickListener {
                    // Implement call functionality
                    Toast.makeText(this, "Memanggil driver...", Toast.LENGTH_SHORT).show()
                }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnChatDriver)
                .setOnClickListener {
                    // Implement chat functionality
                    Toast.makeText(this, "Membuka chat...", Toast.LENGTH_SHORT).show()
                }
    }

    private fun setupMockData() {
        // Set connection status to connected
        connectionStatus.findViewById<android.widget.TextView>(R.id.connectionStatus).text =
                "Mock Data"
        connectionStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.success_color))

        // Initialize mock route
        val mockRoute =
                listOf(
                        GeoPoint(-6.2088, 106.8456),
                        GeoPoint(-6.2095, 106.8458),
                        GeoPoint(-6.2102, 106.8460),
                        GeoPoint(-6.2109, 106.8462),
                        GeoPoint(-6.2116, 106.8464),
                        GeoPoint(-6.2123, 106.8466),
                        GeoPoint(-6.2130, 106.8468),
                        GeoPoint(-6.2137, 106.8470),
                        GeoPoint(-6.2144, 106.8472),
                        GeoPoint(-6.2146, 106.8451)
                )

        updateRoute(mockRoute)

        // Start mock location updates
        startMockLocationUpdates()

        // Set initial driver info
        updateDriverInfo("Ahmad Supriadi", "5 menit", "B 1234 ABC\nMerah - Box Truck")
        updateStatus("in_transit", "Driver sedang dalam perjalanan")
    }

    private fun startMockLocationUpdates() {
        val handler = Handler(Looper.getMainLooper())
        val mockRoute =
                listOf(
                        GeoPoint(-6.2088, 106.8456),
                        GeoPoint(-6.2095, 106.8458),
                        GeoPoint(-6.2102, 106.8460),
                        GeoPoint(-6.2109, 106.8462),
                        GeoPoint(-6.2116, 106.8464),
                        GeoPoint(-6.2123, 106.8466),
                        GeoPoint(-6.2130, 106.8468),
                        GeoPoint(-6.2137, 106.8470),
                        GeoPoint(-6.2144, 106.8472),
                        GeoPoint(-6.2146, 106.8451)
                )

        var currentIndex = 0

        val runnable =
                object : Runnable {
                    override fun run() {
                        if (currentIndex < mockRoute.size) {
                            val location = mockRoute[currentIndex]
                            updateDriverLocation(location.latitude, location.longitude)

                            // Update ETA
                            val remainingPoints = mockRoute.size - currentIndex - 1
                            val eta =
                                    if (remainingPoints > 0) "${remainingPoints * 2} menit"
                                    else "Tiba"
                            updateDriverInfo("Ahmad Supriadi", eta, "B 1234 ABC\nMerah - Box Truck")

                            // Update status
                            when {
                                currentIndex == 0 ->
                                        updateStatus("picked_up", "Driver telah mengambil paket")
                                currentIndex < mockRoute.size - 1 ->
                                        updateStatus("in_transit", "Driver sedang dalam perjalanan")
                                else -> updateStatus("delivered", "Paket telah sampai tujuan")
                            }

                            currentIndex++
                        } else {
                            // Reset for demo
                            currentIndex = 0
                        }

                        handler.postDelayed(this, 3000) // Update every 3 seconds
                    }
                }

        handler.post(runnable)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted")
            } else {
                Snackbar.make(
                                mapView,
                                "Location permission required for tracking",
                                Snackbar.LENGTH_LONG
                        )
                        .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!USE_MOCK_DATA) {
            webSocket.close(1000, "Activity destroyed")
            client.dispatcher.executorService.shutdown()
        }
    }
}
