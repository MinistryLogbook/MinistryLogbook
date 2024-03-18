package app.ministrylogbook.ui.intro

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import app.ministrylogbook.ui.AppGraph

sealed class IntroGraph(private val rawRoute: String) {

    object Welcome : IntroGraph("welcome")

    object Setup : IntroGraph("setup")

    override fun toString() = route

    val route
        get() = "${AppGraph.Intro}/$rawRoute"
}

fun NavGraphBuilder.introGraph() {
    navigation(route = AppGraph.Intro.route, startDestination = IntroGraph.Welcome.route) {
        composable(IntroGraph.Welcome.route) {
            WelcomePage()
        }

        composable(IntroGraph.Setup.route) {
            SetupPage()
        }
    }
}

fun NavController.navigateToSetup() = navigate(IntroGraph.Setup.route) {
    popUpTo(IntroGraph.Welcome.route)
}
