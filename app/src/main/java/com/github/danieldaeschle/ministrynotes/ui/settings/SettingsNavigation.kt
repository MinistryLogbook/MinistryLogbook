package com.github.danieldaeschle.ministrynotes.ui.settings

import androidx.navigation.NavHostController
import com.github.danieldaeschle.ministrynotes.ui.home.HomeGraph

fun NavHostController.navigateToSettings() = navigate(SettingsGraph.Root.route) {
    popUpTo(HomeGraph.Root.route)
}

fun NavHostController.navigateToSettingsName() = navigate(SettingsGraph.Name.route)

fun NavHostController.navigateToSettingsGoal() = navigate(SettingsGraph.Goal.route)
