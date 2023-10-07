package com.hongmu.overlayexample

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.hongmu.overlayexample.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "정상적인 서비스 이용을 위해 권한을 허용하여 주세요", Toast.LENGTH_SHORT).show()
        } else {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if(isGranted) {
            val intent = Intent(this, CameraService::class.java)
            ContextCompat.startForegroundService(this, intent)
        } else {
            Toast.makeText(this, "정상적인 서비스 이용을 위해 권한을 허용하여 주세요", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            if(!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                launcher.launch(intent)
            } else {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }

        binding.btnStop.setOnClickListener {
            val intent = Intent(this, CameraService::class.java)
            stopService(intent)
        }
    }
}