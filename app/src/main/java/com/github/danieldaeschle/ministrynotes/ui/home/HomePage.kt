package com.github.danieldaeschle.ministrynotes.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.lib.ExtendableFloatingActionButton
import com.github.danieldaeschle.ministrynotes.lib.bottomSheet
import com.github.danieldaeschle.ministrynotes.ui.AppGraph
import com.github.danieldaeschle.ministrynotes.ui.LocalAppNavController
import com.github.danieldaeschle.ministrynotes.ui.home.detailssection.DetailsSection
import com.github.danieldaeschle.ministrynotes.ui.home.detailssection.ShareDialog
import com.github.danieldaeschle.ministrynotes.ui.home.historysection.HistorySection
import com.github.danieldaeschle.ministrynotes.ui.home.recorddetails.EntryDetailsBottomSheetContent
import com.github.danieldaeschle.ministrynotes.ui.home.recorddetails.StudiesBottomSheetContent
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.HomeViewModel
import com.github.danieldaeschle.ministrynotes.ui.shared.Toolbar
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel
import java.time.format.TextStyle
import java.util.Locale

sealed class HomeGraph(val route: String, val arguments: List<NamedNavArgument> = listOf()) {
    object Root : HomeGraph(
        route = "root?year={year}&monthNumber={monthNumber}", arguments = listOf(
            navArgument("year") { nullable = true; defaultValue = null },
            navArgument("monthNumber") { nullable = true; defaultValue = null },
        )
    ) {
        fun createRoute(year: Int, monthNumber: Int): String {
            return "root?year=${year}&monthNumber=${monthNumber}"
        }
    }

    object Studies : HomeGraph(
        route = "home/{year}/{monthNumber}/studies", arguments = listOf(
            navArgument("year") { type = NavType.IntType },
            navArgument("monthNumber") { type = NavType.IntType },
        )
    ) {
        fun createRoute(year: Int, monthNumber: Int): String {
            return "home/${year}/${monthNumber}/studies"
        }
    }

    object EntryDetails : HomeGraph(
        route = "home/entry-details/{id}", arguments = listOf(
            navArgument("id") { nullable = true; defaultValue = null },
        )
    ) {
        fun createRoute(id: Int? = null): String {
            if (id == null) {
                return "home/entry-details/new"
            }
            return "home/entry-details/$id"
        }
    }
}

fun NavGraphBuilder.homeGraph(navController: NavHostController) {
    navigation(route = AppGraph.Home.route, startDestination = HomeGraph.Root.route) {
        composable(HomeGraph.Root.route, arguments = HomeGraph.Root.arguments) {
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val year = it.arguments?.getString("year")?.toInt() ?: currentDate.year
            val monthNumber =
                it.arguments?.getString("monthNumber")?.toInt() ?: currentDate.monthNumber
            HomePage(year, monthNumber)
        }

        bottomSheet(HomeGraph.EntryDetails.route, arguments = HomeGraph.Root.arguments) {
            val id = it.arguments?.getString("id")?.let { value ->
                if (value == "new") {
                    null
                } else {
                    value.toInt()
                }
            }

            EntryDetailsBottomSheetContent(id)
        }

        bottomSheet(HomeGraph.Studies.route, arguments = HomeGraph.Root.arguments) {
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val year = it.arguments?.getString("year")?.toInt() ?: currentDate.year
            val monthNumber =
                it.arguments?.getString("monthNumber")?.toInt() ?: currentDate.monthNumber
            StudiesBottomSheetContent(year, monthNumber)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(year: Int, monthNumber: Int, homeViewModel: HomeViewModel = koinViewModel()) {
    var isShareDialogOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var fabExtended by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()
    val navController = LocalAppNavController.current
    val entries = homeViewModel.entries.collectAsState()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route
    val studies = homeViewModel.studies.collectAsState(0)

    LaunchedEffect(year, monthNumber, currentRoute) {
        if (currentRoute == HomeGraph.Root.route) {
            homeViewModel.load(year, monthNumber)
        }
    }

    LaunchedEffect(scrollState) {
        var prev = 0
        snapshotFlow { scrollState.value }.collect {
            fabExtended = it <= prev
            prev = it
        }
    }

    val handleShare = {
        isShareDialogOpen = false
        val hours = entries.value.sumOf { it.hours }
        val minutes = entries.value.sumOf { it.minutes }
        val allHours = hours + minutes / 60
        val placements = entries.value.sumOf { it.placements }
        val videoShowings = entries.value.sumOf { it.videoShowings }
        val returnVisits = entries.value.sumOf { it.returnVisits }
        val monthName = homeViewModel.selectedMonth.value.month.getDisplayName(
            TextStyle.FULL, Locale.ENGLISH
        )
        val text = """
            My field service report for the month: $monthName
            
            Hours: $allHours
            Placements: $placements
            Video showings: $videoShowings
            Return visits: $returnVisits
            Studies: ${studies.value}
        """.trimIndent()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        context.startActivity(shareIntent)
    }

    val handleCreate = {
        navController.navigate(HomeGraph.EntryDetails.createRoute())
    }

    ShareDialog(
        open = isShareDialogOpen, cancel = { isShareDialogOpen = false }, share = handleShare
    )

    Scaffold(floatingActionButton = {
        ExtendableFloatingActionButton(onClick = handleCreate, extended = fabExtended, icon = {
            Icon(painterResource(R.drawable.ic_add), contentDescription = null)
        }, text = {
            Text("Add to report")
        })
    }) {
        Box {
            Toolbar(
                modifier = Modifier.zIndex(1f),
                elevation = if (scrollState.value > 0) 4.dp else 0.dp,
            ) {
                ToolbarMonthSelect()

                Spacer(Modifier.weight(1f))

                val handleOpenSettings = {
                    navController.navigate(AppGraph.Settings.route)
                }
                ToolbarActions(onOpenSettings = handleOpenSettings)
            }
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(bottom = 90.dp)
            ) {
                Box(modifier = Modifier.statusBarsPadding()) {
                    Spacer(modifier = Modifier.height(56.dp))
                }

                DetailsSection()

                if (entries.value.isNotEmpty()) {
                    Divider(color = MaterialTheme.colorScheme.onBackground.copy(0.05f))
                }

                HistorySection()
            }
        }
    }
}
