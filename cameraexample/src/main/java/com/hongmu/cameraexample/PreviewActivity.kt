package com.hongmu.cameraexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.hongmu.cameraexample.databinding.ActivityMainBinding
import com.hongmu.cameraexample.databinding.ActivityPreviewBinding

class PreviewActivity : AppCompatActivity() {
    private val binding by lazy { ActivityPreviewBinding.inflate(layoutInflater) }
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this).also {
            it.addListener({
                val cameraProvider = it.get()
                bindPreview(cameraProvider)
            }, ContextCompat.getMainExecutor(this))
        }
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview)
    }
}