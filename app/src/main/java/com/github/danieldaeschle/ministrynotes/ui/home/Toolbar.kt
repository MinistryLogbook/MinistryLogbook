package com.github.danieldaeschle.ministrynotes.ui.home

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.ui.LocalAppNavController
import com.github.danieldaeschle.ministrynotes.ui.home.share.ShareDialog
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.HomeViewModel
import com.github.danieldaeschle.ministrynotes.ui.shared.ToolbarAction
import org.koin.androidx.compose.koinViewModel

@Composable
fun ToolbarActions() {
    var isShareDialogOpen by remember { mutableStateOf(false) }

    ShareDialog(
        isOpen = isShareDialogOpen,
        onClose = { isShareDialogOpen = false },
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToolbarAction(onClick = {
            isShareDialogOpen = true
        }) {
            Icon(
                painterResource(R.drawable.ic_share),
                contentDescription = "Share",
            )
        }
        ProfileButton()
    }
}

@Composable
fun ToolbarMonthSelect(homeViewModel: HomeViewModel = koinViewModel()) {
    val expanded = remember { mutableStateOf(false) }
    val navController = LocalAppNavController.current
    val selectedMonth by homeViewModel.selectedMonth.collectAsState()
    val monthTitle by homeViewModel.monthTitle.collectAsState("")

    Box {
        Row(
            modifier = Modifier
                .height(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary.copy(0.15f))
                .clickable { expanded.value = true }
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(monthTitle, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Rounded.ArrowDropDown,
                contentDescription = "Dropdown Arrow for Month selection",
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
        MonthPickerPopup(
            expanded = expanded.value,
            selectedMonth = selectedMonth,
            onDismissRequest = {
                expanded.value = !expanded.value
            },
            onSelectMonth = { month ->
                expanded.value = false
                navController.navigate(HomeGraph.Root.createRoute(month.year, month.monthNumber))
            },
        )
    }
}
