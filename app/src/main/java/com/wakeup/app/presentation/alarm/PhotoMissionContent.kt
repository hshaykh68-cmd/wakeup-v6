package com.wakeup.app.presentation.alarm

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.wakeup.app.core.camera.CameraXController
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.core.util.PhotoSimilarity
import com.wakeup.app.data.mission.MissionData
import com.wakeup.app.data.mission.photoReferencePath
import com.wakeup.app.data.mission.photoReferenceHash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Composable for Photo matching mission.
 * Uses CameraX to capture photos and compares them with reference using perceptual hashing.
 */
@Composable
fun PhotoMissionContent(
    missionData: MissionData,
    onMissionComplete: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val referencePath = missionData.photoReferencePath
    val referenceHash = missionData.photoReferenceHash

    var photoState by remember { mutableStateOf<PhotoState>(PhotoState.Preview) }
    var similarity by remember { mutableFloatStateOf(0f) }
    var capturedPath by remember { mutableStateOf<String?>(null) }

    val cameraController = remember { CameraXController(context) }

    // Initialize camera
    LaunchedEffect(Unit) {
        cameraController.initialize()
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            cameraController.release()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Take a matching photo",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Capture the same photo you saved to dismiss the alarm",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Reference image preview (small)
        if (referencePath != null) {
            ReferenceImage(referencePath)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Camera preview or captured photo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, WakeUpColors.iosBlue, RoundedCornerShape(16.dp))
        ) {
            when (photoState) {
                PhotoState.Preview -> {
                    // Live camera preview
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
                                    enableAnalysis = false
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

                    // Capture button overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 24.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        IconButton(
                            onClick = {
                                photoState = PhotoState.Capturing
                                capturePhoto(
                                    cameraController = cameraController,
                                    onCaptured = { path ->
                                        capturedPath = path
                                        photoState = PhotoState.Processing

                                        // Compare with reference
                                        scope.launch {
                                            val result = comparePhotos(referencePath, referenceHash, path)
                                            similarity = result ?: 0f

                                            photoState = if (result != null && result >= 0.85f) {
                                                PhotoState.Success
                                            } else {
                                                PhotoState.Failed
                                            }
                                        }
                                    },
                                    onError = {
                                        photoState = PhotoState.Preview
                                    }
                                )
                            },
                            modifier = Modifier
                                .size(72.dp)
                                .background(WakeUpColors.iosBlue, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Capture",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                PhotoState.Capturing -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = WakeUpColors.iosBlue)
                    }
                }

                PhotoState.Processing -> {
                    // Show captured image with processing overlay
                    capturedPath?.let { path ->
                        CapturedImage(path)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Comparing photos...",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                PhotoState.Failed -> {
                    // Show captured image
                    capturedPath?.let { path ->
                        CapturedImage(path)
                    }

                    // Failed overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(WakeUpColors.iosRed.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Photos don't match",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Similarity: ${(similarity * 100).toInt()}%",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Need 85% or higher",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    photoState = PhotoState.Preview
                                    capturedPath?.let { File(it).delete() }
                                    capturedPath = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                                Text("Try Again", color = WakeUpColors.iosRed)
                            }
                        }
                    }
                }

                PhotoState.Success -> {
                    // Show matched image
                    capturedPath?.let { path ->
                        CapturedImage(path)
                    }

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
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Match found!",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Similarity: ${(similarity * 100).toInt()}%",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Auto-complete after delay
                    LaunchedEffect(photoState) {
                        kotlinx.coroutines.delay(1500)
                        onMissionComplete(true)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Similarity progress indicator (visible during comparison)
        if (photoState == PhotoState.Processing || photoState == PhotoState.Failed) {
            Column(
                modifier = Modifier.fillMaxWidth(0.8f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (photoState == PhotoState.Processing) "Analyzing..." else "Not similar enough",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { similarity.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (similarity >= 0.85f) WakeUpColors.iosGreen else WakeUpColors.iosRed,
                    trackColor = Color.White.copy(alpha = 0.2f),
                )
            }
        }
    }
}

/**
 * Capture photo using CameraX
 */
private fun capturePhoto(
    cameraController: CameraXController,
    onCaptured: (String) -> Unit,
    onError: () -> Unit
) {
    cameraController.takePhoto(
        onImageCaptured = { outputResults ->
            outputResults.savedUri?.path?.let { path ->
                onCaptured(path)
            } ?: onError()
        },
        onError = { onError() }
    )
}

/**
 * Compare captured photo with reference
 */
suspend fun comparePhotos(
    referencePath: String?,
    referenceHash: String?,
    capturedPath: String
): Float? = withContext(Dispatchers.Default) {
    if (referencePath == null && referenceHash == null) {
        // No reference, accept any photo
        return@withContext 1.0f
    }

    // If we have both paths, compare directly
    if (referencePath != null) {
        val similarity = PhotoSimilarity.compareImages(referencePath, capturedPath)
        if (similarity != null) {
            return@withContext similarity
        }
    }

    // Fall back to hash comparison if we have reference hash
    if (referenceHash != null) {
        val capturedHash = PhotoSimilarity.computeHash(File(capturedPath))
        if (capturedHash != null) {
            return@withContext PhotoSimilarity.compareHashes(referenceHash, capturedHash)
        }
    }

    null
}

/**
 * Display reference image
 */
@Composable
private fun ReferenceImage(path: String) {
    val bitmap = remember(path) {
        try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Reference Photo",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Reference photo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, WakeUpColors.iosBlue, RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * Display captured image
 */
@Composable
private fun CapturedImage(path: String) {
    val bitmap = remember(path) {
        try {
            BitmapFactory.decodeFile(path)
        } catch (e: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Captured photo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Photo mission states
 */
private enum class PhotoState {
    Preview,      // Showing camera preview
    Capturing,    // Taking photo
    Processing,   // Comparing images
    Failed,       // No match
    Success       // Match found
}
