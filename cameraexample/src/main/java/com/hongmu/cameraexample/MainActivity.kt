package com.hongmu.cameraexample

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.hongmu.cameraexample.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var storagePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>

    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { isGranted ->
            if(isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다!", Toast.LENGTH_SHORT).show()
            }
        }

        storagePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { isGranted ->
            if(isGranted) {
                updateView()
            } else {
                Toast.makeText(this, "외부 저장소 권한이 필요합니다!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.TakePicture()) { isSuccess ->
            if(isSuccess) {
                binding.imagePreview.setImageURI(photoUri)
            }

        }

        storagePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun updateView() {
        binding.btnCamera.setOnClickListener {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }

        binding.btnPreview.setOnClickListener {
            startActivity(Intent(this, PreviewActivity::class.java))
        }
    }

    private fun openCamera() {
        val photoFile = File.createTempFile(
            "IMG_",
            ".jpg",
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )

        photoUri = FileProvider.getUriForFile(
            this,
            "$packageName.provider",
            photoFile
        )

        cameraLauncher.launch(photoUri)
    }
}