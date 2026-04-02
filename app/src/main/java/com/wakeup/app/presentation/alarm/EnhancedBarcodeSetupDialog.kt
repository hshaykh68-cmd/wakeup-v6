package com.wakeup.app.presentation.alarm

import android.net.Uri
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.wakeup.app.domain.model.BarcodeTemplate

/**
 * Enhanced barcode setup dialog with template selection and guided camera overlay.
 * Two-phase setup: 1) Select template, 2) Scan barcode with guidance.
 */
@Composable
fun EnhancedBarcodeSetupDialog(
    onBarcodeScanned: (String, Int, BarcodeTemplate) -> Unit,
    onCancel: () -> Unit
) {
    var setupPhase by remember { mutableStateOf(SetupPhase.TEMPLATE_SELECTION) }
    var selectedTemplate by remember { mutableStateOf<BarcodeTemplate?>(null) }
    var scannedBarcode by remember { mutableStateOf<Pair<String, Int>?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header with back button
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (setupPhase != SetupPhase.TEMPLATE_SELECTION) {
                IconButton(
                    onClick = {
                        when (setupPhase) {
                            SetupPhase.BARCODE_SCANNING -> {
                                setupPhase = SetupPhase.TEMPLATE_SELECTION
                                selectedTemplate = null
                            }
                            SetupPhase.PHOTO_CAPTURE -> setupPhase = SetupPhase.BARCODE_SCANNING
                            else -> {}
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Text(
                text = when (setupPhase) {
                    SetupPhase.TEMPLATE_SELECTION -> "Choose Object"
                    SetupPhase.BARCODE_SCANNING -> "Scan Barcode"
                    SetupPhase.PHOTO_CAPTURE -> "Verify Object"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )

            TextButton(
                onClick = onCancel,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text("Cancel", color = WakeUpColors.iosBlue)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress indicator
        SetupProgressIndicator(
            currentPhase = setupPhase,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        AnimatedContent(
            targetState = setupPhase,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
            },
            modifier = Modifier.weight(1f)
        ) { phase ->
            when (phase) {
                SetupPhase.TEMPLATE_SELECTION -> {
                    BarcodeTemplateSelection(
                        onTemplateSelected = { template ->
                            selectedTemplate = template
                            setupPhase = SetupPhase.BARCODE_SCANNING
                        }
                    )
                }

                SetupPhase.BARCODE_SCANNING -> {
                    selectedTemplate?.let { template ->
                        BarcodeScanningContent(
                            template = template,
                            onBarcodeScanned = { barcode, format ->
                                scannedBarcode = barcode to format
                                setupPhase = SetupPhase.PHOTO_CAPTURE
                            }
                        )
                    }
                }

                SetupPhase.PHOTO_CAPTURE -> {
                    // Photo capture step for combo mission
                    ComboPhotoCaptureContent(
                        onPhotoCaptured = { photoUri ->
                            scannedBarcode?.let { (barcode, format) ->
                                selectedTemplate?.let { template ->
                                    onBarcodeScanned(barcode, format, template)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupProgressIndicator(
    currentPhase: SetupPhase,
    modifier: Modifier = Modifier
) {
    val phases = listOf(SetupPhase.TEMPLATE_SELECTION, SetupPhase.BARCODE_SCANNING, SetupPhase.PHOTO_CAPTURE)
    val currentIndex = phases.indexOf(currentPhase)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        phases.forEachIndexed { index, phase ->
            val isCompleted = index < currentIndex
            val isCurrent = index == currentIndex
            val color = when {
                isCompleted -> WakeUpColors.iosGreen
                isCurrent -> WakeUpColors.iosBlue
                else -> Color.White.copy(alpha = 0.3f)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when (phase) {
                        SetupPhase.TEMPLATE_SELECTION -> "Template"
                        SetupPhase.BARCODE_SCANNING -> "Scan"
                        SetupPhase.PHOTO_CAPTURE -> "Photo"
                    },
                    fontSize = 11.sp,
                    color = color,
                    textAlign = TextAlign.Center
                )
            }

            if (index < phases.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .weight(0.5f)
                        .height(2.dp)
                        .background(
                            if (index < currentIndex) WakeUpColors.iosGreen
                            else Color.White.copy(alpha = 0.2f)
                        )
                )
            }
        }
    }
}

@Composable
private fun BarcodeScanningContent(
    template: BarcodeTemplate,
    onBarcodeScanned: (String, Int) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var scanState by remember { mutableStateOf<EnhancedBarcodeScanState>(EnhancedBarcodeScanState.Scanning) }
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
        modifier = Modifier.fillMaxWidth()
    ) {
        // Template guidance card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = WakeUpColors.iosBlue.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = template.displayName,
                    fontWeight = FontWeight.Bold,
                    color = WakeUpColors.iosBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = template.setupHint,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Example: ${template.exampleObject}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Camera preview with guided overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(16.dp))
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
                            enableAnalysis = scanState == EnhancedBarcodeScanState.Scanning,
                            analysisListener = { imageProxy: ImageProxy ->
                                processBarcode(
                                    imageProxy = imageProxy,
                                    barcodeScanner = barcodeScanner,
                                    onBarcodeDetected = { barcode, format ->
                                        if (scanState == EnhancedBarcodeScanState.Scanning) {
                                            detectedBarcode = barcode to format
                                            scanState = EnhancedBarcodeScanState.Detected
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

            // Guided scanner overlay
            GuidedScannerOverlay(template = template)

            // Success overlay
            if (scanState == EnhancedBarcodeScanState.Detected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(WakeUpColors.iosGreen.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Barcode captured!",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GuidedScannerOverlay(template: BarcodeTemplate) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Semi-transparent overlay with cutout
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        // Target bracket area
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Clear cutout area
            Box(
                modifier = Modifier
                    .size(280.dp, 160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Transparent)
            ) {
                // Corner brackets
                GuidedCornerBrackets()
            }
        }

        // Instructions at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    text = "Align barcode within the brackets",
                    modifier = Modifier.padding(12.dp),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun GuidedCornerBrackets() {
    val bracketColor = WakeUpColors.iosBlue
    val strokeWidth = 4f
    val cornerLength = 40f

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        // Top-left corner
        drawLine(
            color = bracketColor,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(cornerLength, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = bracketColor,
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(0f, cornerLength),
            strokeWidth = strokeWidth
        )

        // Top-right corner
        drawLine(
            color = bracketColor,
            start = androidx.compose.ui.geometry.Offset(size.width, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width - cornerLength, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = bracketColor,
            start = androidx.compose.ui.geometry.Offset(size.width, 0f),
            end = androidx.compose.ui.geometry.Offset(size.width, cornerLength),
            strokeWidth = strokeWidth
        )

        // Bottom-left corner
        drawLine(
            color = bracketColor,
            start = androidx.compose.ui.geometry.Offset(0f, size.height),
            end = androidx.compose.ui.geometry.Offset(cornerLength, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = bracketColor,
            start = androidx.compose.ui.geometry.Offset(0f, size.height),
            end = androidx.compose.ui.geometry.Offset(0f, size.height - cornerLength),
            strokeWidth = strokeWidth
        )

        // Bottom-right corner
        drawLine(
            color = bracketColor,
            start = androidx.compose.ui.geometry.Offset(size.width, size.height),
            end = androidx.compose.ui.geometry.Offset(size.width - cornerLength, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = bracketColor,
            start = androidx.compose.ui.geometry.Offset(size.width, size.height),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height - cornerLength),
            strokeWidth = strokeWidth
        )
    }
}

@Composable
private fun ComboPhotoCaptureContent(
    onPhotoCaptured: (Uri) -> Unit
) {
    // Placeholder for photo capture step
    // This integrates with existing PhotoMissionContent logic
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = WakeUpColors.iosGreen.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    tint = WakeUpColors.iosGreen,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Photo verification step",
                    fontWeight = FontWeight.Bold,
                    color = WakeUpColors.iosGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Take a photo of the object to verify it's really there",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { /* Photo capture handled by parent */ },
            colors = ButtonDefaults.buttonColors(containerColor = WakeUpColors.iosBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Capture Reference Photo")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = { /* Skip photo step */ }
        ) {
            Text("Skip (Barcode Only)", color = Color.White.copy(alpha = 0.7f))
        }
    }
}

private fun processBarcode(
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

private enum class SetupPhase {
    TEMPLATE_SELECTION,
    BARCODE_SCANNING,
    PHOTO_CAPTURE
}

private sealed class EnhancedBarcodeScanState {
    data object Scanning : EnhancedBarcodeScanState()
    data object Detected : EnhancedBarcodeScanState()
}
