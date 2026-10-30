package com.tether.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tether.app.TetherApp
import com.tether.app.ui.create.CreateCircleScreen
import com.tether.app.ui.detail.CircleDetailScreen
import com.tether.app.ui.home.HomeScreen
import com.tether.app.ui.join.JoinCircleScreen
import com.tether.app.ui.name.NameScreen
import com.tether.app.ui.permissions.PermissionsScreen

@Composable
fun TetherNavHost(
    navController: NavHostController
) {
    val app = LocalContext.current.applicationContext as TetherApp
    val isRegistered = app.container.userPreferences.isProfileCompleted && app.container.auth.currentUser != null
    val startDestination = if (isRegistered) Routes.HOME else Routes.PERMISSIONS

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.PERMISSIONS) {
            PermissionsScreen(
                onContinue = { navController.navigate(Routes.NAME) }
            )
        }

        composable(Routes.NAME) {
            NameScreen(
                onSetupComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PERMISSIONS) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onCreateCircleClick = { navController.navigate(Routes.CREATE_CIRCLE) },
                onJoinCircleClick = { navController.navigate(Routes.JOIN_CIRCLE) },
                onCircleClick = { circleId -> navController.navigate(Routes.circleDetail(circleId)) }
            )
        }

        composable(Routes.CREATE_CIRCLE) {
            CreateCircleScreen(
                onBackClick = { navController.popBackStack() },
                onCircleCreated = { circleId ->
                    navController.navigate(Routes.circleDetail(circleId)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(Routes.JOIN_CIRCLE) {
            JoinCircleScreen(
                onBackClick = { navController.popBackStack() },
                onJoined = { circleId ->
                    navController.navigate(Routes.circleDetail(circleId)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(
            route = Routes.CIRCLE_DETAIL,
            arguments = listOf(navArgument("circleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val circleId = backStackEntry.arguments?.getString("circleId").orEmpty()
            CircleDetailScreen(
                circleId = circleId,
                onBackClick = { navController.popBackStack() },
                onLeaveSuccess = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                }
            )
        }
    }
}