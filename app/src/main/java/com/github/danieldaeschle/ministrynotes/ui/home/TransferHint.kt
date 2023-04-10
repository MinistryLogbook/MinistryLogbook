package com.github.danieldaeschle.ministrynotes.ui.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.data.EntryType
import com.github.danieldaeschle.ministrynotes.lib.condition
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun TransferHint(viewModel: HomeViewModel = koinViewModel()) {
    val restLastMonth by viewModel.restLastMonth.collectAsState()
    val entries by viewModel.entries.collectAsState()
    val hasTransfer by remember { derivedStateOf { entries.any { it.type == EntryType.Transfer } } }
    val show = restLastMonth.isNotEmpty() && !hasTransfer

    Spacer(
        Modifier
            .animateContentSize()
            .condition(!show) {
                height(0.dp)
            }
            .condition(show) {
                height(16.dp)
            })

    Tile(
        Modifier
            .animateContentSize()
            .condition(!show) {
                height(0.dp)
            },
        title = {
            Text(stringResource(R.string.last_month_time_remaining_title))
        },
        actions = {
            TextButton(onClick = { viewModel.transferFromLastMonth(restLastMonth.minutes) }) {
                Text(stringResource(R.string.transfer))
            }
        },
        onDismiss = { viewModel.transferFromLastMonth(0) }
    ) {
        Text(
            stringResource(R.string.last_month_time_remaining_description, restLastMonth.minutes)
        )
    }
}