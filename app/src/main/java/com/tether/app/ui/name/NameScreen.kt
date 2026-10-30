package com.tether.app.ui.name

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tether.app.TetherApp
import com.tether.app.ui.components.MemberAvatar
import com.tether.app.ui.components.PrimaryButton
import com.tether.app.ui.theme.AvatarColors
import com.tether.app.ui.theme.BorderSubtle
import com.tether.app.ui.theme.TetherBg
import com.tether.app.ui.theme.TetherCoral
import com.tether.app.ui.theme.TetherSurfaceElevated
import com.tether.app.ui.theme.TetherTeal
import com.tether.app.ui.theme.TextPrimary
import com.tether.app.ui.theme.TextSecondary

@Composable
fun NameScreen(
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as TetherApp
    val viewModel: NameViewModel = viewModel(
        factory = NameViewModel.provideFactory(
            auth = app.container.auth,
            firestore = app.container.firestore,
            userPreferences = app.container.userPreferences
        )
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        containerColor = TetherBg,
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp)) {
                PrimaryButton(
                    text = "Complete setup",
                    onClick = { viewModel.completeSetup(onSetupComplete) },
                    enabled = state.isValid,
                    isLoading = state.isLoading
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your name",
                style = MaterialTheme.typography.displayLarge,
                fontSize = 28.sp,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This is the name your friends and family will see on the map and in exit alerts.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Large avatar initials preview
            MemberAvatar(
                name = state.name.ifBlank { "You" },
                avatarColorIndex = state.selectedColorIndex,
                size = 88.dp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Display name input field
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Display name (e.g. Aman)", color = TextSecondary) },
                singleLine = true,
                isError = state.errorMessage != null,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TetherTeal,
                    unfocusedBorderColor = BorderSubtle,
                    focusedContainerColor = TetherSurfaceElevated,
                    unfocusedContainerColor = TetherSurfaceElevated,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = state.errorMessage ?: "",
                    color = TetherCoral,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Avatar Color Swatches
            Text(
                text = "Pick an avatar color",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AvatarColors.forEachIndexed { index, color ->
                    val isSelected = state.selectedColorIndex == index
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(color, CircleShape)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { viewModel.onColorSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = TetherBg,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Only your circle members will see this.",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Start)
            )
        }
    }
}