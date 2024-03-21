package app.ministrylogbook.ui.home.time

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.ministrylogbook.R
import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.shared.utilities.transfers
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.home.navigateToEntryDetails
import app.ministrylogbook.ui.home.viewmodel.HomeIntent
import app.ministrylogbook.ui.home.viewmodel.HomeState

@Composable
fun HistorySection(state: HomeState, dispatch: (intent: HomeIntent) -> Unit = {}) {
    val navController = LocalAppNavController.current
    val orderedEntries by remember(state) {
        derivedStateOf { state.entries.sortedBy { it.datetime }.reversed() }
    }
    val orderedEntriesWithoutTransfers by remember(orderedEntries) {
        derivedStateOf { orderedEntries.filter { it.type != EntryType.Transfer } }
    }
    val transfers by remember(state) {
        derivedStateOf { state.entries.transfers().filter { it.time.isNotEmpty } }
    }
    var transferToUndo by remember { mutableStateOf<Entry?>(null) }

    val handleClick: (entry: Entry) -> Unit = {
        navController.navigateToEntryDetails(state.month, it.id)
    }

    val handleUndoTransfer: () -> Unit = {
        transferToUndo?.let { entry ->
            val intent = HomeIntent.UndoTransfer(entry)
            dispatch(intent)
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
            onDismissRequest = { transferToUndo = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        state.transferred.forEach {
            HistoryItem(it, subtract = true, onClick = { transferToUndo = it })
        }
        orderedEntriesWithoutTransfers.forEach {
            HistoryItem(it, onClick = { handleClick(it) })
        }
        transfers.forEach {
            HistoryItem(it, onClick = { transferToUndo = it })
        }
    }
}
