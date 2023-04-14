package com.github.danieldaeschle.ministrynotes.ui.settings

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavGraphBuilder
import com.github.danieldaeschle.ministrynotes.ui.AppGraph
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation

sealed class SettingsGraph(val route: String) {
    object Root : SettingsGraph("/")

    object Name : SettingsGraph("/name")

    object Goal : SettingsGraph("/goal")
}

@OptIn(ExperimentalAnimationApi::class)
fun NavGraphBuilder.settingsGraph() {
    navigation(
        route = AppGraph.Settings.route,
        startDestination = SettingsGraph.Root.route,
        enterTransition = {
            slideInHorizontally(tween(200)) { it / 8 } + fadeIn(tween(100))
        },
        exitTransition = {
            slideOutHorizontally(tween(200)) { it / 3 } + fadeOut(tween(100))
        },
        popEnterTransition = {
            slideInHorizontally(tween(200)) { -it / 8 } + fadeIn(tween(100))
        },
        popExitTransition = {
            slideOutHorizontally(tween(200)) { -it / 3 } + fadeOut(tween(100))
        },
    ) {
        composable(SettingsGraph.Root.route) {
            SettingsPage()
        }

        composable(SettingsGraph.Name.route) {
            NamePage()
        }

        composable(SettingsGraph.Goal.route) {
            GoalPage()
        }
    }
}
