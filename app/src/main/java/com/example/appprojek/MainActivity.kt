package com.example.appprojek

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appprojek.auth.LoginActivity
import com.example.appprojek.notification.NotificationActivity
import com.example.appprojek.profile.ProfileActivity
import com.example.appprojek.search.SearchActivity
import com.example.appprojek.util.AuthManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private val authManager by lazy { AuthManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // padding bottom = 0
            insets
        }
        // init first fragment
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, HomeFragment())
                    .commit()
        }
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar.setOnMenuItemClickListener { item ->
            // Tidak ada menu search lagi
            false
        }
        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottom.setOnItemSelectedListener { item ->
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
    }
}
