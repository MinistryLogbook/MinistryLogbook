package com.github.danieldaeschle.ministrynotes.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.data.EntryKind
import com.github.danieldaeschle.ministrynotes.lib.condition
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransferHint(homeViewModel: HomeViewModel = koinViewModel()) {
    val restLastMonth by homeViewModel.restLastMonth.collectAsState()
    val entries by homeViewModel.entries.collectAsState()
    val hasTransfer by remember { derivedStateOf { entries.any { it.kind == EntryKind.Transfer } } }

    if (restLastMonth.isNotEmpty() && !hasTransfer) {
        Spacer(Modifier.height(16.dp))
    }

    Tile {
        Column(
            Modifier
                .fillMaxWidth()
                .condition(restLastMonth.isEmpty() || hasTransfer) {
                    height(0.dp)
                }
                .animateContentSize()) {
            Text(
                "The last month has 10 minutes left. Do you want to transfer it to this month?",
                modifier = Modifier.padding(
                    top = 16.dp,
                    end = 16.dp,
                    start = 16.dp,
                    bottom = 4.dp
                )
            )
            Row(Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {
                    homeViewModel.transfer(dismiss = true)
                }) {
                    Text("Dismiss")
                }
                TextButton(onClick = {
                    homeViewModel.transfer()
                }) {
                    Text("Transfer")
                }
            }
        }
    }
}