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
import app.ministrylogbook.R
import app.ministrylogbook.lib.getLocale
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.home.viewmodel.HomeViewModel
import app.ministrylogbook.ui.share.navigateToShare
import app.ministrylogbook.ui.shared.ToolbarAction
import org.koin.androidx.compose.koinViewModel

@Composable
fun ToolbarActions(viewModel: HomeViewModel = koinViewModel()) {
    val navController = LocalAppNavController.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToolbarAction(onClick = {
            navController.navigateToShare(viewModel.month.year, viewModel.month.monthNumber)
        }) {
            Icon(
                painterResource(R.drawable.ic_share),
                contentDescription = stringResource(R.string.share_field_service_report)
            )
        }
        ProfileButton()
    }
}

@Composable
fun ToolbarMonthSelect(viewModel: HomeViewModel = koinViewModel()) {
    val navController = LocalAppNavController.current
    var expanded by remember { mutableStateOf(false) }
    val locale = getLocale()

    Box {
        Row(
            modifier = Modifier
                .height(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary.copy(0.15f))
                .clickable { expanded = true }
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(viewModel.getMonthTitle(locale), color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Rounded.ArrowDropDown,
                contentDescription = "Dropdown Arrow for month selection", // TODO: translation
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        MonthPickerPopup(
            expanded = expanded,
            selectedMonth = viewModel.month,
            onDismissRequest = {
                expanded = !expanded
            },
            onSelectMonth = { month ->
                expanded = false
                // TODO: wait for animation to finish
                navController.navigateToMonth(month.year, month.monthNumber)
            }
        )
    }
}
