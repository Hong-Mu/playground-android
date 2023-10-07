package com.hongmu.overlayexample

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.hongmu.overlayexample.mediapipe.OverlayView
import com.hongmu.overlayexample.mediapipe.PoseLandmarkerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class CameraService : LifecycleService(), PoseLandmarkerHelper.LandmarkerListener {

    companion object {
        const val CHANNEL_ID = "my_foreground_service_channel"
        const val CHANNEL_NAME = "카메라 동작 알림"
    }

    //private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private lateinit var windowManager: WindowManager
    private lateinit var mView: View
    private lateinit var viewFinder: PreviewView
    private lateinit var overlay: OverlayView

    private lateinit var poseLandmarkerHelper: PoseLandmarkerHelper
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraFacing = CameraSelector.LENS_FACING_BACK

    private lateinit var backgroundExecutor: ExecutorService

    private val job = SupervisorJob()

    override fun onCreate() {
        super.onCreate()

        """
        cameraProviderFuture = ProcessCameraProvider.getInstance(this).also {
            it.addListener({
                val cameraProvider = it.get()
                bindPreview(cameraProvider)
            }, ContextCompat.getMainExecutor(this))
        }
        """


        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            0,
            0,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT,
        )

        val mInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mView = mInflater.inflate(R.layout.fragment_camera, null)

        viewFinder = mView.findViewById(R.id.viewFinder)
        overlay = mView.findViewById(R.id.overlay)
        windowManager.addView(mView, layoutParams)
        onViewCreated()
    }

    private fun onViewCreated() {
        backgroundExecutor = Executors.newSingleThreadExecutor()

        // Wait for the views to be properly laid out
        viewFinder.post {
            // Set up the camera and its use cases
            setUpCamera()
        }

        // Create the PoseLandmarkerHelper that will posele the inference
        backgroundExecutor.execute {
            poseLandmarkerHelper = PoseLandmarkerHelper(
                context = baseContext,
                runningMode = RunningMode.LIVE_STREAM,
                minPoseDetectionConfidence = PoseLandmarkerHelper.DEFAULT_POSE_DETECTION_CONFIDENCE,
                minPoseTrackingConfidence = PoseLandmarkerHelper.DEFAULT_POSE_TRACKING_CONFIDENCE,
                minPosePresenceConfidence = PoseLandmarkerHelper.DEFAULT_POSE_PRESENCE_CONFIDENCE,
                currentDelegate = PoseLandmarkerHelper.DELEGATE_CPU,
                poseLandmarkerHelperListener = this
            )
        }
    }

    private fun setUpCamera() {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(baseContext)
        cameraProviderFuture.addListener(
            {
                // CameraProvider
                cameraProvider = cameraProviderFuture.get()

                // Build and bind the camera use cases
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(baseContext)
        )
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(cameraFacing).build()

        // Preview. Only using the 4:3 ratio because this is the closest to our models
        preview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(viewFinder.display.rotation)
            .build()

        // ImageAnalysis. Using RGBA 8888 to match how our models work
        imageAnalyzer =
            ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                // The analyzer can then be assigned to the instance
                .also {
                    it.setAnalyzer(backgroundExecutor) { image ->
                        detectPose(image)
                    }
                }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalyzer
            )

            // Attach the viewfinder's surface provider to preview use case
            preview?.setSurfaceProvider(viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e("CameraFragment.TAG", "Use case binding failed", exc)
        }
    }

    private fun detectPose(imageProxy: ImageProxy) {
        if(this::poseLandmarkerHelper.isInitialized) {
            poseLandmarkerHelper.detectLiveStream(
                imageProxy = imageProxy,
                isFrontCamera = cameraFacing == CameraSelector.LENS_FACING_FRONT
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation =
            viewFinder.display.rotation
    }

    private fun createNotification() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            NotificationManagerCompat.from(this).apply {
                createNotificationChannel(channel)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotification()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Running...")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()

        startForeground(1, notification)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        windowManager.removeView(mView)
        backgroundExecutor.shutdown()
        backgroundExecutor.awaitTermination(
            Long.MAX_VALUE, TimeUnit.NANOSECONDS
        )
    }

    override fun onError(error: String, errorCode: Int) {
        //Toast.makeText(baseContext, error, Toast.LENGTH_SHORT).show()
        Timber.d(error)
    }

    override fun onResults(
        resultBundle: PoseLandmarkerHelper.ResultBundle
    ) {
        CoroutineScope(Dispatchers.Main + job).launch {
            // Pass necessary information to OverlayView for drawing on the canvas
            overlay.setResults(
                resultBundle.results.first(),
                resultBundle.inputImageHeight,
                resultBundle.inputImageWidth,
                RunningMode.LIVE_STREAM
            )

            // Force a redraw
            overlay.invalidate()
        }
    }
}