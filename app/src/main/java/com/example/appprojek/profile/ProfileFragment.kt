package com.example.appprojek.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.appprojek.auth.LoginActivity
import com.example.appprojek.chat.ChatSupportActivity
import com.example.appprojek.databinding.ActivityProfileBinding
import com.example.appprojek.order.OrderHistoryActivity
import com.example.appprojek.util.AuthManager
import com.example.appprojek.wishlist.WishlistActivity
import com.example.appprojek.profile.EditProfileActivity

class ProfileFragment : Fragment() {
    private var _binding: ActivityProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var authManager: AuthManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityProfileBinding.inflate(inflater, container, false)
        authManager = AuthManager(requireContext())
        setupUI()
        loadUserData()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Reload latest session data in case EditProfileActivity updated it
        loadUserData()
    }

    private fun setupUI() {
        binding.toolbar.visibility = View.GONE // Hide toolbar in fragment
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
        binding.layoutOrderHistory.setOnClickListener {
            startActivity(Intent(requireContext(), OrderHistoryActivity::class.java))
        }
        binding.layoutWishlist.setOnClickListener {
            startActivity(Intent(requireContext(), WishlistActivity::class.java))
        }
        binding.layoutSettings.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur pengaturan akan segera hadir", Toast.LENGTH_SHORT).show()
        }
        binding.layoutHelp.setOnClickListener {
            startActivity(Intent(requireContext(), ChatSupportActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            authManager.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
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
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
