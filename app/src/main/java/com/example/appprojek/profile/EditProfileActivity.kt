package com.example.appprojek.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.example.appprojek.R
import com.example.appprojek.data.UserRepository
import com.example.appprojek.util.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log

class EditProfileActivity : AppCompatActivity() {
        private val LOCATION_PERMISSION_REQUEST = 1001
        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private val cancellationTokenSource = CancellationTokenSource()
        override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_edit_profile)

                val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
                toolbar.setNavigationOnClickListener { finish() }

                                val auth = AuthManager(this)
                                val user = auth.getCurrentUser()
                                if (user == null) {
                                        Toast.makeText(this, "Sesi pengguna tidak ditemukan. Silakan login kembali.", Toast.LENGTH_LONG).show()
                                        finish()
                                        return
                                }

                val nameField = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputName)
                val phoneField = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputPhone)
                val addressField = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.inputAddress)
                val progress = findViewById<com.google.android.material.progressindicator.LinearProgressIndicator>(R.id.progressBar)
                val layoutPhone = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.layoutPhone)
                val layoutAddress = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.layoutAddress)
                val buttonUseLocation = findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonUseLocation)

                nameField.setText(user?.name ?: "")
                phoneField.setText(user?.phone ?: "")
                addressField.setText(user?.address ?: "")

                                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

                                buttonUseLocation.setOnClickListener {
                                                // Check permission
                                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
                                                                return@setOnClickListener
                                                }
                                                // Get last known location
                                                @Suppress("DEPRECATION")
                                                progress.visibility = android.view.View.VISIBLE
                                                fusedLocationClient.getCurrentLocation(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token)
                                                        .addOnSuccessListener { location ->
                                                                if (location != null) {
                                                                        val lat = location.latitude
                                                                        val lng = location.longitude
                                                                        val coordText = "${'$'}lat,${'$'}lng"

                                                                        // Reverse geocoding in background
                                                                        lifecycleScope.launch {
                                                                                val humanAddress = withContext(Dispatchers.IO) {
                                                                                        try {
                                                                                                val geocoder = android.location.Geocoder(this@EditProfileActivity)
                                                                                                @Suppress("DEPRECATION")
                                                                                                val list = geocoder.getFromLocation(lat, lng, 1)
                                                                                                if (!list.isNullOrEmpty()) {
                                                                                                        val addr = list[0]
                                                                                                        val parts = mutableListOf<String>()
                                                                                                        addr.thoroughfare?.let { parts.add(it) }
                                                                                                        addr.subLocality?.let { parts.add(it) }
                                                                                                        addr.locality?.let { parts.add(it) }
                                                                                                        addr.adminArea?.let { parts.add(it) }
                                                                                                        addr.postalCode?.let { parts.add(it) }
                                                                                                        addr.countryName?.let { parts.add(it) }
                                                                                                        parts.joinToString(", ")
                                                                                                } else null
                                                                                                } catch (e: Exception) {
                                                                                                        null
                                                                                                }
                                                                                        }

                                                                                // If we got a human-readable address, show it; otherwise show lat,lng
                                                                                val display = humanAddress ?: coordText
                                                                                addressField.setText(display)

                                                                                // Always save lat/lng to backend and send human-readable address if available
                                                                                                                                                                val repo = UserRepository()

                                                                                                                                                                val userIdInt = user.id.toIntOrNull()
                                                                                                                                                                if (userIdInt == null) {
                                                                                                                                                                        // session invalid
                                                                                                                                                                        withContext(Dispatchers.Main) {
                                                                                                                                                                                Toast.makeText(this@EditProfileActivity, "Sesi tidak valid. Silakan login ulang.", Toast.LENGTH_LONG).show()
                                                                                                                                                                        }
                                                                                                                                                                        progress.visibility = android.view.View.GONE
                                                                                                                                                                        return@launch
                                                                                                                                                                }

                                                                                                                                                                                                                                                Log.i("EditProfile", "calling updateProfile userId=$userIdInt email=${user.email} address=$display lat=$lat lng=$lng")

                                                                                                                                                                                                                                                val success = withContext(Dispatchers.IO) {
                                                                                                                                                                                                                                                        repo.updateProfile(
                                                                                                                                                                                                                                                                        userId = userIdInt,
                                                                                                                                                                                                                                                                        email = user.email,
                                                                                                                                                                                                                                                                        name = null,
                                                                                                                                                                                                                                                                        phone = null,
                                                                                                                                                                                                                                                                        address = display,
                                                                                                                                                                                                                                                                        latitude = lat,
                                                                                                                                                                                                                                                                        longitude = lng
                                                                                                                                                                                                                                                        )
                                                                                                                                                                                                                                                }

                                                                                                                                                                                                                                                Log.i("EditProfile", "updateProfile returned: $success for userId=$userIdInt")

                                                                                if (success) {
                                                                                        Toast.makeText(this@EditProfileActivity, "Lokasi tersimpan", Toast.LENGTH_SHORT).show()
                                                                                        // update local session
                                                                                        if (user.id.isNotEmpty()) {
                                                                                            AuthManager(this@EditProfileActivity).saveBackendUser(
                                                                                                userId = user.id.toInt(),
                                                                                                email = user.email,
                                                                                                name = user.name ?: "",
                                                                                                phone = user.phone ?: "",
                                                                                                address = display
                                                                                            )
                                                                                        }
                                                                                } else {
                                                                                        Toast.makeText(this@EditProfileActivity, "Gagal menyimpan lokasi", Toast.LENGTH_SHORT).show()
                                                                                }
                                                                                progress.visibility = android.view.View.GONE
                                                                        }
                                                                } else {
                                                                        progress.visibility = android.view.View.GONE
                                                                        Toast.makeText(this, "Tidak dapat mendapatkan lokasi saat ini", Toast.LENGTH_SHORT).show()
                                                                }
                                                        }
                                                        .addOnFailureListener { e ->
                                                                progress.visibility = android.view.View.GONE
                                                                Toast.makeText(this, "Gagal mengambil lokasi: ${'$'}{e.message}", Toast.LENGTH_SHORT).show()
                                                        }
                                }

                findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonSave)
                        .setOnClickListener {
                                val name = nameField.text?.toString()?.trim().orEmpty()
                                val phone = phoneField.text?.toString()?.trim().orEmpty()
                                val address = addressField.text?.toString()?.trim().orEmpty()

                                if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                                        Toast.makeText(this, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                                        return@setOnClickListener
                                }

                                // Validasi sederhana nomor HP (digit 8-15)
                                val phoneDigits = phone.filter { it.isDigit() }
                                if (phoneDigits.length < 8 || phoneDigits.length > 15) {
                                        layoutPhone.error = "Nomor HP tidak valid"
                                        return@setOnClickListener
                                } else layoutPhone.error = null

                                // Validasi alamat tidak terlalu pendek
                                if (address.length < 5) {
                                        layoutAddress.error = "Alamat terlalu pendek"
                                        return@setOnClickListener
                                } else layoutAddress.error = null

                                lifecycleScope.launch {
                                        progress.visibility = android.view.View.VISIBLE
                                        val repo = UserRepository()
                                        val success = withContext(Dispatchers.IO) {
                                                // If address is in 'lat,lng' format, send lat/lng fields too
                                                var latVal: Double? = null
                                                var lngVal: Double? = null
                                                val parts = address.split(',').map { it.trim() }
                                                if (parts.size == 2) {
                                                        try {
                                                                latVal = parts[0].toDouble()
                                                                lngVal = parts[1].toDouble()
                                                        } catch (e: Exception) {
                                                                // ignore parse errors
                                                        }
                                                }
                                                repo.updateProfile(
                                                        userId = user?.id?.toIntOrNull(),
                                                        email = user?.email,
                                                        name = name,
                                                        phone = phone,
                                                        address = address,
                                                        latitude = latVal,
                                                        longitude = lngVal
                                                )
                                        }
                                        if (success) {
                                                // Simpan ke sesi lokal
                                                if (user.id.isNotEmpty()) {
                                                        AuthManager(this@EditProfileActivity)
                                                                .saveBackendUser(
                                                                                userId = user.id.toInt(),
                                                                                email = user.email,
                                                                                name = name,
                                                                                phone = phone,
                                                                                address = address
                                                                )
                                                }
                                                Toast.makeText(this@EditProfileActivity, "Profil diperbarui", Toast.LENGTH_SHORT).show()
                                                setResult(RESULT_OK)
                                                finish()
                                        } else {
                                                Toast.makeText(this@EditProfileActivity, "Gagal memperbarui profil", Toast.LENGTH_SHORT).show()
                                        }
                                        progress.visibility = android.view.View.GONE
                                }
                        }
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                if (requestCode == LOCATION_PERMISSION_REQUEST) {
                        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                // permission granted - simulate click on button to fetch location
                                findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonUseLocation).performClick()
                        } else {
                                Toast.makeText(this, "Permission lokasi diperlukan", Toast.LENGTH_SHORT).show()
                        }
                }
        }

        override fun onDestroy() {
                super.onDestroy()
                cancellationTokenSource.cancel()
        }
}


