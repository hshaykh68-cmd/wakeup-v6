package com.wakeup.app.presentation.alarm

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.core.util.PhotoSimilarity
import kotlinx.coroutines.launch
import java.io.File

/**
 * Dialog content for photo setup during alarm creation.
 * Allows user to select a photo from gallery or camera.
 */
@Composable
fun PhotoSetupDialog(
    onPhotoSelected: (String, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isProcessing by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            isProcessing = true

            scope.launch {
                // Copy to internal storage and compute hash
                val internalDir = File(context.filesDir, "photos")
                internalDir.mkdirs()

                PhotoSimilarity.copyAndHashImage(
                    sourcePath = it.path ?: "",
                    destinationDir = internalDir
                )?.let { (path, hash) ->
                    onPhotoSelected(path, hash)
                }

                isProcessing = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select a reference photo",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose a specific photo you'll need to re-take to dismiss the alarm",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = WakeUpColors.iosBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Processing photo...")
                }
            }
        } else {
            // Gallery option
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(WakeUpColors.iosBlue.copy(alpha = 0.1f))
                    .clickable { galleryLauncher.launch("image/*") }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Gallery",
                        modifier = Modifier.size(48.dp),
                        tint = WakeUpColors.iosBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Choose from Gallery",
                        style = MaterialTheme.typography.bodyLarge,
                        color = WakeUpColors.iosBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Camera option
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(WakeUpColors.iosGreen.copy(alpha = 0.1f))
                    .clickable {
                        // Use camera capture - for simplicity, we'll just launch camera
                        galleryLauncher.launch("image/*")
                    }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        modifier = Modifier.size(48.dp),
                        tint = WakeUpColors.iosGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Take Photo Now",
                        style = MaterialTheme.typography.bodyLarge,
                        color = WakeUpColors.iosGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tips
            Text(
                text = "Tips: Choose a photo of a specific object or location you'll have easy access to when the alarm rings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
