package com.tether.app.ui.permissions

data class PermissionState(
    val hasFineLocation: Boolean = false,
    val hasCoarseLocation: Boolean = false,
    val hasBackgroundLocation: Boolean = false,
    val hasNotificationPermission: Boolean = false
) {
    // PRD F2: Continue is enabled once location (foreground) is granted
    val canProceed: Boolean get() = hasFineLocation || hasCoarseLocation
    val isApproximateOnly: Boolean get() = hasCoarseLocation && !hasFineLocation
}