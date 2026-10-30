package com.tether.app.alerts

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.tether.app.data.local.UserPreferences
import com.tether.app.data.model.Circle
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * Listens in real time to events in all circles the user belongs to.
 * When another member generates an EXIT event, shows a local notification.
 */
class ExitAlertWatcher(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userPrefs: UserPreferences,
    private val notificationHelper: NotificationHelper
) {
    private val activeCircleListeners = ConcurrentHashMap<String, ListenerRegistration>()
    private val processedEventIds = Collections.synchronizedSet(HashSet<String>())
    private var circlesListener: ListenerRegistration? = null

    fun start() {
        stop()

        val uid = auth.currentUser?.uid ?: userPrefs.userId
        if (uid.isNullOrBlank()) {
            auth.addAuthStateListener { firebaseAuth ->
                if (firebaseAuth.currentUser != null) {
                    start()
                }
            }
            return
        }

        circlesListener = firestore.collection("circles")
            .whereArrayContains("memberIds", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    Log.e(TAG, "Error observing circles for alerts", error)
                    return@addSnapshotListener
                }

                val currentCircles = snapshot.documents.map { doc ->
                    Circle.fromMap(doc.id, doc.data ?: emptyMap())
                }
                val currentCircleIds = currentCircles.map { it.id }.toSet()

                // Remove stale listeners
                val iterator = activeCircleListeners.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    if (!currentCircleIds.contains(entry.key)) {
                        entry.value.remove()
                        iterator.remove()
                    }
                }

                // Add listeners for active circles
                for (circle in currentCircles) {
                    if (!activeCircleListeners.containsKey(circle.id)) {
                        attachEventListener(circle, uid)
                    }
                }
            }
    }

    private fun attachEventListener(circle: Circle, currentUserId: String) {
        var isInitialSnapshot = true

        val listener = firestore.collection("circles")
            .document(circle.id)
            .collection("events")
            .whereEqualTo("type", "EXIT")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                // On first load, record existing events without re-notifying historical ones
                if (isInitialSnapshot) {
                    isInitialSnapshot = false
                    for (doc in snapshot.documents) {
                        processedEventIds.add(doc.id)
                    }
                    return@addSnapshotListener
                }

                // For subsequent snapshot emissions, notify only for newly added events
                for (change in snapshot.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        val doc = change.document
                        val eventId = doc.id
                        val userId = doc.getString("userId").orEmpty()
                        val userName = doc.getString("userName") ?: "A member"

                        // Notify only if the exit was performed by someone else
                        if (userId != currentUserId && !processedEventIds.contains(eventId)) {
                            processedEventIds.add(eventId)
                            Log.i(TAG, "TRIGGERING NOTIFICATION: $userName left ${circle.name}")
                            notificationHelper.showExitNotification(
                                circleId = circle.id,
                                circleName = circle.name,
                                userName = userName,
                                fenceLabel = circle.fenceLabel,
                                eventId = eventId
                            )
                        }
                    }
                }
            }

        activeCircleListeners[circle.id] = listener
    }

    fun stop() {
        circlesListener?.remove()
        circlesListener = null
        activeCircleListeners.values.forEach { it.remove() }
        activeCircleListeners.clear()
    }

    companion object {
        private const val TAG = "ExitAlertWatcher"
    }
}