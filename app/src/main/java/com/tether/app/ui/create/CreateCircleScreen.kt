package com.tether.app.ui.create

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle as MapCircle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.tether.app.R
import com.tether.app.TetherApp
import com.tether.app.ui.components.PrimaryButton
import com.tether.app.ui.theme.BorderSubtle
import com.tether.app.ui.theme.TetherBg
import com.tether.app.ui.theme.TetherCoral
import com.tether.app.ui.theme.TetherSurface
import com.tether.app.ui.theme.TetherSurfaceElevated
import com.tether.app.ui.theme.TetherTeal
import com.tether.app.ui.theme.TetherTealDim
import com.tether.app.ui.theme.TextPrimary
import com.tether.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCircleScreen(
    onBackClick: () -> Unit,
    onCircleCreated: (String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TetherApp
    val viewModel: CreateCircleViewModel = viewModel(
        factory = CreateCircleViewModel.provideFactory(
            repository = app.container.repository,
            geofenceManager = app.container.geofenceManager
        )
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(state.centerLatLng, 15f)
    }

    // Update center coordinates when user pans the map
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            viewModel.onCameraMoved(cameraPositionState.position.target)
        }
    }

    val labels = listOf("Home", "Campus", "Office", "Other")

    Box(modifier = Modifier.fillMaxSize()) {
        // Full screen interactive Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            // Live pulsing glowing fence ring around camera center
            MapCircle(
                center = cameraPositionState.position.target,
                radius = state.radiusM.toDouble(),
                fillColor = TetherTealDim,
                strokeColor = TetherTeal,
                strokeWidth = 4f
            )
        }

        // Fixed Center Pin Overlay (Camera center = Fence center)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = TetherTeal,
                modifier = Modifier.size(44.dp)
            )
        }

        // Top Navigation Bar
        TopAppBar(
            title = { Text("Create circle", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = TetherBg.copy(alpha = 0.85f)
            ),
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Bottom Configuration Panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(TetherSurface, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .border(1.dp, BorderSubtle, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(24.dp)
        ) {
            // Circle Name Input
            OutlinedTextField(
                value = state.circleName,
                onValueChange = viewModel::onNameChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Circle name (e.g. Home Safe)", color = TextSecondary) },
                singleLine = true,
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

            Spacer(modifier = Modifier.height(16.dp))

            // Fence Label Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                labels.forEach { label ->
                    val isSelected = state.selectedLabel == label
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) TetherTeal else TetherSurfaceElevated,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) TetherTeal else BorderSubtle,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.onLabelSelected(label) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) TetherBg else TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Radius Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Fence radius", color = TextSecondary, fontSize = 14.sp)
                Text(
                    text = "${state.radiusM.toInt()} m",
                    color = TetherTeal,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Slider(
                value = state.radiusM,
                onValueChange = viewModel::onRadiusChange,
                valueRange = 100f..2000f,
                steps = 37, // 50m increments
                colors = SliderDefaults.colors(
                    thumbColor = TetherTeal,
                    activeTrackColor = TetherTeal,
                    inactiveTrackColor = TetherSurfaceElevated
                )
            )

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = state.errorMessage ?: "",
                    color = TetherCoral,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = "Create circle",
                onClick = { viewModel.createCircle(onCircleCreated) },
                enabled = state.isValid,
                isLoading = state.isLoading
            )
        }
    }
}