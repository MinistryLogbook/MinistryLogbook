package app.ministrylogbook.ui.home

import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.ministrylogbook.ui.shared.Toolbar
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun HomePage() {
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

    Surface {
        Toolbar(
            modifier = Modifier.zIndex(1f),
            elevation = if (scrollPosition > 0) 4.dp else 0.dp
        ) {
            ToolbarMonthSelect(selectedMonth = selectedMonth, onSelect = {
                selectedMonth = it
                navController.navigateToMonth(it.year, it.monthNumber)
            })
            Spacer(Modifier.weight(1f))
            ToolbarActions(selectedMonth)
        }

        HomeNavHost(
            navController = navController,
            onScroll = {
                scrollPosition = it
            }
        )
    }
}
