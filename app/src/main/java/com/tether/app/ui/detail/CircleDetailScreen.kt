package com.tether.app.ui.detail

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle as MapCircle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.tether.app.R
import com.tether.app.TetherApp
import com.tether.app.data.model.Circle
import com.tether.app.data.model.Member
import com.tether.app.data.model.MemberStatus
import com.tether.app.ui.components.MemberAvatar
import com.tether.app.ui.components.StatusChip
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
fun CircleDetailScreen(
    circleId: String,
    onBackClick: () -> Unit,
    onLeaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TetherApp
    val viewModel: CircleDetailViewModel = viewModel(
        factory = CircleDetailViewModel.provideFactory(
            circleId = circleId,
            repository = app.container.repository,
            geofenceManager = app.container.geofenceManager
        )
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var showMenu by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    val hasLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    Scaffold(
        containerColor = TetherBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.circle?.name ?: "Circle",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = TextPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(TetherSurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Leave circle", color = TetherCoral) },
                            onClick = {
                                showMenu = false
                                showLeaveDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TetherBg)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TetherTeal)
            }
        } else {
            val circle = state.circle
            if (circle == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Circle no longer available", color = TextSecondary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Top 42% Map
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.42f)
                    ) {
                        DetailMap(
                            circle = circle,
                            hasLocationPermission = hasLocationPermission
                        )
                    }

                    // Bottom Sheet / Content Area
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.58f)
                            .background(TetherBg)
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        // Status Chip Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${circle.fenceLabel} boundary",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            StatusChip(
                                status = state.overallStatus,
                                outsideCount = state.outsideCount
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Invite Code Card
                        InviteCodeCard(
                            circleName = circle.name,
                            inviteCode = circle.inviteCode,
                            context = context
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Members List
                        Text(
                            text = "Members (${state.members.size})",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(state.members, key = { it.uid }) { member ->
                                MemberRow(
                                    member = member,
                                    isSelf = member.uid == state.currentUserId
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Leave Confirmation Dialog
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            containerColor = TetherSurfaceElevated,
            title = {
                Text(
                    text = "Leave ${state.circle?.name.orEmpty()}?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "You will stop being monitored and will no longer receive alerts from this circle.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        viewModel.leaveCircle(onLeaveSuccess)
                    }
                ) {
                    Text("Leave", color = TetherCoral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun DetailMap(
    circle: Circle,
    hasLocationPermission: Boolean
) {
    val context = LocalContext.current
    val fenceLocation = LatLng(circle.fenceLat, circle.fenceLng)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(fenceLocation, 15f)
    }

    LaunchedEffect(fenceLocation) {
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(fenceLocation, 15f))
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark),
            isMyLocationEnabled = hasLocationPermission
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = false,
            myLocationButtonEnabled = false
        )
    ) {
        // Center Pin
        Marker(
            state = MarkerState(position = fenceLocation),
            title = circle.name
        )

        // Translucent Fence Ring
        MapCircle(
            center = fenceLocation,
            radius = circle.fenceRadiusM,
            fillColor = TetherTealDim,
            strokeColor = TetherTeal,
            strokeWidth = 4f
        )
    }
}

@Composable
private fun InviteCodeCard(
    circleName: String,
    inviteCode: String,
    context: Context
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TetherSurface, RoundedCornerShape(20.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Invite code",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = inviteCode,
                    color = TetherTeal,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Copy Action
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Tether Invite Code", inviteCode))
                        Toast.makeText(context, "Copied $inviteCode", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(TetherSurfaceElevated, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = TetherTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Share Action
                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Join my Tether circle $circleName with code $inviteCode"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share invite code"))
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(TetherSurfaceElevated, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share invite",
                        tint = TetherTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: Member,
    isSelf: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TetherSurface, RoundedCornerShape(16.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MemberAvatar(
                name = member.displayName,
                avatarColorIndex = member.avatarColor,
                status = member.status,
                size = 42.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.displayName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isSelf) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(You)",
                            color = TetherTeal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                val statusText = when (member.status) {
                    MemberStatus.INSIDE -> "Inside"
                    MemberStatus.OUTSIDE -> "Outside"
                    MemberStatus.UNKNOWN -> "Waiting for updates"
                }

                val statusColor = when (member.status) {
                    MemberStatus.INSIDE -> TetherTeal
                    MemberStatus.OUTSIDE -> TetherCoral
                    MemberStatus.UNKNOWN -> TextSecondary
                }

                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}