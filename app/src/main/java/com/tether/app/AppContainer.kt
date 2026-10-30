package com.tether.app

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.tether.app.alerts.ExitAlertWatcher
import com.tether.app.alerts.NotificationHelper
import com.tether.app.data.local.UserPreferences
import com.tether.app.data.repository.TetherRepository
import com.tether.app.geofence.GeofenceManager

class AppContainer(private val context: Context) {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val userPreferences: UserPreferences by lazy { UserPreferences(context) }

    val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(context)
    }

    val exitAlertWatcher: ExitAlertWatcher by lazy {
        ExitAlertWatcher(firestore, auth, userPreferences, notificationHelper)
    }

    val geofenceManager: GeofenceManager by lazy {
        GeofenceManager(context, firestore, userPreferences)
    }

    val repository: TetherRepository by lazy {
        TetherRepository(auth, firestore, userPreferences)
    }

    val appContext: Context get() = context
}