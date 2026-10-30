package com.tether.app.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.maps.model.LatLng
import com.tether.app.data.repository.TetherRepository
import com.tether.app.geofence.GeofenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateCircleUiState(
    val circleName: String = "",
    val centerLatLng: LatLng = LatLng(19.2183, 72.9781), // Default coordinates (Thane/Mumbai)
    val radiusM: Float = 300f,
    val selectedLabel: String = "Home",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val isValid: Boolean get() = circleName.trim().length in 2..30
}

class CreateCircleViewModel(
    private val repository: TetherRepository,
    private val geofenceManager: GeofenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCircleUiState())
    val uiState: StateFlow<CreateCircleUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        if (name.length <= 30) {
            _uiState.update { it.copy(circleName = name, errorMessage = null) }
        }
    }

    fun onRadiusChange(radius: Float) {
        _uiState.update { it.copy(radiusM = radius) }
    }

    fun onLabelSelected(label: String) {
        _uiState.update { it.copy(selectedLabel = label) }
    }

    fun onCameraMoved(newCenter: LatLng) {
        _uiState.update { it.copy(centerLatLng = newCenter) }
    }

    fun createCircle(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        if (!state.isValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.createCircle(
                name = state.circleName,
                fenceLat = state.centerLatLng.latitude,
                fenceLng = state.centerLatLng.longitude,
                fenceRadiusM = state.radiusM.toDouble(),
                fenceLabel = state.selectedLabel
            )

            result.onSuccess { createdCircle ->
                // Compute initial status (INSIDE or OUTSIDE) without creating an event
                geofenceManager.computeInitialStatus(createdCircle)
                _uiState.update { it.copy(isLoading = false) }
                onSuccess(createdCircle.id)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Failed to create circle"
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            repository: TetherRepository,
            geofenceManager: GeofenceManager
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CreateCircleViewModel(repository, geofenceManager)
            }
        }
    }
}