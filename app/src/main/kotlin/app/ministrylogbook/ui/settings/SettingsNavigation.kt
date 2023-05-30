package app.ministrylogbook.ui.settings

import androidx.navigation.NavHostController
import app.ministrylogbook.ui.home.HomeGraph

fun NavHostController.navigateToSettings() = navigate(SettingsGraph.Root.route) {
    popUpTo(HomeGraph.Root.route)
}

fun NavHostController.navigateToSettingsName() = navigate(SettingsGraph.Name.route) {
    popUpTo(SettingsGraph.Root.route)
}

fun NavHostController.navigateToSettingsGoal() = navigate(SettingsGraph.Goal.route) {
    popUpTo(SettingsGraph.Root.route)
}
