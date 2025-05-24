package com.codewithmehdi.myofflinemap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.codewithmehdi.myofflinemap.databinding.ActivityResidentMainBinding
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.android.util.AndroidUtil
import org.mapsforge.map.layer.overlay.Marker
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.map.rendertheme.InternalRenderTheme
import java.io.FileInputStream

class ResidentActivity : AppCompatActivity() {

    companion object {
        val HCM = LatLong(10.762622, 106.660172)
        private const val LOCATION_REQUEST_CODE = 1001
    }

    private lateinit var b: ActivityResidentMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        AndroidGraphicFactory.createInstance(application)

        b = ActivityResidentMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // --- BEGIN: Retrieve token and userId for API use ---
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        val userId = prefs.getString("user_id", null)



        // Request location permission if not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdates(1)
            .build()

        val contract = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            result?.data?.data?.let { uri ->
                openMap(uri)
            }
        }
        contract.launch(
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
        )



        //Report button
        b.reportBtn.setOnClickListener {
            // Option 1: Pass token/userId via Intent (if you want)
            val intent = Intent(this, ReportActivity::class.java)
            intent.putExtra("token", token)
            intent.putExtra("user_id", userId)
            startActivity(intent)
            finish()
            // Option 2: Just use SharedPreferences in ReportActivity (recommended)
        }


    }

    private fun openMap(uri: Uri) {
        b.map.mapScaleBar.isVisible = true
        b.map.setBuiltInZoomControls(true)

        val cache = AndroidUtil.createTileCache(
            this,
            "mycache",
            b.map.model.displayModel.tileSize,
            1f,
            b.map.model.frameBufferModel.overdrawFactor
        )

        val stream = contentResolver.openInputStream(uri) as FileInputStream
        val mapStore = MapFile(stream)

        val renderLayer = TileRendererLayer(
            cache,
            mapStore,
            b.map.model.mapViewPosition,
            AndroidGraphicFactory.INSTANCE
        )
        renderLayer.setXmlRenderTheme(InternalRenderTheme.DEFAULT)
        b.map.layerManager.layers.add(renderLayer)

        getUserLocationOrFallback()
    }

    private fun getUserLocationOrFallback() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            // Requesting permission - wait for callback
            return
        }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location != null) {
                val userLatLong = LatLong(location.latitude, location.longitude)

                b.map.setCenter(userLatLong)
                b.map.setZoomLevel(18)

                val userMarker = createMarker(userLatLong)
                b.map.layerManager.layers.add(userMarker)
            } else {
                fallbackToHCM()
            }
        }.addOnFailureListener {
            fallbackToHCM()
        }
    }

    private fun fallbackToHCM() {
        b.map.setCenter(HCM)
        b.map.setZoomLevel(18)
    }

    private fun createMarker(position: LatLong): Marker {
        val drawable: Drawable? = ContextCompat.getDrawable(this, R.drawable.ic_user_location)
        val bitmap = AndroidGraphicFactory.convertToBitmap(drawable)
        return Marker(position, bitmap, 0, -bitmap.height / 2)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getUserLocationOrFallback()
        } else {
            fallbackToHCM()
        }
    }
}
