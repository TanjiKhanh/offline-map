package com.codewithmehdi.myofflinemap

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.codewithmehdi.myofflinemap.databinding.ActivityReportBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class ReportActivity : AppCompatActivity() {
    private lateinit var b: ActivityReportBinding
    private var photoUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            photoUri = it
            b.fileName.text = it.toString()
            b.filesAttach.visibility = View.VISIBLE
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            try {
                val uri = saveBitmapToCache(it)
                photoUri = uri
                b.fileName.text = "camera_image.jpg"
                b.filesAttach.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityReportBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.returnBtn.setOnClickListener {
            startActivity(Intent(this, ResidentActivity::class.java))
            finish()
        }

        b.attachIcon.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        b.notAttachBtn.setOnClickListener {
            photoUri = null
            b.fileName.text = ""
            b.filesAttach.visibility = View.GONE
        }

        b.cameraIcon.setOnClickListener {
            takePictureLauncher.launch(null)
        }

        b.submitBtn.setOnClickListener {
            submitReport()
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri {
        val file = File(cacheDir, "camera_image.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        return FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.provider",
            file
        )
    }

    private fun submitReport() {
        val description = b.messageEdit.text.toString().trim()
        val location = b.locationEdit.text.toString().trim()

        if (description.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Please fill in description and location", Toast.LENGTH_SHORT).show()
            return
        }

        // Retrieve token from SharedPreferences
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Not authenticated. Please log in again.", Toast.LENGTH_LONG).show()
            // Optionally redirect to login
            startActivity(Intent(this, com.codewithmehdi.myofflinemap.credential.LoginActivity::class.java))
            finish()
            return
        }

        val client = OkHttpClient()
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("description", description)
            .addFormDataPart("location", location)

        photoUri?.let { uri ->
            // Use content resolver to get the stream if it's a content URI
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "upload_${System.currentTimeMillis()}.jpg"
            if (inputStream != null) {
                val tempFile = File.createTempFile("upload_", ".jpg", cacheDir)
                tempFile.outputStream().use { out -> inputStream.copyTo(out) }
                builder.addFormDataPart(
                    "photo", fileName,
                    RequestBody.create("image/*".toMediaTypeOrNull(), tempFile)
                )
            }
        }

        val request = Request.Builder()
            .url("http://10.0.2.2/website/report.php")
            .addHeader("Authorization", "Bearer $token") // Send token in header
            .post(builder.build())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ReportActivity, "Failed to submit: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    val responseBody = response.body?.string()
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val json = JSONObject(responseBody)
                            val success = json.optBoolean("success", false)
                            Toast.makeText(this@ReportActivity, json.optString("message", "Report submitted"), Toast.LENGTH_SHORT).show()
                            if (success) {
                                // Only navigate if submission was successful
                                val intent = Intent(this@ReportActivity, ResidentActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@ReportActivity, "Report failed" + e, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@ReportActivity, "Server error: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}