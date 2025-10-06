package com.example.appprojek

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Use Android 12+ splash API via theme; just route to MainActivity
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}


