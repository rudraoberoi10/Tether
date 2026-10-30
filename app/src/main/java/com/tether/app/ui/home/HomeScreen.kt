package com.tether.app.ui.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tether.app.TetherApp
import com.tether.app.data.model.Circle
import com.tether.app.data.model.MemberStatus
import com.tether.app.ui.components.MemberAvatar
import com.tether.app.ui.components.PrimaryButton
import com.tether.app.ui.components.SecondaryButton
import com.tether.app.ui.components.StatusChip
import com.tether.app.ui.theme.BorderSubtle
import com.tether.app.ui.theme.TetherBg
import com.tether.app.ui.theme.TetherSurface
import com.tether.app.ui.theme.TetherSurfaceElevated
import com.tether.app.ui.theme.TetherTeal
import com.tether.app.ui.theme.TextPrimary
import com.tether.app.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onCreateCircleClick: () -> Unit,
    onJoinCircleClick: () -> Unit,
    onCircleClick: (String) -> Unit
) {
    val app = LocalContext.current.applicationContext as TetherApp
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.provideFactory(
            repository = app.container.repository,
            userPrefs = app.container.userPreferences,
            geofenceManager = app.container.geofenceManager
        )
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = TetherBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tether",
                    color = TetherTeal,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                MemberAvatar(
                    name = state.userName.ifBlank { "You" },
                    avatarColorIndex = state.userAvatarColor,
                    size = 40.dp
                )
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton(
                    text = "Create circle",
                    onClick = onCreateCircleClick
                )
                SecondaryButton(
                    text = "Join with code",
                    onClick = onJoinCircleClick
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = TetherTeal
                    )
                }
                state.circles.isEmpty() -> {
                    EmptyHomeState(
                        onCreateCircle = onCreateCircleClick,
                        onJoinCircle = onJoinCircleClick
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Your circles (${state.circles.size})",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(state.circles, key = { it.id }) { circle ->
                            CircleCard(
                                circle = circle,
                                onClick = { onCircleClick(circle.id) }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CircleCard(
    circle: Circle,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TetherSurface, RoundedCornerShape(24.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = circle.name,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(status = MemberStatus.INSIDE)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = TetherTeal,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${circle.fenceLabel} • ${circle.fenceRadiusM.toInt()}m fence",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${circle.memberIds.size} members",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyHomeState(
    onCreateCircle: () -> Unit,
    onJoinCircle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(TetherSurfaceElevated, CircleShape)
                .border(2.dp, TetherTeal.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = TetherTeal,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No circles yet",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create one to protect your people, or join with a 6-character code.",
            color = TextSecondary,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}