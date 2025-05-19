package com.codewithmehdi.myofflinemap

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.codewithmehdi.myofflinemap.databinding.ActivityRegisterBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var b: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize view binding
        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Handle register button click
        b.registerButton.setOnClickListener {
            val email = b.registerEmail.text.toString().trim()
            val password = b.registerPassword.text.toString().trim()
            val username = b.registerUsername.text.toString().trim()

            when {
                email.isEmpty() || password.isEmpty() || username.isEmpty() -> {
                    showSnackbar("Please fill all fields")
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    showSnackbar("Please enter a valid email address")
                }
                password.length < 6 -> {
                    showSnackbar("Password must be at least 6 characters")
                }
                username.length < 3 -> {
                    showSnackbar("Username must be at least 3 characters")
                }
                else -> {
                    // Show loading state
                    b.registerButton.isEnabled = false
                    b.registerButton.text = "Registering..."

                    // Create user with Firebase
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Set username in Firebase user profile
                                val user = auth.currentUser
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build()
                                user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                                    b.registerButton.isEnabled = true
                                    b.registerButton.text = "Create account"

                                    if (profileTask.isSuccessful) {
                                        showSnackbar("Registration successful!")
                                        navigateToMainActivity()
                                    } else {
                                        showSnackbar("Failed to set username: ${profileTask.exception?.message}")
                                    }
                                }
                            } else {
                                b.registerButton.isEnabled = true
                                b.registerButton.text = "Create account"
                                showSnackbar("Registration failed: ${task.exception?.message ?: "Unknown error"}")
                            }
                        }
                }
            }
        }

        // Redirect to LoginActivity
        b.txtLogIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // Finish RegisterActivity to enforce flow
        }

        // Handle social login buttons
        b.btnGoogleSignIn.setOnClickListener {
            showSnackbar("Google sign-in not implemented yet")
        }

        b.btnAppleSignIn.setOnClickListener {
            showSnackbar("Apple sign-in not implemented yet")
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