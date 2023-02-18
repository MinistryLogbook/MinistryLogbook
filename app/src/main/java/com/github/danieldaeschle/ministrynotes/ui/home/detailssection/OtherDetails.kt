package com.github.danieldaeschle.ministrynotes.ui.home.detailssection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.ui.LocalAppNavController
import com.github.danieldaeschle.ministrynotes.ui.home.HomeGraph
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun OtherDetails(homeViewModel: HomeViewModel = koinViewModel()) {
    val navController = LocalAppNavController.current
    val entries = homeViewModel.entries.collectAsState()
    val studies = homeViewModel.studies.collectAsState(0)
    val selectedMonth = homeViewModel.selectedMonth.collectAsState()
    val accumulatedPlacements = entries.value.sumOf { it.placements }
    val accumulatedReturnVisits = entries.value.sumOf { it.returnVisits }
    val accumulatedVideoShowings = entries.value.sumOf { it.videoShowings }

    Row {
        Column(modifier = Modifier.weight(1f)) {
            OtherDetailRow("Placements", accumulatedPlacements)
            Spacer(modifier = Modifier.height(6.dp))
            OtherDetailRow("Video showings", accumulatedVideoShowings)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            OtherDetailRow("Return visits", accumulatedReturnVisits)
            Spacer(modifier = Modifier.height(6.dp))
            OtherDetailRow("Studies", studies.value, icon = {
                Icon(
                    painterResource(R.drawable.ic_edit),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }, onClick = {
                navController.navigate(
                    HomeGraph.Studies.createRoute(
                        selectedMonth.value.year, selectedMonth.value.monthNumber
                    )
                )
            })
        }
    }
}

@Composable
fun OtherDetailRow(
    name: String, count: Int, icon: (@Composable () -> Unit)? = null, onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(name, color = MaterialTheme.colorScheme.onSurface)
        var modifier = Modifier
            .clip(CircleShape)
            .defaultMinSize(minWidth = 48.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(0.1f))
        if (onClick != null) {
            modifier = modifier.clickable(onClick = onClick)
        }
        modifier = modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                icon()
                Spacer(Modifier.width(8.dp))
            }
            Text(count.toString(), color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Preview
@Composable
fun OtherDetailRowPreview() {
    OtherDetailRow(name = "Test", count = 5)
}