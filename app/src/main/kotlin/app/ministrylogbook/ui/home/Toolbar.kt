package app.ministrylogbook.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ministrylogbook.R
import app.ministrylogbook.shared.layouts.MonthPickerPopup
import app.ministrylogbook.shared.utilities.getLocale
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.share.navigateToShare
import app.ministrylogbook.ui.shared.ToolbarAction
import java.time.format.TextStyle
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun ToolbarActions(name: String, month: LocalDate) {
    val navController = LocalAppNavController.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToolbarAction(onClick = {
            navController.navigateToShare(month.year, month.monthNumber)
        }) {
            Icon(
                painterResource(R.drawable.ic_share),
                contentDescription = stringResource(R.string.share_field_service_report)
            )
        }
        ProfileButton(name)
    }
}

@Composable
fun ToolbarMonthSelect(
    selectedMonth: LocalDate,
    onSelect: (newDate: LocalDate) -> Unit = {}
) {
    val navController = LocalAppNavController.current
    val locale = getLocale()
    val monthTitle by remember(selectedMonth) {
        derivedStateOf {
            val monthName = selectedMonth.month.getDisplayName(TextStyle.FULL, locale)
            val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
            if (selectedMonth.year != currentYear) "$monthName ${selectedMonth.year}" else monthName
        }
    }
    val currentBackStackEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(null)
    var expanded by remember { mutableStateOf(false) }
    val isHomeScreen by remember(currentBackStackEntry) {
        derivedStateOf {
            currentBackStackEntry?.destination?.route == HomeGraph.Root.route
        }
    }

    LaunchedEffect(isHomeScreen, expanded) {
        if (!isHomeScreen && expanded) {
            expanded = false
        }
    }

    Box {
        Row(
            modifier = Modifier
                .height(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary.copy(0.2f))
                .clickable { expanded = true }
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(monthTitle, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Rounded.ArrowDropDown,
                contentDescription = "Dropdown Arrow for month selection", // TODO: translation
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        MonthPickerPopup(
            expanded = expanded && isHomeScreen,
            selectedMonth = selectedMonth,
            onDismissRequest = {
                expanded = !expanded
            },
            onSelect = { month ->
                expanded = false
                onSelect(month)
            }
        )
    }
}
