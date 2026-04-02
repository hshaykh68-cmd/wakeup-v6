package com.wakeup.app.core.camera

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Composable for displaying CameraX preview.
 * Handles camera lifecycle and cleanup automatically.
 *
 * @param modifier Modifier for the preview layout
 * @param controller CameraXController instance
 * @param enableAnalysis Whether to enable image analysis (for barcode scanning)
 * @param analysisListener Callback for image analysis frames
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    controller: CameraXController? = null,
    enableAnalysis: Boolean = false,
    analysisListener: ((androidx.camera.core.ImageProxy) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraController = remember(controller) {
        controller ?: CameraXController(context)
    }

    // Initialize controller if not provided
    DisposableEffect(Unit) {
        if (controller == null) {
            cameraController.initialize()
        }
        onDispose { }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        update = { previewView ->
            if (cameraController.isReady.value) {
                cameraController.startCamera(
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    enableAnalysis = enableAnalysis,
                    analysisListener = analysisListener
                )
            }
        }
    )

    // Cleanup when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            if (controller == null) {
                cameraController.release()
            }
        }
    }
}
