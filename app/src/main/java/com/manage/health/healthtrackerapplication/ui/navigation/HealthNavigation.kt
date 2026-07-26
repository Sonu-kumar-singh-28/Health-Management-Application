package com.manage.health.healthtrackerapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.manage.health.healthtrackerapplication.data.service.NotificationService
import com.manage.health.healthtrackerapplication.ui.screens.AccessibilitySettingsScreen
import com.manage.health.healthtrackerapplication.ui.screens.ConnectionStatusScreen
import com.manage.health.healthtrackerapplication.ui.screens.GoalsScreen
import com.manage.health.healthtrackerapplication.ui.screens.GoogleFitSetupScreen
import com.manage.health.healthtrackerapplication.ui.screens.ModernDashBoardScreen
import com.manage.health.healthtrackerapplication.ui.screens.NotificationSettingsScreen
import com.manage.health.healthtrackerapplication.ui.screens.ReportsScreen
import com.manage.health.healthtrackerapplication.ui.screens.RunningMapScreen
import com.manage.health.healthtrackerapplication.ui.screens.SettingsScreen
import com.manage.health.healthtrackerapplication.ui.screens.SleepTrackingScreen
import com.manage.health.healthtrackerapplication.ui.screens.StepTrackingScreen
import com.manage.health.healthtrackerapplication.ui.screens.WaterTrackingScreen
import com.manage.health.healthtrackerapplication.ui.screens.WearableSetupScreen

@Composable
fun HealthNavigation(
    navHostController: NavHostController,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navHostController,
        startDestination = "dashboard",
        modifier = modifier
    ) {

        composable("dashboard") {
            ModernDashBoardScreen(onNavigateToWater = { navHostController.navigate("water") },
                onNavigateToSteps = { navHostController.navigate("steps") },
                onNavigateToSleep = { navHostController.navigate("sleep") },
                onNavigateToGoals = { navHostController.navigate("goals") },
                onNavigateToReports = { navHostController.navigate("reports") },
                onNavigateToConnectionStatus = { navHostController.navigate("connection_status") },
                onNavigateToRunningMap = { navHostController.navigate("running_map") },
                onSignOut = onSignOut
            )
        }
        composable("water"){
            WaterTrackingScreen()
        }

        composable("steps"){
            StepTrackingScreen()
        }

        composable("running_map"){
            RunningMapScreen(
                onNavigatedBack = {navHostController.popBackStack()}
            )
        }

        composable("sleep"){
            SleepTrackingScreen()
        }

        composable("reports"){
            ReportsScreen()
        }

        composable("settings"){
            SettingsScreen(
                onNavigateToNotifications = {navHostController.navigate("notifications")},
                onNavigateToGoals = {navHostController.navigate("goals")},
                onNavigateToAccessiblity = {navHostController.navigate("accessibility")}
            )
        }

        composable("goals"){
            GoalsScreen(
            onNavigateBack= {navHostController.popBackStack()}
            )
        }

        composable("accessibility") {
            AccessibilitySettingsScreen(
                onNavigateToNotifications = {
                    navHostController.navigate("notifications")
                },
                onNavigateBack = {
                    navHostController.popBackStack()
                }
            )
        }

        composable("notifications") {
            NotificationSettingsScreen(
                notificationService = NotificationService(
                    LocalContext.current
                ),
                onNavigateBack = {
                    navHostController.popBackStack()
                }
            )
        }

        composable("connection_status") {
            ConnectionStatusScreen(
                onNavigateBack = {
                    navHostController.popBackStack()
                },
                onNavigateToGoogleFitSetup = {
                    navHostController.navigate("google_fit_setup")
                },
                onNavigateToWearableSetup = {
                    navHostController.navigate("wearable_setup")
                }
            )
        }

        composable("google_fit_setup") {
            GoogleFitSetupScreen(
                onNavigateBack = {
                    navHostController.popBackStack()
                },
                onSetupComplete = {
                    navHostController.popBackStack()
                }
            )
        }

        composable("wearable_setup") {
            WearableSetupScreen(
                onNavigateBack = {
                    navHostController.popBackStack()
                },
                onSetupComplete = {
                    navHostController.popBackStack()
                }
            )
        }
    }
}