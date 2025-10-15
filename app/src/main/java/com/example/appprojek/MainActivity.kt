package com.example.appprojek

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appprojek.databinding.ActivityMainBinding
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.appprojek.util.AuthManager


class MainActivity : AppCompatActivity() {
    private val authManager by lazy { AuthManager(this) }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    // enableEdgeToEdge() // Hapus jika tidak tersedia
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
        // init first fragment
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commit()
        }
        binding.topAppBar.setOnMenuItemClickListener { item ->
            false
        }
        binding.bottomNav.setOnItemSelectedListener { item ->
            try {
                when (item.itemId) {
                    R.id.nav_home -> {
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, HomeFragment())
                            .commit()
                        true
                    }
                    R.id.nav_cart -> {
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, CartFragment())
                            .commit()
                        true
                    }
                    R.id.nav_notifications -> {
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, com.example.appprojek.notification.NotificationFragment())
                            .commit()
                        true
                    }
                    R.id.nav_profile -> {
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, com.example.appprojek.profile.ProfileFragment())
                            .commit()
                        true
                    }
                    else -> false
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                false
            }
        }
        // ...existing code...
    }
}
