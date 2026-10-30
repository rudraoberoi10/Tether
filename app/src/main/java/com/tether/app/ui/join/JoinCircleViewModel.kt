package com.tether.app.ui.join

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tether.app.data.repository.TetherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JoinCircleUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val isComplete: Boolean get() = code.length == 6
}

class JoinCircleViewModel(
    private val repository: TetherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinCircleUiState())
    val uiState: StateFlow<JoinCircleUiState> = _uiState.asStateFlow()

    fun onCodeChange(newCode: String) {
        val filtered = newCode.uppercase().filter { it.isLetterOrDigit() }.take(6)
        _uiState.update { it.copy(code = filtered, errorMessage = null) }
    }

    fun joinCircle(onSuccess: (String) -> Unit) {
        val code = _uiState.value.code
        if (code.length != 6) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.joinCircle(code)

            result.onSuccess { circleId ->
                _uiState.update { it.copy(isLoading = false) }
                onSuccess(circleId)
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Failed to join circle"
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(repository: TetherRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                JoinCircleViewModel(repository)
            }
        }
    }
}