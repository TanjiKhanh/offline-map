package com.codewithmehdi.myofflinemap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codewithmehdi.myofflinemap.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Example login logic
        b.loginButton.setOnClickListener {
            val email = b.loginEmail.text.toString()
            val password = b.loginPassword.text.toString()

            if (email == "napu@gmail.com" && password == "123456") {
                // Show success message
                Snackbar.make(b.root, "Login successful!", Snackbar.LENGTH_SHORT).show()

                // Proceed to main activity
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                // Show error message
                Snackbar.make(b.root, "Wrong email or password", Snackbar.LENGTH_SHORT).show()
            }
        }

        // Navigate to register screen
        b.registerRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
