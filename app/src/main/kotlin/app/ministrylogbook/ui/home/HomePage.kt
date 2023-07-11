package app.ministrylogbook.ui.home

import android.content.Intent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.ministrylogbook.MainActivity
import app.ministrylogbook.shared.layouts.ToolbarLayout
import app.ministrylogbook.shared.utilities.restartApp
import app.ministrylogbook.ui.home.backup.BackupImportDialog
import app.ministrylogbook.ui.home.viewmodel.HomeIntent
import app.ministrylogbook.ui.home.viewmodel.HomeState
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun HomePage(state: HomeState, dispatch: (intent: HomeIntent) -> Unit = {}) {
    val navController = rememberNavController()
    var scrollPosition by remember { mutableIntStateOf(0) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    var selectedMonth by remember(navBackStackEntry) {
        val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val arguments = navBackStackEntry?.arguments
        val year = arguments?.getString("year")?.toInt() ?: currentDate.year
        val monthNumber = arguments?.getString("monthNumber")?.toInt() ?: currentDate.monthNumber
        return@remember mutableStateOf(LocalDate(year, monthNumber, 1))
    }
    val context = LocalContext.current

    LaunchedEffect(state.importFinished) {
        if (state.importFinished) {
            context.restartApp(Intent(context, MainActivity::class.java))
        }
    }

    if (state.selectedBackupFile != null && state.isBackupValid) {
        BackupImportDialog(
            state.selectedBackupFile,
            state.latestEntry,
            onImport = {
                dispatch(HomeIntent.ImportBackup)
            },
            onDismiss = {
                dispatch(HomeIntent.DismissImportBackup)
            }
        )
    }

    ToolbarLayout(elevation = scrollPosition > 0, toolbarContent = {
        ToolbarMonthSelect(selectedMonth = selectedMonth, onSelect = {
            selectedMonth = it
            navController.navigateToMonth(it.year, it.monthNumber)
        })
        Spacer(Modifier.weight(1f))
        ToolbarActions(state.name, selectedMonth)
    }) {
        HomeNavHost(
            navController = navController,
            onScroll = { scrollPosition = it }
        )
    }
}
