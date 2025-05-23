package com.codewithmehdi.myofflinemap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codewithmehdi.myofflinemap.databinding.ActivityPoliceMainBinding

class PoliceActivity : AppCompatActivity() {

    private lateinit var b: ActivityPoliceMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityPoliceMainBinding.inflate(layoutInflater)
        setContentView(b.root)
    }
}
