package com.github.danieldaeschle.ministrynotes.ui.home.historysection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.data.Entry
import com.github.danieldaeschle.ministrynotes.ui.LocalAppNavController
import com.github.danieldaeschle.ministrynotes.ui.home.HomeGraph
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.HomeViewModel
import kotlinx.datetime.toJavaLocalDate
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter

@Composable
fun HistorySection(homeViewModel: HomeViewModel = koinViewModel()) {
    val navController = LocalAppNavController.current
    val entries = homeViewModel.entries.collectAsState()
    val orderedEntries = entries.value.sortedBy { it.datetime }.reversed()

    val handleClick: (entry: Entry) -> Unit = {
        navController.navigate(HomeGraph.EntryDetails.createRoute(it.id))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .navigationBarsPadding()
    ) {
        orderedEntries.forEach {
            HistoryItem(it, onClick = { handleClick(it) })
        }
    }
}

@Composable
fun HistoryItem(entry: Entry, onClick: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("E, dd. MMMM")
    val dateText = formatter.format(entry.datetime.toJavaLocalDate())

    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(dateText, modifier = Modifier.padding(bottom = 4.dp))
        Row {
            if (entry.hours > 0 || entry.minutes > 0) {
                val minutes =
                    if (entry.minutes > 0) ":${entry.minutes.toString().padStart(2, '0')}" else ""
                HistoryItemChip(
                    icon = painterResource(R.drawable.ic_schedule),
                    text = "${entry.hours}${minutes} hrs"
                )
                Spacer(Modifier.width(8.dp))
            }
            if (entry.placements > 0) {
                HistoryItemChip(
                    icon = painterResource(R.drawable.ic_article),
                    text = entry.placements.toString()
                )
                Spacer(Modifier.width(8.dp))
            }
            if (entry.returnVisits > 0) {
                HistoryItemChip(
                    icon = painterResource(R.drawable.ic_group),
                    text = entry.returnVisits.toString()
                )
                Spacer(Modifier.width(8.dp))
            }
            if (entry.videoShowings > 0) {
                HistoryItemChip(
                    icon = painterResource(R.drawable.ic_play_circle),
                    text = entry.videoShowings.toString()
                )
                Spacer(Modifier.width(8.dp))
            }
            if (entry.creditHours > 0 || entry.creditMinutes > 0) {
                val minutes =
                    if (entry.creditMinutes > 0) ":${entry.creditMinutes.toString().padStart(2, '0')}" else ""
                HistoryItemChip(
                    icon = painterResource(R.drawable.ic_volunteer_activism),
                    text = "${entry.creditHours}${minutes} hrs"
                )
            }
        }
    }
}

@Composable
fun HistoryItemChip(icon: Painter? = null, text: String) {
    val color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
    Row(
        modifier = Modifier
            .clip(shape = RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(0.1f))
            .padding(horizontal = 2.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = color)
        }
        Text(
            text,
            style = TextStyle(color = color),
            modifier = Modifier.padding(start = 4.dp, end = 4.dp)
        )
    }
}