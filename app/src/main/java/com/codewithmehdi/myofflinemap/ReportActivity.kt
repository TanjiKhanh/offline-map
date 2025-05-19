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
import com.google.firebase.auth.FirebaseAuth
import org.mapsforge.map.android.graphics.AndroidGraphicFactory

import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore


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
        AndroidGraphicFactory.createInstance(application)
        b = ActivityReportBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.returnBtn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
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
        val locationText = b.locationEdit.text.toString().trim()

        if (description.isEmpty() || locationText.isEmpty()) {
            Toast.makeText(this, "Please enter description and location", Toast.LENGTH_SHORT).show()
            return
        }

        if (photoUri != null) {
            uploadPhotoAndSave(description, locationText)
        } else {
            saveToFirestore(description, locationText, null)
        }
    }

    private fun uploadPhotoAndSave(description: String, location: String) {
        val storageRef = Firebase.storage.reference
        val imageRef = storageRef.child("reports/${UUID.randomUUID()}.jpg")

        imageRef.putFile(photoUri!!)
            .continueWithTask { task ->
                if (!task.isSuccessful) task.exception?.let { throw it }
                imageRef.downloadUrl
            }
            .addOnSuccessListener { downloadUri ->
                saveToFirestore(description, location, downloadUri.toString())
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToFirestore(description: String, location: String, photoUrl: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val report = hashMapOf(
            "userId" to currentUser.uid,
            "description" to description,
            "location" to location,
            "photoUrl" to photoUrl,
            "status" to "pending",
            "timestamp" to FieldValue.serverTimestamp()
        )

        Firebase.firestore.collection("reports")
            .add(report)
            .addOnSuccessListener {
                Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save report: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace() // Also prints to Logcat
            }
    }

}

