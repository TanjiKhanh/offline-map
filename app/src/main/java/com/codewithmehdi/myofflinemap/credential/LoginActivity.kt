package com.codewithmehdi.myofflinemap.credential

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.codewithmehdi.myofflinemap.databinding.ActivityLoginBinding
import com.codewithmehdi.myofflinemap.PoliceActivity
import com.codewithmehdi.myofflinemap.ResidentActivity
import com.google.android.material.snackbar.Snackbar
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private lateinit var b: ActivityLoginBinding
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.loginButton.setOnClickListener {
            val email = b.loginEmail.text.toString().trim()
            val password = b.loginPassword.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.isEmpty()) {
                showSnackbar("Invalid credentials")
                return@setOnClickListener
            }

            val formBody = FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build()

            val request = Request.Builder()
                .url("http://10.0.2.2/website/login.php")
                .post(formBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        showSnackbar("Login failed: ${e.message}")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    val json = JSONObject(body ?: "{}")
                    val success = json.optBoolean("success")
                    val role = json.optString("role_id")
                    val token = json.optString("token")
                    val userId = json.optString("user_id")

                    runOnUiThread {
                        if (success && token.isNotEmpty()) {
                            // Save token and user info to SharedPreferences
                            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                            prefs.edit()
                                .putString("token", token)
                                .putString("role", role)
                                .putString("user_id", userId)
                                .apply()

                            val intent = if (role == "2") {
                                Intent(this@LoginActivity, PoliceActivity::class.java)
                            } else {
                                Intent(this@LoginActivity, ResidentActivity::class.java)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            showSnackbar(json.optString("message", "Login failed"))
                        }
                    }
                }
            })
        }

        b.registerRedirect.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(b.root, message, Snackbar.LENGTH_SHORT).show()
    }
}