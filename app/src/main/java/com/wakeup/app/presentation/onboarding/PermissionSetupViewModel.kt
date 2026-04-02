package com.wakeup.app.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakeup.app.domain.usecase.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionSetupViewModel @Inject constructor(
    private val saveSettingsUseCase: SaveSettingsUseCase
) : ViewModel() {

    fun completeOnboarding() {
        viewModelScope.launch {
            saveSettingsUseCase.setOnboardingCompleted(true)
        }
    }
}
