package com.tether.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tether.app.data.local.UserPreferences
import com.tether.app.data.repository.TetherRepository
import com.tether.app.geofence.GeofenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: TetherRepository,
    private val userPrefs: UserPreferences,
    private val geofenceManager: GeofenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            userName = userPrefs.displayName,
            userAvatarColor = userPrefs.avatarColor
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeUserCircles().collect { circleList ->
                _uiState.update {
                    it.copy(
                        circles = circleList,
                        isLoading = false,
                        userName = userPrefs.displayName,
                        userAvatarColor = userPrefs.avatarColor
                    )
                }
                // Automatically re-sync active hardware geofences with Play Services
                geofenceManager.syncFences(circleList)
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: TetherRepository,
            userPrefs: UserPreferences,
            geofenceManager: GeofenceManager
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(repository, userPrefs, geofenceManager)
            }
        }
    }
}