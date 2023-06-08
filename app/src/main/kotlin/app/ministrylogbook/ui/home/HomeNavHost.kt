package app.ministrylogbook.ui.home

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import app.ministrylogbook.shared.stayOut
import app.ministrylogbook.ui.SlideOutTransitionMillis
import app.ministrylogbook.ui.home.overview.OverviewPage
import app.ministrylogbook.ui.home.viewmodel.OverviewViewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeNavHost(
    modifier: Modifier = Modifier,
    navController: HomeNavHostController,
    startDestination: String = InnerHomeGraph.Overview.route,
    onScroll: (Int) -> Unit = {}
) {
    CompositionLocalProvider(LocalHomeNavController provides navController) {
        AnimatedNavHost(
            modifier = modifier,
            navController = navController,
            startDestination = startDestination,
            enterTransition = { EnterTransition.None },
            exitTransition = { stayOut(SlideOutTransitionMillis) }
        ) {
            composable(
                InnerHomeGraph.Overview.route,
                arguments = InnerHomeGraph.Overview.arguments
            ) {
                val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val yearArgument = it.arguments?.getString("year")
                val year = yearArgument?.toInt() ?: currentDate.year
                val monthNumberArgument = it.arguments?.getString("monthNumber")
                val monthNumber = monthNumberArgument?.toInt() ?: currentDate.monthNumber
                val month = LocalDate(year, monthNumber, 1)

                val viewModel = getViewModel<OverviewViewModel>(parameters = { parametersOf(month) })
                OverviewPage(onScroll = onScroll, viewModel = viewModel)
            }
        }
    }
}

typealias HomeNavHostController = NavHostController

val LocalHomeNavController =
    compositionLocalOf<HomeNavHostController> { error("HomeNavHostController error") }
