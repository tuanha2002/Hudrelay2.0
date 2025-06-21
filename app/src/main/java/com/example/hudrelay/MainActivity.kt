package com.example.hudrelay

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var etDeviceAddress: EditText
    private lateinit var btnConnect: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etDeviceAddress = findViewById(R.id.etDeviceAddress)
        btnConnect = findViewById(R.id.btnConnect)

        btnConnect.setOnClickListener {
            val address = etDeviceAddress.text.toString()
            BleSender.init(this, address)
        }
    }
}
