package com.codewithmehdi.myofflinemap.credential

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import com.codewithmehdi.myofflinemap.ResidentActivity
import com.codewithmehdi.myofflinemap.databinding.ActivityRegisterBinding
import com.google.android.material.snackbar.Snackbar
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var b: ActivityRegisterBinding
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.registerButton.setOnClickListener {
            val username = b.registerUsername.text.toString().trim()
            val email = b.registerEmail.text.toString().trim()
            val password = b.registerPassword.text.toString().trim()

            if (username.length < 3 || !Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.length < 6) {
                showSnackbar("Please enter valid credentials")
                return@setOnClickListener
            }

            val formBody = FormBody.Builder()
                .add("username", username)
                .add("email", email)
                .add("password", password)
                .build()

            val request = Request.Builder()
                .url("http://10.0.2.2/website/register.php")
                .post(formBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        showSnackbar("Registration failed: ${e.message}")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body?.string()
                    val json = JSONObject(body ?: "{}")
                    val success = json.optBoolean("success")

                    runOnUiThread {
                        if (success) {
                            showSnackbar("Registration successful!")
                            startActivity(Intent(this@RegisterActivity, ResidentActivity::class.java))
                            finish()
                        } else {
                            showSnackbar(json.optString("message", "Registration failed"))
                        }
                    }
                }
            })
        }

        b.txtLogIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(b.root, message, Snackbar.LENGTH_LONG).show()
    }
}
