package com.example.appprojek

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class TrackingActivity : AppCompatActivity(), OnMapReadyCallback {
    private var courierMarker: Marker? = null
    private lateinit var map: GoogleMap
    private val baseUrl = System.getenv("BACKEND_BASE_URL") ?: "http://10.0.2.2:8000"
    private val pollHandler by lazy { android.os.Handler(mainLooper) }
    private var pollRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)
        val mapFragment =
                supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val store = LatLng(-6.200000, 106.816666) // Jakarta
        var home = LatLng(-6.230000, 106.820000)

        courierMarker =
                map.addMarker(
                        MarkerOptions()
                                .position(store)
                                .title("Kurir")
                                .icon(
                                        BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_AZURE
                                        )
                                )
                )

        if (hasLocationPermission()) {
            val fused = LocationServices.getFusedLocationProviderClient(this)
            fused.lastLocation
                    .addOnSuccessListener { loc ->
                        if (loc != null) {
                            home = LatLng(loc.latitude, loc.longitude)
                        }
                        map.addMarker(MarkerOptions().position(home).title("Rumah Anda"))
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 12f))
                        requestEtaFromBackend(store, home)
                        startPollingCourier("c1", home)
                    }
                    .addOnFailureListener {
                        map.addMarker(MarkerOptions().position(home).title("Rumah Anda"))
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 12f))
                        requestEtaFromBackend(store, home)
                        startPollingCourier("c1", home)
                    }
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    100
            )
            map.addMarker(MarkerOptions().position(home).title("Rumah Anda"))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(home, 12f))
            requestEtaFromBackend(store, home)
            startPollingCourier("c1", home)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        pollRunnable?.let { pollHandler.removeCallbacks(it) }
        pollRunnable = null
    }

    private fun startPollingCourier(courierId: String, destination: LatLng) {
        // Poll lokasi kurir tiap 2 detik dari backend: GET /courier/{id}/location
        pollRunnable?.let { pollHandler.removeCallbacks(it) }
        val runnable =
                object : Runnable {
                    override fun run() {
                        Thread {
                                    try {
                                        val url = "$baseUrl/courier/$courierId/location"
                                        val client = OkHttpClient()
                                        val req = Request.Builder().url(url).get().build()
                                        val res = client.newCall(req).execute()
                                        val bodyStr = res.body?.string() ?: return@Thread
                                        val json = JSONObject(bodyStr)
                                        val lat = json.getDouble("lat")
                                        val lon = json.getDouble("lon")
                                        val courier = LatLng(lat, lon)
                                        runOnUiThread {
                                            if (courierMarker == null) {
                                                courierMarker =
                                                        map.addMarker(
                                                                MarkerOptions()
                                                                        .position(courier)
                                                                        .title("Kurir")
                                                                        .icon(
                                                                                BitmapDescriptorFactory
                                                                                        .defaultMarker(
                                                                                                BitmapDescriptorFactory
                                                                                                        .HUE_AZURE
                                                                                        )
                                                                        )
                                                        )
                                            } else {
                                                courierMarker?.position = courier
                                            }
                                            // Optional: update ETA setiap polling
                                            requestEtaFromBackend(courier, destination)
                                        }
                                    } catch (_: Exception) {}
                                }
                                .start()
                        pollHandler.postDelayed(this, 2000L)
                    }
                }
        pollRunnable = runnable
        pollHandler.post(runnable)
    }

    private fun requestEtaFromBackend(origin: LatLng, destination: LatLng) {
        // POST /route { from:{lat,lon}, to:{lat,lon} }
        Thread {
                    try {
                        val url = "$baseUrl/route"
                        val bodyJson = JSONObject()
                        val from = JSONObject()
                        from.put("lat", origin.latitude)
                        from.put("lon", origin.longitude)
                        val to = JSONObject()
                        to.put("lat", destination.latitude)
                        to.put("lon", destination.longitude)
                        bodyJson.put("from", from)
                        bodyJson.put("to", to)

                        val client = OkHttpClient()
                        val req =
                                Request.Builder()
                                        .url(url)
                                        .post(
                                                bodyJson.toString()
                                                        .toRequestBody(
                                                                "application/json".toMediaType()
                                                        )
                                        )
                                        .build()
                        val res = client.newCall(req).execute()
                        val bodyStr = res.body?.string() ?: return@Thread
                        val json = JSONObject(bodyStr)
                        if (res.isSuccessful) {
                            val durationMin = json.optDouble("duration_min", Double.NaN)
                            if (!durationMin.isNaN()) {
                                runOnUiThread {
                                    Toast.makeText(
                                                    this,
                                                    "ETA: ${String.format("%.1f", durationMin)} menit",
                                                    Toast.LENGTH_SHORT
                                            )
                                            .show()
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
                .start()
    }

    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(latLng)
        }

        return poly
    }
}
