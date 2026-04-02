package com.wakeup.app.core.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Controller class for managing CameraX operations.
 * Handles camera initialization, preview, image analysis, and capture.
 */
class CameraXController(private val context: Context) {

    companion object {
        private const val TAG = "CameraXController"
    }

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var imageCapture: ImageCapture? = null

    private val executor = Executors.newSingleThreadExecutor()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _hasCameraPermission = MutableStateFlow(false)
    val hasCameraPermission: StateFlow<Boolean> = _hasCameraPermission.asStateFlow()

    private var imageAnalysisListener: ((ImageProxy) -> Unit)? = null

    /**
     * Initialize camera provider
     */
    fun initialize(onInitialized: () -> Unit = {}) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                _isReady.value = true
                onInitialized()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get camera provider", e)
                _isReady.value = false
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Start camera with preview and optional analysis
     */
    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        enableAnalysis: Boolean = false,
        analysisListener: ((ImageProxy) -> Unit)? = null
    ) {
        val provider = cameraProvider ?: run {
            Log.e(TAG, "Camera provider not initialized")
            return
        }

        this.imageAnalysisListener = analysisListener

        // Select back camera
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        // Preview use case
        preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        // Image analysis use case (for barcode scanning)
        if (enableAnalysis && analysisListener != null) {
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(executor) { imageProxy ->
                        analysisListener(imageProxy)
                    }
                }
        }

        // Image capture use case (for photo mission)
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        try {
            // Unbind all use cases before rebinding
            provider.unbindAll()

    // Bind use cases to camera - use vararg spread operator correctly
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview!!,
                *(listOfNotNull(imageAnalysis, imageCapture).toTypedArray())
            )

            // Set up tap to focus
            previewView.setOnTouchListener { view, event ->
                val action = FocusMeteringAction.Builder(
                    previewView.meteringPointFactory.createPoint(event.x, event.y),
                    FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                ).setAutoCancelDuration(3, TimeUnit.SECONDS)
                    .build()
                camera?.cameraControl?.startFocusAndMetering(action)
                view.performClick()
                true
            }

        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    /**
     * Stop camera and release resources
     */
    fun stopCamera() {
        try {
            cameraProvider?.unbindAll()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping camera", e)
        }
        imageAnalysisListener = null
    }

    /**
     * Capture image
     */
    fun takePhoto(
        onImageCaptured: (ImageCapture.OutputFileResults) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val capture = imageCapture ?: run {
            onError(IllegalStateException("ImageCapture not initialized"))
            return
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            java.io.File(
                context.cacheDir,
                "photo_${System.currentTimeMillis()}.jpg"
            )
        ).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed", exc)
                    onError(exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onImageCaptured(output)
                }
            }
        )
    }

    /**
     * Set flash mode
     */
    fun setFlashMode(mode: Int) {
        imageCapture?.flashMode = mode
    }

    /**
     * Check if camera is available
     */
    fun hasCamera(): Boolean {
        return try {
            cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Release all resources
     */
    fun release() {
        stopCamera()
        executor.shutdown()
    }
}
