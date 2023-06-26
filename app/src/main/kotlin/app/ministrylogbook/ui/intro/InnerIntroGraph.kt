package app.ministrylogbook.ui.intro

import androidx.navigation.NavHostController

sealed class InnerIntroGraph(val route: String) {

    object Name : InnerIntroGraph("name")

    object Role : InnerIntroGraph("role")

    object Goal : InnerIntroGraph("goal")

    object Reminders : InnerIntroGraph("reminders")

    override fun toString() = route
}

fun NavHostController.navigateToSetupName() = navigate(InnerIntroGraph.Name.route)

fun NavHostController.navigateToSetupRole() = navigate(InnerIntroGraph.Role.route)

fun NavHostController.navigateToSetupReminders() = navigate(InnerIntroGraph.Reminders.route)
