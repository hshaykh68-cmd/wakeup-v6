package com.wakeup.app.presentation.onboarding

import android.Manifest
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeup.app.domain.usecase.GetSettingsUseCase
import com.wakeup.app.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Unified state for initial app launch flow.
 */
sealed class SplashState {
    object Loading : SplashState()
    object FirstLaunch : SplashState()  // Show VideoOnboarding
    object NeedsPermissions : SplashState()  // Show PermissionSetup
    object Completed : SplashState()  // Show Main
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        checkInitialState()
    }

    private fun checkInitialState() {
        viewModelScope.launch {
            val isOnboardingCompleted = getSettingsUseCase.isOnboardingCompleted()
            val hasPermissions = checkNotificationPermission() && checkAlarmPermission()

            _state.value = when {
                !isOnboardingCompleted -> SplashState.FirstLaunch
                !hasPermissions -> SplashState.NeedsPermissions
                else -> SplashState.Completed
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            completeOnboardingUseCase()
            // After onboarding, check if permissions are still needed
            val hasPermissions = checkNotificationPermission() && checkAlarmPermission()
            _state.value = if (hasPermissions) {
                SplashState.Completed
            } else {
                SplashState.NeedsPermissions
            }
        }
    }

    fun completePermissions() {
        _state.value = SplashState.Completed
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required before Android 13
        }
    }

    private fun checkAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // Not required before Android 12
        }
    }
}
