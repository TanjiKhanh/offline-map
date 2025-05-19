package com.codewithmehdi.myofflinemap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codewithmehdi.myofflinemap.databinding.ActivityWelcomeBinding
import com.google.android.material.snackbar.Snackbar

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up click listeners
        binding.loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Removes WelcomeActivity from back stack
            // Remove finish() if you want to allow back navigation to WelcomeActivity
        }

        binding.googleLoginButton.setOnClickListener {
            showSnackbar("Google sign-in not implemented yet")
        }

        binding.appleLoginButton.setOnClickListener {
            showSnackbar("Apple sign-in not implemented yet")
        }

        binding.signUpText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish() // Removes WelcomeActivity from back stack
            // Remove finish() if you want to allow back navigation to WelcomeActivity
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        // Exit the app since this is the entry point
        finish()
    }
}