package com.hongmu.serviceexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hongmu.serviceexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            startService()
        }

        binding.btnStop.setOnClickListener {
            stopService()
        }
    }

    private fun startService() {
        startService(Intent(this, MyService::class.java).apply {
            action = MyService.ACTION_START
        })
    }

    private fun stopService() {
        stopService(Intent(this, MyService::class.java))
    }
}