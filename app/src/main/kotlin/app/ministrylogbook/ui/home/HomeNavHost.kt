package app.ministrylogbook.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import app.ministrylogbook.ui.home.overview.OverviewPage
import app.ministrylogbook.ui.home.viewmodel.OverviewViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun HomeNavHost(
    modifier: Modifier = Modifier,
    navController: HomeNavHostController,
    startDestination: String = InnerHomeGraph.Overview.route,
    onScroll: (Int) -> Unit = {}
) {
    CompositionLocalProvider(LocalHomeNavController provides navController) {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = startDestination
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
