package com.tether.app.ui.name

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.tether.app.data.local.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NameViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(NameUiState())
    val uiState: StateFlow<NameUiState> = _uiState.asStateFlow()

    fun onNameChange(newName: String) {
        if (newName.length <= 24) {
            _uiState.update { it.copy(name = newName, errorMessage = null) }
        }
    }

    fun onColorSelected(index: Int) {
        _uiState.update { it.copy(selectedColorIndex = index) }
    }

    fun completeSetup(onSuccess: () -> Unit) {
        val trimmedName = _uiState.value.name.trim()
        if (trimmedName.length < 2) {
            _uiState.update { it.copy(errorMessage = "Name must be at least 2 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Silent Firebase Anonymous Auth
                val currentUser = auth.currentUser ?: auth.signInAnonymously().await().user
                val uid = checkNotNull(currentUser?.uid) { "Authentication returned null UID" }

                // Save profile directly to Firestore users/{uid}
                val profileData = hashMapOf(
                    "displayName" to trimmedName,
                    "avatarColor" to _uiState.value.selectedColorIndex,
                    "createdAt" to FieldValue.serverTimestamp()
                )
                firestore.collection("users").document(uid).set(profileData).await()

                // Cache credentials locally for fast background access
                userPreferences.userId = uid
                userPreferences.displayName = trimmedName
                userPreferences.avatarColor = _uiState.value.selectedColorIndex
                userPreferences.isProfileCompleted = true

                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Failed to set up profile. Check internet connection."
                    )
                }
            }
        }
    }

    companion object {
        fun provideFactory(
            auth: FirebaseAuth,
            firestore: FirebaseFirestore,
            userPreferences: UserPreferences
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                NameViewModel(auth, firestore, userPreferences)
            }
        }
    }
}