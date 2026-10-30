package com.tether.app.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.tether.app.data.local.UserPreferences
import com.tether.app.data.model.MemberStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Captures geofence transitions from Google Play Services.
 * Runs in the background and writes transition events directly to Firestore.
 */
class GeofenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "GeofencingEvent error: $errorMessage")
            return
        }

        val transition = geofencingEvent.geofenceTransition
        val isExit = transition == Geofence.GEOFENCE_TRANSITION_EXIT
        val isEnter = transition == Geofence.GEOFENCE_TRANSITION_ENTER

        if (!isExit && !isEnter) return

        val triggeringGeofences = geofencingEvent.triggeringGeofences.orEmpty()
        if (triggeringGeofences.isEmpty()) return

        val userPrefs = UserPreferences(context)
        val uid = userPrefs.userId
        val displayName = userPrefs.displayName.ifBlank { "Member" }

        if (uid.isNullOrBlank()) {
            Log.w(TAG, "No cached UID found; ignoring geofence transition")
            return
        }

        val eventType = if (isExit) "EXIT" else "ENTER"
        val newStatus = if (isExit) MemberStatus.OUTSIDE else MemberStatus.INSIDE

        // goAsync keeps the BroadcastReceiver process alive during the async Firestore write
        val pendingResult = goAsync()
        val firestore = FirebaseFirestore.getInstance()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                withTimeoutOrNull(12_000L) {
                    for (geofence in triggeringGeofences) {
                        val circleId = geofence.requestId // RequestId is the circleId
                        val nowMillis = System.currentTimeMillis()
                        val eventId = "${uid}_${nowMillis}_$eventType"

                        val circleRef = firestore.collection("circles").document(circleId)
                        val eventRef = circleRef.collection("events").document(eventId)
                        val memberRef = circleRef.collection("members").document(uid)

                        val eventData = hashMapOf(
                            "userId" to uid,
                            "userName" to displayName,
                            "type" to eventType,
                            "occurredAt" to FieldValue.serverTimestamp()
                        )

                        val memberUpdateData = hashMapOf(
                            "status" to newStatus.name,
                            "statusUpdatedAt" to FieldValue.serverTimestamp()
                        )

                        // Write event record and update member live status in one atomic batch
                        val batch = firestore.batch()
                        batch.set(eventRef, eventData)
                        batch.update(memberRef, memberUpdateData as Map<String, Any>)
                        batch.commit().await()

                        Log.i(TAG, "Successfully recorded $eventType for circle $circleId")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write geofence transition to Firestore", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
    }
}