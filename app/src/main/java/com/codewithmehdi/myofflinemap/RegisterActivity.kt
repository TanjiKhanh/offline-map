package com.codewithmehdi.myofflinemap

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.codewithmehdi.myofflinemap.databinding.ActivityRegisterBinding
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {

    private lateinit var b: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.registerButton.setOnClickListener {
            val email = b.registerEmail.text.toString().trim()
            val password = b.registerPassword.text.toString().trim()
            val confirmPassword = b.confirmPassword.text.toString().trim()

            when {
                email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    showSnackbar("Please fill all fields")
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    showSnackbar("Please enter a valid email address")
                }
                password != confirmPassword -> {
                    showSnackbar("Passwords do not match")
                }
                else -> {
                    showSnackbar("Registered successfully!")

                    // Redirect to login activity after success
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }

        b.loginRedirect.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(b.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
