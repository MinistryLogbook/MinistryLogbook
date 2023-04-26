package com.github.danieldaeschle.ministrynotes.ui.home.historysection

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.data.Entry
import com.github.danieldaeschle.ministrynotes.data.EntryType
import com.github.danieldaeschle.ministrynotes.ui.LocalAppNavController
import com.github.danieldaeschle.ministrynotes.ui.home.navigateToEntryDetails
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.HomeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistorySection(viewModel: HomeViewModel = koinViewModel()) {
    val navController = LocalAppNavController.current
    val entries by viewModel.entries.collectAsState()
    val orderedEntries by remember(entries) {
        derivedStateOf { entries.sortedBy { it.datetime }.reversed() }
    }
    var transferToUndo by remember { mutableStateOf<Entry?>(null) }
    val transferred by viewModel.transferred.collectAsState()

    val handleClick: (entry: Entry) -> Unit = {
        navController.navigateToEntryDetails(viewModel.month, it.id)
    }

    val handleUndoTransfer: () -> Unit = {
        transferToUndo?.let { entry ->
            viewModel.undoTransfer(entry)
            transferToUndo = null
        }
    }

    if (transferToUndo != null) {
        AlertDialog(
            title = {
                Text(stringResource(R.string.undo_transfer_title))
            },
            text = {
                Text(stringResource(R.string.undo_transfer_description))
            },
            dismissButton = {
                TextButton(onClick = { transferToUndo = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(onClick = handleUndoTransfer) {
                    Text(stringResource(R.string.undo))
                }
            },
            onDismissRequest = { transferToUndo = null },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        transferred.filter { it.time.isNotEmpty }.forEach {
            HistoryItem(it, subtract = true, onClick = { transferToUndo = it })
        }
        orderedEntries.filter { it.type != EntryType.Transfer }.forEach {
            HistoryItem(it, onClick = { handleClick(it) })
        }
        orderedEntries.filter { it.type == EntryType.Transfer }.forEach {
            HistoryItem(it, onClick = { transferToUndo = it })
        }
    }
}
