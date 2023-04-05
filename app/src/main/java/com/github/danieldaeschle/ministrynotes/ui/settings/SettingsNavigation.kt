package com.github.danieldaeschle.ministrynotes.ui.settings

import androidx.navigation.NavHostController

fun NavHostController.navigateToSettings() = navigate(SettingsGraph.Root.route)

fun NavHostController.navigateToSettingsName() = navigate(SettingsGraph.Name.route)
