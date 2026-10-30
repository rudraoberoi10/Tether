package com.tether.app.ui.name

data class NameUiState(
    val name: String = "",
    val selectedColorIndex: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val isValid: Boolean get() = name.trim().length in 2..24
}