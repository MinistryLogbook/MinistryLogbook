package app.ministrylogbook.ui.intro

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import app.ministrylogbook.ui.AppGraph
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation

sealed class IntroGraph(private val rawRoute: String) {

    object Welcome : IntroGraph("welcome")

    object Setup : IntroGraph("setup")

    override fun toString() = route

    val route
        get() = "${AppGraph.Intro}/$rawRoute"
}

@OptIn(ExperimentalAnimationApi::class)
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
