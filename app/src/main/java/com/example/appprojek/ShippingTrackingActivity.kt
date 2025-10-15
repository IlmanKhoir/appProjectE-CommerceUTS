package com.example.appprojek.shipping

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
import com.example.appprojek.MainActivity
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
       private const val WEBSOCKET_URL = "ws://192.168.1.19/tracking"
        // Fallback untuk testing tanpa server
    private const val USE_MOCK_DATA = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.appprojek.R.layout.activity_shipping)

        // Cek apakah activity ini dibuka dari halaman lain
        val isNewTracking = intent.getBooleanExtra("is_new_tracking", false)

        // Jika ini adalah pembukaan baru, lanjutkan seperti biasa
        // Jika tidak (dibuka dari ShippingActivity), kembalikan ke halaman sebelumnya
        if (!isNewTracking && !isTaskRoot) {
            finish()
            return
        }

        // Setup toolbar with back button navigation to home
        setupToolbar()

        // Initialize OSMDroid
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        initializeViews()
        setupMap()
        checkPermissions()

        setupMockData()

        setupClickListeners()
    }

    private fun initializeViews() {
        mapView = findViewById(com.example.appprojek.R.id.mapView)
        progressBar = findViewById(com.example.appprojek.R.id.progressBar)
        connectionStatus = findViewById(com.example.appprojek.R.id.connectionStatus)
        routeInfoOverlay = findViewById(com.example.appprojek.R.id.routeInfoOverlay)
        driverInfoCard = findViewById(com.example.appprojek.R.id.driverInfoCard)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(com.example.appprojek.R.id.topAppBar)
        toolbar.setNavigationOnClickListener {
            // Navigate back to MainActivity with HomeFragment
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupClickListeners() {
        // Setup click listeners for driver interaction buttons
        val btnCallDriver = findViewById<com.google.android.material.button.MaterialButton>(com.example.appprojek.R.id.btnCallDriver)
        val btnChatDriver = findViewById<com.google.android.material.button.MaterialButton>(com.example.appprojek.R.id.btnChatDriver)

        btnCallDriver.setOnClickListener {
            // Handle call driver action
            Toast.makeText(this, "Menghubungi driver...", Toast.LENGTH_SHORT).show()
        }

        btnChatDriver.setOnClickListener {
            // Handle chat with driver action
            Toast.makeText(this, "Membuka chat dengan driver...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDriverInfo(name: String, eta: String, vehicleInfo: String) {
        runOnUiThread {
            val driverName = findViewById<android.widget.TextView>(com.example.appprojek.R.id.driverName)
            val etaText = findViewById<android.widget.TextView>(com.example.appprojek.R.id.etaText)
            val vehicleInfoText = findViewById<android.widget.TextView>(com.example.appprojek.R.id.vehicleInfo)

            driverName.text = name
            etaText.text = eta
            vehicleInfoText.text = vehicleInfo
        }
    }

    private fun updateStatus(status: String, message: String) {
        runOnUiThread {
            val statusText = findViewById<android.widget.TextView>(com.example.appprojek.R.id.statusText)
            val statusIcon = findViewById<android.widget.ImageView>(com.example.appprojek.R.id.statusIcon)
            val deliveryStage = findViewById<android.widget.TextView>(com.example.appprojek.R.id.deliveryStage)
            val deliveryProgress = findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(com.example.appprojek.R.id.deliveryProgress)

            statusText.text = message

            // Update status icon and progress based on status
            when (status) {
                "picked_up" -> {
                    statusIcon.setImageResource(android.R.drawable.ic_menu_mylocation)
                    deliveryStage.text = "Paket Diambil"
                    deliveryProgress.progress = 25
                }
                "in_transit" -> {
                    statusIcon.setImageResource(android.R.drawable.ic_menu_directions)
                    deliveryStage.text = "Dalam Perjalanan"
                    deliveryProgress.progress = 50
                }
                "near_destination" -> {
                    statusIcon.setImageResource(android.R.drawable.ic_menu_mylocation)
                    deliveryStage.text = "Mendekati Tujuan"
                    deliveryProgress.progress = 75
                }
                "delivered" -> {
                    statusIcon.setImageResource(android.R.drawable.ic_menu_save)
                    deliveryStage.text = "Paket Telah Sampai"
                    deliveryProgress.progress = 100
                }
                else -> {
                    statusIcon.setImageResource(android.R.drawable.ic_menu_info_details)
                    deliveryStage.text = "Status Pengiriman"
                    deliveryProgress.progress = 0
                }
            }
        }
    }

    private fun updateRouteInfo(distance: String, time: String) {
        runOnUiThread {
            try {
                val routeDistance = findViewById<android.widget.TextView>(com.example.appprojek.R.id.routeDistance)
                val routeTime = findViewById<android.widget.TextView>(com.example.appprojek.R.id.routeTime)

                routeDistance.text = "Jarak: $distance"
                routeTime.text = "Estimasi: $time"

                // Tampilkan overlay jika sebelumnya tersembunyi
                routeInfoOverlay.visibility = View.VISIBLE
            } catch (e: Exception) {
                Log.e(TAG, "Error updating route info", e)
            }
        }
    }

    // Overload function tanpa parameter untuk backward compatibility
    private fun updateRouteInfo() {
        updateRouteInfo("Menghitung...", "Menghitung...")
    }

    private fun setupMap() {
        // Configure map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapController = mapView.controller

        // Set initial location (Jakarta)
        val defaultLocation = org.osmdroid.util.GeoPoint(-6.2088, 106.8456)
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
                                com.example.appprojek.R.id.connectionStatus
                            )
                                .text = "Terhubung"
                            connectionStatus.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@ShippingTrackingActivity,
                                    com.example.appprojek.R.color.success_color
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
                                com.example.appprojek.R.id.connectionStatus
                            )
                                .text = "Terputus"
                            connectionStatus.setBackgroundColor(
                                ContextCompat.getColor(
                                    this@ShippingTrackingActivity,
                                    com.example.appprojek.R.color.error_color
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
            // PERBAIKAN: Gunakan resource yang tersedia
            driverMarker?.icon = ContextCompat.getDrawable(this, com.example.appprojek.R.drawable.ic_car_animasi)
            driverMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(driverMarker)
        }

        driverMarker?.position = newLocation
        driverMarker?.title = "Driver Location"

        // Center map on driver location
        mapController.animateTo(newLocation)

        // PERBAIKAN: Update route info dengan parameter
        if (destinationLocation != null) {
            val (distance, time) = calculateDistanceAndTime(newLocation, destinationLocation!!)
            updateRouteInfo(distance, time)
        } else {
            updateRouteInfo("Menghitung...", "Menghitung...")
        }

        mapView.invalidate()
    }

    private fun updateRoute(route: List<GeoPoint>) {
        Thread {
            val snappedRoute = try {
                com.example.appprojek.util.OsrmUtil.matchRoute(route)
            } catch (e: Exception) {
                emptyList<GeoPoint>()
            }
            runOnUiThread {
                val finalRoute = snappedRoute.ifEmpty { route }
                routePoints.clear()
                routePoints.addAll(finalRoute)

                // Remove existing route
                routePolyline?.let { mapView.overlays.remove(it) }

                // Create new route polyline
                routePolyline = Polyline().apply {
                    setPoints(finalRoute)
                    outlinePaint.color = Color.parseColor("#4CAF50") // Green color
                    outlinePaint.strokeWidth = 12f
                }

                mapView.overlays.add(routePolyline)

                // Add destination marker
                if (finalRoute.isNotEmpty()) {
                    destinationLocation = finalRoute.last()

                    if (destinationMarker == null) {
                        destinationMarker = Marker(mapView)
                        destinationMarker?.icon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_mylocation)
                        destinationMarker?.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        mapView.overlays.add(destinationMarker)
                    }

                    destinationMarker?.position = destinationLocation
                    destinationMarker?.title = "Destination"

                    if (driverLocation != null) {
                        val (distance, time) = calculateDistanceAndTime(driverLocation!!, destinationLocation!!)
                        updateRouteInfo(distance, time)
                    }
                }

                mapView.invalidate()
            }
        }.start()
    }

    private fun calculateDistanceAndTime(start: GeoPoint, end: GeoPoint): Pair<String, String> {
        // Simple distance calculation (approximate)
        val distanceInKm = calculateDistanceBetweenPoints(start, end)
        val timeInMinutes = (distanceInKm * 2).toInt() // Assume 30 km/h average speed

        return "${String.format("%.1f", distanceInKm)} km" to "$timeInMinutes menit"
    }

    private fun calculateDistanceBetweenPoints(point1: GeoPoint, point2: GeoPoint): Double {
        val earthRadius = 6371.0 // kilometers

        val lat1 = Math.toRadians(point1.latitude)
        val lon1 = Math.toRadians(point1.longitude)
        val lat2 = Math.toRadians(point2.latitude)
        val lon2 = Math.toRadians(point2.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }

    private fun setupMockData() {
        // Set connection status to connected
        connectionStatus.findViewById<android.widget.TextView>(com.example.appprojek.R.id.connectionStatus).text =
            "Mock Data"
        connectionStatus.setBackgroundColor(ContextCompat.getColor(this, com.example.appprojek.R.color.success_color))

        // Mock route: Monas to Bundaran HI via Jalan MH Thamrin (Jakarta)
        val mockRoute = listOf(
            GeoPoint(-6.175392, 106.827153), // Monas
            GeoPoint(-6.181616, 106.828216), // Gambir
            GeoPoint(-6.186484, 106.829518), // Jalan Medan Merdeka Selatan
            GeoPoint(-6.190807, 106.830978), // Jalan MH Thamrin (dekat Sarinah)
            GeoPoint(-6.193125, 106.832512), // Thamrin City
            GeoPoint(-6.197584, 106.834091), // Plaza Indonesia
            GeoPoint(-6.200497, 106.836447), // Bundaran HI
            GeoPoint(-6.201905, 106.837563)  // Sisi selatan Bundaran HI
        )

        // Pastikan route diupdate sebelum memulai animasi
        updateRoute(mockRoute)

        // Start mock location updates
        startMockLocationUpdates()

        // Set initial driver info
        updateDriverInfo("Ahmad Supriadi", "5 menit", "B 1234 ABC\nMerah - Box Truck")
        updateStatus("in_transit", "Driver sedang dalam perjalanan")
    }

    private fun startMockLocationUpdates() {
        val handler = Handler(Looper.getMainLooper())
        val mockRoute = listOf(
            GeoPoint(-6.175392, 106.827153), // Monas
            GeoPoint(-6.181616, 106.828216), // Gambir
            GeoPoint(-6.186484, 106.829518), // Jalan Medan Merdeka Selatan
            GeoPoint(-6.190807, 106.830978), // Jalan MH Thamrin (dekat Sarinah)
            GeoPoint(-6.193125, 106.832512), // Thamrin City
            GeoPoint(-6.197584, 106.834091), // Plaza Indonesia
            GeoPoint(-6.200497, 106.836447), // Bundaran HI
            GeoPoint(-6.201905, 106.837563)  // Sisi selatan Bundaran HI
        )

        var currentIndex = 0

        val runnable =
            object : Runnable {
                override fun run() {
                    if (currentIndex < mockRoute.size) {
                        val location = mockRoute[currentIndex]
                        updateDriverLocation(location.latitude, location.longitude)

                        // PERBAIKAN: Hitung jarak dan waktu berdasarkan posisi saat ini
                        val currentLocation = mockRoute[currentIndex]
                        val destination = mockRoute.last()
                        val (distance, time) = calculateDistanceAndTime(currentLocation, destination)

                        val eta = if (currentIndex < mockRoute.size - 1) time else "Tiba"
                        updateDriverInfo("Ahmad Supriadi", eta, "B 1234 ABC\nMerah - Box Truck")
                        updateRouteInfo(distance, time)

                        // Update status
                        when {
                            currentIndex == 0 ->
                                updateStatus("picked_up", "Driver telah mengambil paket")
                            currentIndex < mockRoute.size - 1 ->
                                updateStatus("in_transit", "Driver sedang dalam perjalanan")
                            else -> updateStatus("delivered", "Paket telah sampai tujuan")
                        }

                        currentIndex++

                        if (currentIndex < mockRoute.size) {
                            handler.postDelayed(this, 3000) // Update every 3 seconds
                        } else {
                            // Pastikan jalur tetap terlihat setelah animasi selesai
                            updateRoute(mockRoute)

                            // Tampilkan pesan ketika sudah sampai
                            Toast.makeText(this@ShippingTrackingActivity,
                                "Pengiriman telah sampai tujuan",
                                Toast.LENGTH_LONG).show()
                        }
                    }
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
        // Tidak perlu menutup WebSocket jika menggunakan mock data
    }
}