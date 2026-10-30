package com.tether.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.tether.app.alerts.NotificationHelper
import com.tether.app.ui.navigation.Routes
import com.tether.app.ui.navigation.TetherNavHost
import com.tether.app.ui.theme.TetherTheme

class MainActivity : ComponentActivity() {

    private var pendingCircleId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle cold start from notification tap
        intent?.getStringExtra(NotificationHelper.EXTRA_CIRCLE_ID)?.let { id ->
            pendingCircleId = id
        }

        setContent {
            TetherTheme {
                val navController = rememberNavController()

                // Route into circle detail if opened via notification
                LaunchedEffect(pendingCircleId) {
                    val circleId = pendingCircleId
                    if (!circleId.isNullOrBlank()) {
                        navController.navigate(Routes.circleDetail(circleId))
                        pendingCircleId = null
                    }
                }

                TetherNavHost(navController = navController)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Handle warm start from notification tap when app is already in memory
        intent.getStringExtra(NotificationHelper.EXTRA_CIRCLE_ID)?.let { id ->
            pendingCircleId = id
        }
    }
}