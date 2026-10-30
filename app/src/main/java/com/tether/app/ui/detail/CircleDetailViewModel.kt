package com.tether.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tether.app.data.repository.TetherRepository
import com.tether.app.geofence.GeofenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CircleDetailViewModel(
    private val circleId: String,
    private val repository: TetherRepository,
    private val geofenceManager: GeofenceManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CircleDetailUiState(currentUserId = repository.currentUserId.orEmpty())
    )
    val uiState: StateFlow<CircleDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeCircle(circleId),
                repository.observeCircleMembers(circleId)
            ) { circle, members ->
                Pair(circle, members)
            }.collect { (circle, members) ->
                _uiState.update {
                    it.copy(
                        circle = circle,
                        members = members,
                        isLoading = false
                    )
                }
                // Compute initial status immediately on screen load
                if (circle != null) {
                    geofenceManager.computeInitialStatus(circle)
                }
            }
        }
    }

    fun leaveCircle(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLeaving = true, errorMessage = null) }
            val result = repository.leaveCircle(circleId)
            result.onSuccess {
                _uiState.update { it.copy(isLeaving = false) }
                onSuccess()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLeaving = false,
                        errorMessage = err.localizedMessage ?: "Failed to leave circle"
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            circleId: String,
            repository: TetherRepository,
            geofenceManager: GeofenceManager
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CircleDetailViewModel(circleId, repository, geofenceManager)
            }
        }
    }
}