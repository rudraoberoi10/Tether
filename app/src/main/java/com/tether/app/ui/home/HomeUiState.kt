package com.tether.app.ui.home

import com.tether.app.data.model.Circle

data class HomeUiState(
    val circles: List<Circle> = emptyList(),
    val isLoading: Boolean = true,
    val userName: String = "",
    val userAvatarColor: Int = 0
)