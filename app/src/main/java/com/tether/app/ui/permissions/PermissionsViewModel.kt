package com.tether.app.ui.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PermissionsViewModel(private val context: Context) : ViewModel() {

    private val _state = MutableStateFlow(checkPermissions())
    val state: StateFlow<PermissionState> = _state.asStateFlow()

    fun refreshPermissions() {
        _state.value = checkPermissions()
    }

    private fun checkPermissions(): PermissionState {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Granted automatically on API < 29
        }

        val notifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Granted automatically on API < 33
        }

        return PermissionState(
            hasFineLocation = fineLocation,
            hasCoarseLocation = coarseLocation,
            hasBackgroundLocation = backgroundLocation,
            hasNotificationPermission = notifications
        )
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PermissionsViewModel(context.applicationContext)
            }
        }
    }
}