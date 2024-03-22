package app.ministrylogbook.ui.home.time

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.ministrylogbook.R
import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.shared.Time
import app.ministrylogbook.shared.layouts.progress.LinearProgressIndicator
import app.ministrylogbook.shared.layouts.progress.Progress
import app.ministrylogbook.shared.utilities.ministryTimeSum
import app.ministrylogbook.shared.utilities.transfers
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.home.navigateToEntryDetails
import app.ministrylogbook.ui.home.viewmodel.HomeIntent
import app.ministrylogbook.ui.home.viewmodel.HomeState
import app.ministrylogbook.ui.theme.ProgressPositive
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

val LocalDate.weekNumber: Int
    get() {
        val firstDayOfYear = LocalDate(year, 1, 1)
        val daysFromFirstDay = dayOfYear - firstDayOfYear.dayOfYear
        val firstDayOfYearDayOfWeek = firstDayOfYear.dayOfWeek.value
        val adjustment = when {
            firstDayOfYearDayOfWeek <= 4 -> firstDayOfYearDayOfWeek - 1
            else -> 8 - firstDayOfYearDayOfWeek
        }
        return (daysFromFirstDay + adjustment) / 7 + 1
    }

@Composable
fun WeekNumberSeparator(text: String, weekGoal: Int, timeSum: Time) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(0.7f)
        )
        if (weekGoal > 0) {
            Spacer(Modifier.height(3.dp))
            val progress = Progress(1f / weekGoal * timeSum.toFloat(), color = ProgressPositive)
            LinearProgressIndicator(
                progresses = listOf(progress),
                modifier = Modifier
                    .height(2.dp)
                    .fillMaxWidth()
                    .clip(CircleShape),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

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
        val currentWeek = Clock.System.todayIn(TimeZone.currentSystemDefault()).weekNumber
        val groupedEntries = orderedEntriesWithoutTransfers
            .groupBy { it.datetime.date.weekNumber }

        val currentWeekSum = if (groupedEntries.containsKey(currentWeek)) {
            groupedEntries[currentWeek]!!.ministryTimeSum()
        } else {
            Time.Empty
        } + state.transferred.ministryTimeSum()
        val formattedWeekTime =
            if (currentWeekSum.minutes == 0) currentWeekSum.hours.toString() else currentWeek.toString()
        val currentWeekFormattedSum = stringResource(R.string.hours_short_unit, formattedWeekTime)
        val weekGoal = (state.goal ?: 0) * 12 / 52

        if (state.transferred.isNotEmpty() || transfers.isNotEmpty() || orderedEntriesWithoutTransfers.isNotEmpty()) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                WeekNumberSeparator(
                    text = stringResource(R.string.current_week) + " ($currentWeekFormattedSum)",
                    weekGoal = weekGoal,
                    timeSum = currentWeekSum
                )
            }
        }

        state.transferred.forEach {
            HistoryItem(it, subtract = true, onClick = { transferToUndo = it })
        }

        groupedEntries.forEach { (week, entries) ->
            if (week != currentWeek) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                    val timeSum = entries.ministryTimeSum()
                    val formattedTime = if (timeSum.minutes == 0) timeSum.hours.toString() else timeSum.toString()
                    val formattedTimeSum = stringResource(R.string.hours_short_unit, formattedTime)
                    val text = if (week == currentWeek - 1) {
                        stringResource(R.string.last_week)
                    } else {
                        stringResource(R.string.calendar_week_shorthand, week)
                    } + if (entries.size > 1) " ($formattedTimeSum)" else ""

                    WeekNumberSeparator(
                        text = text,
                        weekGoal = weekGoal,
                        timeSum = timeSum
                    )
                }
            }
            entries.forEach { entry ->
                HistoryItem(entry, onClick = { handleClick(entry) })
            }
            val isLast = week == groupedEntries.keys.last()
            if (!isLast) {
                Spacer(Modifier.height(8.dp))
            }
        }
        transfers.forEach {
            HistoryItem(it, onClick = { transferToUndo = it })
        }
    }
}
