package com.tether.app.geofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.tether.app.data.local.UserPreferences
import com.tether.app.data.model.Circle
import com.tether.app.data.model.MemberStatus
import kotlinx.coroutines.tasks.await

class GeofenceManager(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val userPrefs: UserPreferences
) {
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceReceiver::class.java)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Re-registers geofences for all circles the user currently belongs to.
     */
    @SuppressLint("MissingPermission")
    suspend fun syncFences(circles: List<Circle>) {
        if (!hasLocationPermission()) {
            Log.w(TAG, "Cannot sync geofences: fine location permission not granted")
            return
        }

        try {
            runCatching {
                geofencingClient.removeGeofences(geofencePendingIntent).await()
            }

            if (circles.isEmpty()) return

            val geofences = circles.map { circle ->
                Geofence.Builder()
                    .setRequestId(circle.id)
                    .setCircularRegion(
                        circle.fenceLat,
                        circle.fenceLng,
                        circle.fenceRadiusM.coerceAtLeast(100.0).toFloat()
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
            }

            val request = GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofences)
                .build()

            geofencingClient.addGeofences(request, geofencePendingIntent).await()
            Log.i(TAG, "Successfully registered ${geofences.size} geofences")

            for (circle in circles) {
                computeInitialStatus(circle)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register geofences with Play Services", e)
        }
    }

    /**
     * Obtains location fix and checks if member transitioned inside or outside.
     * If transitioning from INSIDE to OUTSIDE, records an EXIT event for alerts.
     */
    @SuppressLint("MissingPermission")
    suspend fun computeInitialStatus(circle: Circle) {
        if (!hasLocationPermission()) return
        val uid = userPrefs.userId ?: return

        try {
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
            ).await() ?: fusedLocationClient.lastLocation.await()

            if (location != null) {
                val distanceResults = FloatArray(1)
                Location.distanceBetween(
                    circle.fenceLat,
                    circle.fenceLng,
                    location.latitude,
                    location.longitude,
                    distanceResults
                )
                val distanceM = distanceResults[0]
                val newStatus = if (distanceM <= circle.fenceRadiusM) {
                    MemberStatus.INSIDE
                } else {
                    MemberStatus.OUTSIDE
                }

                val memberRef = firestore.collection("circles")
                    .document(circle.id)
                    .collection("members")
                    .document(uid)

                val snapshot = memberRef.get().await()
                val oldStatusStr = snapshot.getString("status") ?: MemberStatus.UNKNOWN.name

                // 1. Update live member status
                memberRef.update(
                    mapOf(
                        "status" to newStatus.name,
                        "statusUpdatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()

                Log.i(TAG, "Status updated to ${newStatus.name} ($distanceM m from center)")

                // 2. If transitioning from INSIDE to OUTSIDE, write the EXIT event immediately
                if (oldStatusStr == MemberStatus.INSIDE.name && newStatus == MemberStatus.OUTSIDE) {
                    val eventId = "${uid}_${System.currentTimeMillis()}_EXIT"
                    val eventData = hashMapOf(
                        "userId" to uid,
                        "userName" to userPrefs.displayName.ifBlank { "Member" },
                        "type" to "EXIT",
                        "occurredAt" to FieldValue.serverTimestamp()
                    )

                    firestore.collection("circles")
                        .document(circle.id)
                        .collection("events")
                        .document(eventId)
                        .set(eventData)
                        .await()

                    Log.i(TAG, "WROTE EXIT EVENT: $eventId for ${userPrefs.displayName}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error checking location status", e)
        }
    }

    companion object {
        private const val TAG = "GeofenceManager"
    }
}