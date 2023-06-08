package app.ministrylogbook.ui.home

import androidx.compose.animation.ExperimentalAnimationApi
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
import app.ministrylogbook.ui.shared.Toolbar
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomePage() {
    val navController = rememberAnimatedNavController()
    var scrollPosition by remember { mutableIntStateOf(0) }
    var selectedMonth by remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }

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
