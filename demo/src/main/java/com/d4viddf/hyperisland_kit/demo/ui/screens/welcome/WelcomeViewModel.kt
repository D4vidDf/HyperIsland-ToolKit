package com.d4viddf.hyperisland_kit.demo.ui.screens.welcome

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WelcomeUiState(
    val hasPermission: Boolean = false,
    val hasBeenDenied: Boolean = false
)

class WelcomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    fun checkPermission(context: Context) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        _uiState.update { it.copy(hasPermission = granted) }
    }

    fun onPermissionDenied() {
        _uiState.update { it.copy(hasBeenDenied = true) }
    }
}
