package com.codewithmehdi.myofflinemap

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.codewithmehdi.myofflinemap.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        // ✅ Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

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
                else -> {
                    // ✅ Sign in with Firebase
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Show success message
                                showSnackbar("Login successful!")

                                // Navigate to MainActivity
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                // Show error message
                                showSnackbar("Login failed: ${task.exception?.message}")
                            }
                        }
                }
            }
        }

        // Redirect to RegisterActivity
        b.registerRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(b.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
