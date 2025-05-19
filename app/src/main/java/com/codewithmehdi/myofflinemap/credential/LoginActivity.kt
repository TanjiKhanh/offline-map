package com.codewithmehdi.myofflinemap.credential

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.codewithmehdi.myofflinemap.MainActivity
import com.codewithmehdi.myofflinemap.credential.WelcomeActivity
import com.codewithmehdi.myofflinemap.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Handle login button click
        b.loginButton.setOnClickListener {
            val email = b.loginEmail.text.toString().trim()
            val password = b.loginPassword.text.toString().trim()

            when {
                email.isEmpty() || password.isEmpty() -> {
                    showSnackbar("Please fill all fields")
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    showSnackbar("Please enter a valid email address")
                }
                password.length < 6 -> {
                    showSnackbar("Password must be at least 6 characters")
                }
                else -> {
                    // Show loading state
                    b.loginButton.isEnabled = false
                    b.loginButton.text = "Logging in..."

                    // Sign in with Firebase
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            b.loginButton.isEnabled = true
                            b.loginButton.text = "Login"

                            if (task.isSuccessful) {
                                // Optional: Add additional condition (e.g., profile check)
                                // For example, check if user has a username in Firebase (if stored)
                                showSnackbar("Login successful!")
                                navigateToMainActivity()
                            } else {
                                showSnackbar("Login failed: ${task.exception?.message ?: "Unknown error"}")
                            }
                        }
                }
            }
        }

        // Redirect to RegisterActivity
        b.registerRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish() // Finish LoginActivity to enforce flow
        }

        // Google sign-in
        b.googleLoginButton.setOnClickListener {
            showSnackbar("Google sign-in not implemented yet")
        }

        // Forgot password
        b.forgotPassword.setOnClickListener {
            showSnackbar("Forgot password feature not implemented yet")
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(b.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
}