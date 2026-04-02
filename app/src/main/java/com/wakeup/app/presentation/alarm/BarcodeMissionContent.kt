package com.wakeup.app.presentation.alarm

import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.wakeup.app.core.camera.CameraXController
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.data.mission.MissionData
import com.wakeup.app.data.mission.barcodeValue
import com.wakeup.app.data.mission.barcodeFormat
import com.wakeup.app.data.mission.photoReferencePath
import com.wakeup.app.data.mission.photoReferenceHash
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers

/**
 * Composable for Barcode scanning mission.
 * Uses ML Kit Barcode Scanning with CameraX preview.
 */
@Composable
fun BarcodeMissionContent(
    missionData: MissionData,
    onMissionComplete: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val expectedBarcode = missionData.barcodeValue ?: ""
    val expectedFormat = missionData.metadata["barcodeFormat"]?.toIntOrNull() ?: -1

    var scanState by remember { mutableStateOf<BarcodeScanState>(BarcodeScanState.Scanning) }
    var lastScannedValue by remember { mutableStateOf("") }
    var scanAttempts by remember { mutableStateOf(0) }

    val barcodeScanner = remember { BarcodeScanning.getClient() }
    val cameraController = remember { CameraXController(context) }

    // Initialize camera
    LaunchedEffect(Unit) {
        cameraController.initialize()
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            barcodeScanner.close()
            cameraController.release()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Scan the saved barcode",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Point your camera at the barcode you saved",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Camera preview with scanner overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, WakeUpColors.iosBlue, RoundedCornerShape(16.dp))
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
                            enableAnalysis = scanState == BarcodeScanState.Scanning,
                            analysisListener = { imageProxy: ImageProxy ->
                                processBarcodeImage(
                                    imageProxy = imageProxy,
                                    barcodeScanner = barcodeScanner,
                                    expectedValue = expectedBarcode,
                                    expectedFormat = expectedFormat,
                                    onScanResult = { success, value ->
                                        lastScannedValue = value
                                        if (success) {
                                            scanState = BarcodeScanState.Success
                                            onMissionComplete(true)
                                        } else {
                                            scanAttempts++
                                            if (scanAttempts >= 3) {
                                                // After 3 failed attempts, show error but keep trying
                                                scanState = BarcodeScanState.Error("Barcode doesn't match. Keep trying...")
                                            }
                                        }
                                    }
                                )
                            }
                        )
                    }
                )
            } else {
                // Loading state
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                // Corner brackets for scanner effect
                ScannerOverlay()
            }

            // Status indicator
            when (scanState) {
                is BarcodeScanState.Success -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(WakeUpColors.iosGreen.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = WakeUpColors.iosGreen,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                is BarcodeScanState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(WakeUpColors.iosRed.copy(alpha = 0.8f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = (scanState as BarcodeScanState.Error).message,
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Auto-clear error after 2 seconds
                    LaunchedEffect(scanState) {
                        delay(2000)
                        scanState = BarcodeScanState.Scanning
                    }
                }
                else -> { /* Scanning - no overlay */ }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Instructions
        if (expectedBarcode.isNotEmpty()) {
            InfoCard(
                icon = Icons.Default.QrCodeScanner,
                title = "Expected Barcode",
                content = "${expectedBarcode.take(20)}${if (expectedBarcode.length > 20) "..." else ""}"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manual fallback button
        if (scanAttempts >= 5) {
            Button(
                onClick = { onMissionComplete(true) }, // Allow manual skip after many attempts
                colors = ButtonDefaults.buttonColors(containerColor = WakeUpColors.iosBlue)
            ) {
                Text("Skip Mission (Mission Too Hard)")
            }
        }
    }
}

/**
 * Process image for barcode detection
 */
private fun processBarcodeImage(
    imageProxy: ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    expectedValue: String,
    expectedFormat: Int,
    onScanResult: (Boolean, String) -> Unit
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
                    val value = barcode.rawValue ?: ""
                    val format = barcode.format

                    // Check if barcode matches expected value (if set)
                    // Or just accept any valid barcode if no expected value
                    val matches = if (expectedValue.isNotEmpty()) {
                        value == expectedValue
                    } else {
                        true
                    }

                    // Check format if specified
                    val formatMatches = if (expectedFormat != -1) {
                        format == expectedFormat
                    } else {
                        true
                    }

                    if (matches && formatMatches && value.isNotEmpty()) {
                        onScanResult(true, value)
                        return@addOnSuccessListener
                    } else if (value.isNotEmpty()) {
                        onScanResult(false, value)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("BarcodeMission", "Barcode detection failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

/**
 * Scanner overlay with corner brackets using drawBehind
 */
@Composable
private fun ScannerOverlay() {
    val cornerColor = WakeUpColors.iosBlue
    val cornerSize = 24.dp
    val cornerThickness = 3.dp

    Box(modifier = Modifier.fillMaxSize()) {
        // Top left corner
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(cornerSize)
                .drawBehind {
                    // Draw top border
                    drawLine(
                        color = cornerColor,
                        start = Offset(0f, cornerThickness.value / 2),
                        end = Offset(size.width * 0.7f, cornerThickness.value / 2),
                        strokeWidth = cornerThickness.toPx()
                    )
                    // Draw left border
                    drawLine(
                        color = cornerColor,
                        start = Offset(cornerThickness.value / 2, 0f),
                        end = Offset(cornerThickness.value / 2, size.height * 0.7f),
                        strokeWidth = cornerThickness.toPx()
                    )
                }
        )

        // Top right corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(cornerSize)
                .drawBehind {
                    // Draw top border
                    drawLine(
                        color = cornerColor,
                        start = Offset(size.width * 0.3f, cornerThickness.value / 2),
                        end = Offset(size.width, cornerThickness.value / 2),
                        strokeWidth = cornerThickness.toPx()
                    )
                    // Draw right border
                    drawLine(
                        color = cornerColor,
                        start = Offset(size.width - cornerThickness.value / 2, 0f),
                        end = Offset(size.width - cornerThickness.value / 2, size.height * 0.7f),
                        strokeWidth = cornerThickness.toPx()
                    )
                }
        )

        // Bottom left corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(cornerSize)
                .drawBehind {
                    // Draw bottom border
                    drawLine(
                        color = cornerColor,
                        start = Offset(0f, size.height - cornerThickness.value / 2),
                        end = Offset(size.width * 0.7f, size.height - cornerThickness.value / 2),
                        strokeWidth = cornerThickness.toPx()
                    )
                    // Draw left border
                    drawLine(
                        color = cornerColor,
                        start = Offset(cornerThickness.value / 2, size.height * 0.3f),
                        end = Offset(cornerThickness.value / 2, size.height),
                        strokeWidth = cornerThickness.toPx()
                    )
                }
        )

        // Bottom right corner
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(cornerSize)
                .drawBehind {
                    // Draw bottom border
                    drawLine(
                        color = cornerColor,
                        start = Offset(size.width * 0.3f, size.height - cornerThickness.value / 2),
                        end = Offset(size.width, size.height - cornerThickness.value / 2),
                        strokeWidth = cornerThickness.toPx()
                    )
                    // Draw right border
                    drawLine(
                        color = cornerColor,
                        start = Offset(size.width - cornerThickness.value / 2, size.height * 0.3f),
                        end = Offset(size.width - cornerThickness.value / 2, size.height),
                        strokeWidth = cornerThickness.toPx()
                    )
                }
        )

        // Center reticle
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp)
                .border(1.dp, cornerColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
        )
    }
}

/**
 * Info card for showing barcode info
 */
@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = WakeUpColors.iosBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = content,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

/**
 * Scan state for UI updates
 */
private sealed class BarcodeScanState {
    data object Scanning : BarcodeScanState()
    data object Success : BarcodeScanState()
    data class Error(val message: String) : BarcodeScanState()
}
