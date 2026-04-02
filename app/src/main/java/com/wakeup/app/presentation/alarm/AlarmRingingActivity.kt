package com.wakeup.app.presentation.alarm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeup.app.core.theme.WakeUpColors
import com.wakeup.app.core.theme.WakeUpTheme
import com.wakeup.app.core.util.ShakeDetector
import com.wakeup.app.domain.service.HapticsController
import com.wakeup.app.data.alarm.AlarmReceiver
import com.wakeup.app.data.alarm.AlarmSchedulerImpl
import com.wakeup.app.data.mission.MissionFactory
import com.wakeup.app.domain.model.Alarm
import com.wakeup.app.domain.model.MissionDifficulty
import com.wakeup.app.domain.model.MissionType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.sin

@AndroidEntryPoint
class AlarmRingingActivity : ComponentActivity() {

    @Inject
    lateinit var alarmScheduler: AlarmSchedulerImpl

    @Inject
    lateinit var hapticsController: HapticsController

    private var alarmStartTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        alarmStartTime = System.currentTimeMillis()

        val alarmId = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_ID) ?: return
        val alarmLabel = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_LABEL) ?: "Alarm"
        val missionType = intent.getStringExtra(AlarmReceiver.EXTRA_MISSION_TYPE)?.let {
            MissionType.valueOf(it)
        } ?: MissionType.MATH
        val missionDifficulty = intent.getStringExtra(AlarmReceiver.EXTRA_MISSION_DIFFICULTY)?.let {
            MissionDifficulty.valueOf(it)
        } ?: MissionDifficulty.EASY
        val strictMode = intent.getBooleanExtra(AlarmReceiver.EXTRA_STRICT_MODE, false)
        val snoozeEnabled = intent.getBooleanExtra(AlarmReceiver.EXTRA_SNOOZE_ENABLED, true)
        val snoozeInterval = intent.getIntExtra(AlarmReceiver.EXTRA_SNOOZE_INTERVAL, 5)
        val maxSnoozes = intent.getIntExtra(AlarmReceiver.EXTRA_MAX_SNOOZES, 3)
        val photoReferencePath = intent.getStringExtra(AlarmReceiver.EXTRA_PHOTO_REFERENCE_PATH)
        val photoReferenceHash = intent.getStringExtra(AlarmReceiver.EXTRA_PHOTO_REFERENCE_HASH)
        val barcodeValue = intent.getStringExtra(AlarmReceiver.EXTRA_BARCODE_VALUE)
        val barcodeFormat = intent.getStringExtra(AlarmReceiver.EXTRA_BARCODE_FORMAT)

        setContent {
            WakeUpTheme {
                AlarmRingingScreen(
                    alarmId = alarmId,
                    alarmLabel = alarmLabel,
                    missionType = missionType,
                    missionDifficulty = missionDifficulty,
                    strictMode = strictMode,
                    snoozeEnabled = snoozeEnabled,
                    snoozeInterval = snoozeInterval,
                    maxSnoozes = maxSnoozes,
                    photoReferencePath = photoReferencePath,
                    photoReferenceHash = photoReferenceHash,
                    barcodeValue = barcodeValue,
                    barcodeFormat = barcodeFormat,
                    alarmStartTime = alarmStartTime,
                    hapticsController = hapticsController,
                    onSnooze = { snoozeMinutes ->
                        val now = java.time.LocalDateTime.now()
                        val snoozeTime = now.plusMinutes(snoozeMinutes.toLong())
                        val alarm = Alarm(
                            id = alarmId,
                            hour = snoozeTime.hour,
                            minute = snoozeTime.minute,
                            label = alarmLabel,
                            missionType = missionType,
                            missionDifficulty = missionDifficulty,
                            strictMode = strictMode,
                            snoozeEnabled = snoozeEnabled,
                            snoozeInterval = snoozeInterval,
                            maxSnoozes = maxSnoozes, // Preserve max snoozes from original alarm
                            photoReferencePath = photoReferencePath,
                            photoReferenceHash = photoReferenceHash,
                            barcodeValue = barcodeValue,
                            barcodeFormat = barcodeFormat?.toIntOrNull()
                        )
                        alarmScheduler.snooze(alarm, snoozeMinutes)
                        finish()
                    },
                    onDismiss = { 
                        hapticsController.performSuccess()
                        finish() 
                    }
                )
            }
        }
    }

    companion object {
        fun createIntent(
            context: Context,
            alarmId: String,
            alarmLabel: String,
            missionType: MissionType,
            missionDifficulty: MissionDifficulty,
            strictMode: Boolean,
            snoozeEnabled: Boolean,
            snoozeInterval: Int = 5,
            photoReferencePath: String? = null,
            photoReferenceHash: String? = null,
            barcodeValue: String? = null,
            barcodeFormat: String? = null
        ): Intent {
            return Intent(context, AlarmRingingActivity::class.java).apply {
                putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmReceiver.EXTRA_ALARM_LABEL, alarmLabel)
                putExtra(AlarmReceiver.EXTRA_MISSION_TYPE, missionType.name)
                putExtra(AlarmReceiver.EXTRA_MISSION_DIFFICULTY, missionDifficulty.name)
                putExtra(AlarmReceiver.EXTRA_STRICT_MODE, strictMode)
                putExtra(AlarmReceiver.EXTRA_SNOOZE_ENABLED, snoozeEnabled)
                putExtra(AlarmReceiver.EXTRA_SNOOZE_INTERVAL, snoozeInterval)
                putExtra(AlarmReceiver.EXTRA_PHOTO_REFERENCE_PATH, photoReferencePath)
                putExtra(AlarmReceiver.EXTRA_PHOTO_REFERENCE_HASH, photoReferenceHash)
                putExtra(AlarmReceiver.EXTRA_BARCODE_VALUE, barcodeValue)
                putExtra(AlarmReceiver.EXTRA_BARCODE_FORMAT, barcodeFormat)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
}

@Composable
fun AlarmRingingScreen(
    alarmId: String,
    alarmLabel: String,
    missionType: MissionType,
    missionDifficulty: MissionDifficulty,
    strictMode: Boolean,
    snoozeEnabled: Boolean,
    snoozeInterval: Int = 5,
    maxSnoozes: Int = 3,
    photoReferencePath: String? = null,
    photoReferenceHash: String? = null,
    barcodeValue: String? = null,
    barcodeFormat: String? = null,
    alarmStartTime: Long = System.currentTimeMillis(),
    hapticsController: HapticsController,
    onSnooze: (Int) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showMission by remember { mutableStateOf(false) }
    var snoozeCount by remember { mutableIntStateOf(0) }

    val mission = remember {
        MissionFactory.createMission(missionType, missionDifficulty)
    }
    val missionData = remember { mission.generate() }

    var currentTime by remember { mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))
            elapsedSeconds = ((System.currentTimeMillis() - alarmStartTime) / 1000).toInt()
            delay(1000)
        }
    }

    val elapsedTimeText = remember(elapsedSeconds) {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        String.format("Ringing for %02d:%02d", minutes, seconds)
    }

    // Shared infinite transition for all animations - prevents 24+ concurrent transitions
    val sharedInfiniteTransition = rememberInfiniteTransition(label = "shared")

    if (showMission) {
        MissionScreen(
            missionType = missionType,
            missionDifficulty = missionDifficulty,
            missionData = missionData,
            alarmId = alarmId,
            photoReferencePath = photoReferencePath,
            photoReferenceHash = photoReferenceHash,
            barcodeValue = barcodeValue,
            barcodeFormat = barcodeFormat,
            snoozeInterval = snoozeInterval,
            hapticsController = hapticsController,
            onMissionComplete = { success ->
                if (success) {
                    hapticsController.performSuccess()
                    context.stopService(
                        Intent(context, com.wakeup.app.data.alarm.AlarmService::class.java)
                    )
                    onDismiss()
                }
            },
            onSnooze = if (snoozeEnabled && !strictMode) {
                {
                    snoozeCount++
                    hapticsController.performMediumImpact()
                    context.stopService(
                        Intent(context, com.wakeup.app.data.alarm.AlarmService::class.java)
                    )
                    onSnooze(snoozeInterval)
                }
            } else null
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Pass shared transition to all animated components
            AnimatedGradientBackground(sharedInfiniteTransition)
            SoundWaveAnimation(sharedInfiniteTransition)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(60.dp))

                    PulsingAlarmIcon(sharedInfiniteTransition)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = currentTime,
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = alarmLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = elapsedTimeText,
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = WakeUpColors.iosOrange,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = "Complete ${missionType.displayName()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = mission.getDescription(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Difficulty: ${missionDifficulty.displayName()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = when (missionDifficulty) {
                            MissionDifficulty.EASY -> WakeUpColors.iosGreen
                            MissionDifficulty.MEDIUM -> WakeUpColors.iosOrange
                            MissionDifficulty.HARD -> WakeUpColors.iosRed
                        },
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PulsingStartMissionButton(
                        onClick = { showMission = true },
                        sharedTransition = sharedInfiniteTransition
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (snoozeEnabled && !strictMode) {
                        BreathingSnoozeButton(
                            onClick = {
                                hapticsController.performMediumImpact()
                                context.stopService(
                                    Intent(context, com.wakeup.app.data.alarm.AlarmService::class.java)
                                )
                                onSnooze(snoozeInterval)
                            },
                            snoozeInterval = snoozeInterval,
                            sharedTransition = sharedInfiniteTransition
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedGradientBackground(sharedTransition: InfiniteTransition) {
    val animatedOffset by sharedTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient"
    )

    val colorStops = remember(animatedOffset) {
        val offset = animatedOffset
        listOf(
            0f to Color(0xFF1a1a2e),
            (0.2f + offset * 0.1f).coerceAtMost(1f) to Color(0xFF16213e),
            (0.4f + offset * 0.15f).coerceAtMost(1f) to WakeUpColors.iosPurple.copy(alpha = 0.6f),
            (0.6f + offset * 0.1f).coerceAtMost(1f) to Color(0xFF0f3460),
            (0.8f - offset * 0.1f).coerceAtLeast(0f) to WakeUpColors.iosPink.copy(alpha = 0.4f),
            1f to Color(0xFF1a1a2e)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colorStops = colorStops.toTypedArray()
                )
            )
    )
}

@Composable
private fun SoundWaveAnimation(sharedTransition: InfiniteTransition) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        repeat(4) { index ->
            val delay = index * 200
            // Derive scale and alpha from a single shared time value
            val phase by sharedTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, delayMillis = delay, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "wave_phase_$index"
            )
            
            // Calculate scale and alpha from phase (0-1)
            val scale = 1f + (phase * 1.5f)  // 1.0 -> 2.5
            val alpha = 0.3f * (1f - phase) * (1f - index * 0.15f)

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale)
                    .background(
                        color = WakeUpColors.iosRed.copy(alpha = alpha.coerceIn(0f, 1f)),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun PulsingAlarmIcon(sharedTransition: InfiniteTransition) {
    val scale by sharedTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_pulse"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .scale(scale)
            .background(
                color = WakeUpColors.iosRed.copy(alpha = 0.2f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Alarm,
            contentDescription = "Alarm ringing",
            modifier = Modifier.size(40.dp),
            tint = WakeUpColors.iosRed
        )
    }
}

@Composable
private fun PulsingStartMissionButton(
    onClick: () -> Unit,
    sharedTransition: InfiniteTransition
) {
    val phase by sharedTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "button_ring"
    )
    
    // Derive scale and alpha from phase
    val ringScale = 1f + (phase * 0.3f)  // 1.0 -> 1.3
    val ringAlpha = 0.6f * (1f - phase)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .scale(ringScale)
                .background(
                    color = WakeUpColors.iosBlue.copy(alpha = ringAlpha),
                    shape = RoundedCornerShape(20.dp)
                )
        )

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .semantics { contentDescription = "Start mission to dismiss alarm" },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WakeUpColors.iosBlue
            )
        ) {
            Text(
                text = "Start Mission",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BreathingSnoozeButton(
    onClick: () -> Unit,
    snoozeInterval: Int,
    sharedTransition: InfiniteTransition
) {
    val phase by sharedTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = SineEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "snooze_breath"
    )
    
    // Derive scale and alpha from phase using sine curve
    val scale = 1f + (phase * 0.03f)  // 1.0 -> 1.03
    val alpha = 0.15f + (phase * 0.1f)  // 0.15 -> 0.25

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .semantics { contentDescription = "Snooze alarm for $snoozeInterval minutes" },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White.copy(alpha = alpha),
            contentColor = Color.White
        )
    ) {
        Text(
            text = "Snooze ($snoozeInterval min)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

private val SineEasing = Easing { fraction ->
    (sin(fraction * PI) / 2).toFloat() + 0.5f
}
