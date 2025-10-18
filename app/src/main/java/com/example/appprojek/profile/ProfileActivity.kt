package com.example.appprojek.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appprojek.auth.LoginActivity
import com.example.appprojek.chat.ChatSupportActivity
import com.example.appprojek.databinding.ActivityProfileBinding
import com.example.appprojek.order.OrderHistoryActivity
import com.example.appprojek.util.AuthManager
import com.example.appprojek.wishlist.WishlistActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val authManager = AuthManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        loadUserData()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        binding.layoutOrderHistory.setOnClickListener {
            startActivity(Intent(this, OrderHistoryActivity::class.java))
        }

        binding.layoutWishlist.setOnClickListener {
            startActivity(Intent(this, WishlistActivity::class.java))
        }

        binding.layoutSettings.setOnClickListener {
            // TODO: Implement settings
            Toast.makeText(this, "Fitur pengaturan akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        binding.layoutHelp.setOnClickListener {
            startActivity(Intent(this, ChatSupportActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            authManager.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadUserData() {
        val user = authManager.getCurrentUser()
        if (user != null) {
            binding.tvUserName.text = user.name
            binding.tvUserEmail.text = user.email
            binding.tvUserPhone.text = user.phone
            binding.tvUserAddress.text = user.address
        } else {
            // User not logged in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }
}
