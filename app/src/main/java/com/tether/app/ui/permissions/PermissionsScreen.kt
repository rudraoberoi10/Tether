package com.tether.app.ui.permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tether.app.ui.components.PrimaryButton
import com.tether.app.ui.theme.BorderElevated
import com.tether.app.ui.theme.BorderSubtle
import com.tether.app.ui.theme.TetherBg
import com.tether.app.ui.theme.TetherCoral
import com.tether.app.ui.theme.TetherSuccess
import com.tether.app.ui.theme.TetherSurface
import com.tether.app.ui.theme.TetherSurfaceElevated
import com.tether.app.ui.theme.TetherTeal
import com.tether.app.ui.theme.TextPrimary
import com.tether.app.ui.theme.TextSecondary

@Composable
fun PermissionsScreen(
    onContinue: () -> Unit,
    viewModel: PermissionsViewModel = viewModel(
        factory = PermissionsViewModel.provideFactory(LocalContext.current)
    )
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Re-check permissions automatically when returning from Settings
    LifecycleResumeEffect(Unit) {
        viewModel.refreshPermissions()
        onPauseOrDispose { }
    }

    // Launchers for runtime permission requests
    val foregroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshPermissions()
    }

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshPermissions()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshPermissions()
    }

    Scaffold(
        containerColor = TetherBg,
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp)) {
                PrimaryButton(
                    text = "Continue",
                    onClick = onContinue,
                    enabled = state.canProceed
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Permissions",
                style = MaterialTheme.typography.displayLarge,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tether needs a few device permissions to monitor fences reliably in the background.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step 1: Location Access (Foreground)
            PermissionCard(
                icon = Icons.Default.LocationOn,
                title = "Location Access",
                description = "Needed to detect your position relative to your circles.",
                isGranted = state.hasFineLocation,
                buttonText = if (state.isApproximateOnly) "Switch to Precise" else "Allow Location",
                onAction = {
                    foregroundLocationLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            )

            if (state.isApproximateOnly) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Switch to Precise so exits are detected accurately.",
                    color = TetherCoral,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2: Background Location
            PermissionCard(
                icon = Icons.Default.Timeline,
                title = "Background Location",
                description = "Allows Tether to detect boundary exits even when the app is closed.",
                isGranted = state.hasBackgroundLocation,
                isEnabled = state.canProceed,
                buttonText = "Allow All the Time",
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        // On Android 11+, redirect to app settings page
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                        backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 3: Notifications (API 33+)
            PermissionCard(
                icon = Icons.Default.Notifications,
                title = "Exit Notifications",
                description = "Alerts you immediately when someone in your circle leaves.",
                isGranted = state.hasNotificationPermission,
                buttonText = "Allow Notifications",
                onAction = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy Pledge Card
            PrivacyPledgeCard()
        }
    }
}

@Composable
private fun PermissionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    buttonText: String,
    onAction: () -> Unit,
    isEnabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TetherSurface, RoundedCornerShape(24.dp))
            .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(TetherSurfaceElevated, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) TetherSuccess else TetherTeal,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = description,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isGranted) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TetherSurfaceElevated, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = TetherSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Granted",
                        color = TetherSuccess,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onAction,
                    enabled = isEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TetherSurfaceElevated,
                        contentColor = TetherTeal,
                        disabledContainerColor = TetherSurfaceElevated.copy(alpha = 0.5f),
                        disabledContentColor = TextSecondary.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = buttonText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyPledgeCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TetherSurfaceElevated, RoundedCornerShape(24.dp))
            .border(1.dp, BorderElevated, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = TetherTeal,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Tether Privacy Pledge",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tether only checks whether you are inside or outside the fence. It never records your route.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}