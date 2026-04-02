package com.wakeup.app.presentation.alarm

import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.wakeup.app.core.camera.CameraXController
import com.wakeup.app.core.theme.WakeUpColors

/**
 * Dialog content for barcode setup during alarm creation.
 * Allows user to scan a barcode to save as the reference.
 */
@Composable
fun BarcodeSetupDialog(
    onBarcodeScanned: (String, Int) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var scanState by remember { mutableStateOf<BarcodeSetupScanState>(BarcodeSetupScanState.Scanning) }
    var detectedBarcode by remember { mutableStateOf<Pair<String, Int>?>(null) }

    val barcodeScanner = remember { BarcodeScanning.getClient() }
    val cameraController = remember { CameraXController(context) }

    LaunchedEffect(Unit) {
        cameraController.initialize()
    }

    DisposableEffect(Unit) {
        onDispose {
            barcodeScanner.close()
            cameraController.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        Text(
            text = "Point camera at barcode",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (cameraController.isReady.value) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    },
                    update = { previewView ->
                        cameraController.startCamera(
                            lifecycleOwner = lifecycleOwner,
                            previewView = previewView,
                            enableAnalysis = scanState == BarcodeSetupScanState.Scanning,
                            analysisListener = { imageProxy: ImageProxy ->
                                processBarcodeForSetup(
                                    imageProxy = imageProxy,
                                    barcodeScanner = barcodeScanner,
                                    onBarcodeDetected = { barcode, format ->
                                        if (scanState == BarcodeSetupScanState.Scanning) {
                                            detectedBarcode = barcode to format
                                            scanState = BarcodeSetupScanState.Detected
                                            onBarcodeScanned(barcode, format)
                                        }
                                    }
                                )
                            }
                        )
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WakeUpColors.iosBlue)
                }
            }

            // Scanner overlay
            ScannerOverlayContent()

            // Success overlay
            if (scanState == BarcodeSetupScanState.Detected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(WakeUpColors.iosGreen.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = WakeUpColors.iosGreen,
                        modifier = Modifier.size(48.dp)
                    )
                        Text(
                            text = "Barcode captured!",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        detectedBarcode?.first?.let { value ->
                            Text(
                                text = value.take(20),
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Process image for barcode detection during setup
 */
private fun processBarcodeForSetup(
    imageProxy: ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onBarcodeDetected: (String, Int) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val value = barcode.rawValue
                    val format = barcode.format
                    if (!value.isNullOrEmpty()) {
                        onBarcodeDetected(value, format)
                        return@addOnSuccessListener
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

/**
 * Scanner overlay for setup dialog
 */
@Composable
private fun ScannerOverlayContent() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Corner brackets
        val cornerColor = WakeUpColors.iosBlue
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            // Top left
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(24.dp)
                    .background(Color.Transparent)
            )

            // Center reticle
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(200.dp, 100.dp)
                    .background(Color.Transparent)
            ) {
                // Draw corner indicators
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 4f
                    val cornerLength = 40f

                    // Top-left corner
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.White,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(cornerLength, 0f),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.White,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(0f, cornerLength),
                        strokeWidth = strokeWidth
                    )

                    // Top-right corner
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.White,
                        start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width - cornerLength, 0f),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.White,
                        start = androidx.compose.ui.geometry.Offset(size.width, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, cornerLength),
                        strokeWidth = strokeWidth
                    )

                    // Bottom-left corner
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.White,
                        start = androidx.compose.ui.geometry.Offset(0f, size.height),
                        end = androidx.compose.ui.geometry.Offset(cornerLength, size.height),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.White,
                        start = androidx.compose.ui.geometry.Offset(0f, size.height),
                        end = androidx.compose.ui.geometry.Offset(0f, size.height - cornerLength),
                        strokeWidth = strokeWidth
                    )

                    // Bottom-right corner
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.White,
                        start = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        end = androidx.compose.ui.geometry.Offset(size.width - cornerLength, size.height),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = androidx.compose.ui.graphics.Color.White,
                        start = androidx.compose.ui.geometry.Offset(size.width, size.height),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height - cornerLength),
                        strokeWidth = strokeWidth
                    )
                }
            }
        }
    }
}

private sealed class BarcodeSetupScanState {
    data object Scanning : BarcodeSetupScanState()
    data object Detected : BarcodeSetupScanState()
}
