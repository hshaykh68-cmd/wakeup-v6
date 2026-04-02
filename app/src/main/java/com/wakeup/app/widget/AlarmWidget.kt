package com.wakeup.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.wakeup.app.R
import com.wakeup.app.domain.model.Alarm
import com.wakeup.app.domain.usecase.GetNextAlarmFlowUseCase
import com.wakeup.app.domain.usecase.ToggleAlarmFromWidgetUseCase
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

class AlarmWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            AlarmWidgetEntryPoint::class.java
        )
        val getNextAlarmFlowUseCase = entryPoint.getNextAlarmFlowUseCase()
        
        val alarm = getNextAlarmFlowUseCase().first()

        provideContent {
            GlanceTheme {
                AlarmWidgetContent(alarm = alarm)
            }
        }
    }
}

@Composable
private fun AlarmWidgetContent(alarm: Alarm?) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .cornerRadius(16.dp)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (alarm != null) {
            AlarmWidgetWithAlarm(alarm = alarm)
        } else {
            AlarmWidgetEmpty()
        }
    }
}

@Composable
private fun AlarmWidgetWithAlarm(alarm: Alarm) {
    Column(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_alarm_widget),
                contentDescription = "Alarm",
                modifier = GlanceModifier.size(16.dp)
            )
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = "Next Alarm",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
        
        Spacer(modifier = GlanceModifier.height(4.dp))
        
        // Alarm time
        Text(
            text = alarm.formattedTime(),
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface
            )
        )
        
        // Alarm label (if not default)
        if (alarm.label.isNotBlank() && alarm.label != "Alarm") {
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = alarm.label,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Toggle switch row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Toggle button
            Box(
                modifier = GlanceModifier
                    .background(
                        if (alarm.isEnabled) 
                            GlanceTheme.colors.primary 
                        else 
                            GlanceTheme.colors.surfaceVariant
                    )
                    .cornerRadius(16.dp)
                    .clickable(
                        actionRunCallback<ToggleAlarmAction>(
                            actionParametersOf(
                                ToggleAlarmAction.ALARM_ID_KEY to alarm.id
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (alarm.isEnabled) "ON" else "OFF",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (alarm.isEnabled) GlanceTheme.colors.onPrimary 
                            else GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
            
            Spacer(modifier = GlanceModifier.width(8.dp))
            
            // Add button
            Box(
                modifier = GlanceModifier
                    .background(GlanceTheme.colors.surfaceVariant)
                    .cornerRadius(16.dp)
                    .clickable(actionRunCallback<OpenCreateAlarmAction>())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun AlarmWidgetEmpty() {
    Column(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "No Alarms",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        // Add button
        Box(
            modifier = GlanceModifier
                .background(GlanceTheme.colors.primary)
                .cornerRadius(16.dp)
                .clickable(actionRunCallback<OpenCreateAlarmAction>())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onPrimary
                    )
                )
                Spacer(modifier = GlanceModifier.width(4.dp))
                Text(
                    text = "Add Alarm",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GlanceTheme.colors.onPrimary
                    )
                )
            }
        }
    }
}

class ToggleAlarmAction : ActionCallback {
    companion object {
        val ALARM_ID_KEY = ActionParameters.Key<String>("alarm_id")
    }
    
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val alarmId = parameters[ALARM_ID_KEY] ?: return
        
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            AlarmWidgetEntryPoint::class.java
        )
        val toggleAlarmUseCase = entryPoint.toggleAlarmFromWidgetUseCase()
        
        toggleAlarmUseCase(alarmId)
        
        // Trigger widget update
        AlarmWidget().update(context, glanceId)
    }
}

class OpenCreateAlarmAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "create_alarm")
        }
        context.startActivity(intent)
    }
}
