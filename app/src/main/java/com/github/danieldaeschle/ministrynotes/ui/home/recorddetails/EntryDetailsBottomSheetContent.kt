package com.github.danieldaeschle.ministrynotes.ui.home.recorddetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.data.EntryType
import com.github.danieldaeschle.ministrynotes.data.Role
import com.github.danieldaeschle.ministrynotes.data.rememberSettingsDataStore
import com.github.danieldaeschle.ministrynotes.ui.LocalAppNavController
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.EntryDetailsViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailsBottomSheetContent(viewModel: EntryDetailsViewModel = koinViewModel()) {
    val navController = LocalAppNavController.current
    val entry by viewModel.entry.collectAsState()
    val isSavable by remember {
        derivedStateOf {
            entry.let {
                it.hours > 0 || it.minutes > 0 || it.returnVisits > 0 || it.placements > 0
                        || it.videoShowings > 0
            }
        }
    }
    val hasLoaded = (viewModel.id ?: 0) == entry.id
    var isDateDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isEntryKindDialogVisible by rememberSaveable { mutableStateOf(false) }
    val settingsDataStore = rememberSettingsDataStore()
    val role by settingsDataStore.role.collectAsState(Role.Publisher)
    val dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
    val isCreditEnabled by remember { derivedStateOf { role.canHaveCredit || entry.isCredit } }
    val dateMillis by remember {
        derivedStateOf {
            entry.datetime.atTime(0, 0).toInstant(TimeZone.currentSystemDefault())
                .toEpochMilliseconds()
        }
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dateMillis,
        initialDisplayedMonthMillis = dateMillis,
    )
    val datePickerConfirmEnabled by remember {
        derivedStateOf { datePickerState.selectedDateMillis != null }
    }

    val handleClose: () -> Unit = {
        isDateDialogVisible = false
        navController.popBackStack()
    }
    val handleSave = {
        viewModel.save()
        handleClose()
    }
    val handleDelete = {
        viewModel.delete()
        handleClose()
    }
    val handleChangeHours: (newValue: Int) -> Unit = {
        if (it in 0..99) {
            viewModel.update(hours = it)
        }
    }
    val handleChangeMinutes: (newValue: Int) -> Unit = {
        if (entry.hours > 0 && it < 0) {
            viewModel.update(
                hours = entry.hours - 1, minutes = 60 + it
            )
        } else if (it > 59) {
            viewModel.update(
                hours = entry.hours + 1, minutes = it - 60
            )
        } else if (it in 0..55) {
            viewModel.update(minutes = it)
        }
    }
    val handleChangePlacements: (newValue: Int) -> Unit = {
        if (it in 0..999) {
            viewModel.update(placements = it)
        }
    }

    val handleChangeReturnVisits: (newValue: Int) -> Unit = {
        if (it in 0..99) {
            viewModel.update(returnVisits = it)
        }
    }
    val handleChangeVideos: (newValue: Int) -> Unit = {
        if (it in 0..99) {
            viewModel.update(videoShowings = it)
        }
    }
    val handleChangeDate: (newValue: LocalDate) -> Unit = {
        viewModel.update(datetime = it)
    }
    val handleKindDate: (newValue: EntryType) -> Unit = {
        viewModel.update(kind = it)
    }

    if (isDateDialogVisible) {
        val onDismissRequest = {
            isDateDialogVisible = false
        }
        val onDateChange: () -> Unit = {
            datePickerState.selectedDateMillis?.let {
                val dateTime = Instant.fromEpochMilliseconds(it).toLocalDateTime(
                    TimeZone.currentSystemDefault()
                )
                handleChangeDate(dateTime.date)
            }
            isDateDialogVisible = false
        }
        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = onDateChange,
                    enabled = datePickerConfirmEnabled
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isDateDialogVisible = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (isDeleteDialogVisible) {
        val onDismissRequest = {
            isDeleteDialogVisible = false
        }
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text("Delete entry")
            },
            text = {
                Text("Do you really want to delete this entry?")
            },
            confirmButton = {
                val onClick = {
                    isDeleteDialogVisible = false
                    handleDelete()
                }
                TextButton(onClick = onClick) {
                    Text("Yes")
                }
            },
            dismissButton = {
                val onClick = {
                    isDeleteDialogVisible = false
                }
                TextButton(onClick = onClick) {
                    Text("Cancel")
                }
            },
        )
    }

    com.github.danieldaeschle.ministrynotes.lib.AlertDialog(
        isOpen = isEntryKindDialogVisible,
        onClose = { isEntryKindDialogVisible = false },
        paddingValues = PaddingValues(vertical = 8.dp),
    ) {
        val entryTypes = listOfNotNull(
            EntryType.Ministry,
            if (isCreditEnabled) EntryType.TheocraticAssignment else null,
            if (isCreditEnabled) EntryType.TheocraticSchool else null,
            if (!role.canHaveCredit) entry.type else null,
        )

        Column {
            entryTypes.forEach {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            handleKindDate(it)
                            isEntryKindDialogVisible = false
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        selected = it == entry.type,
                        onClick = null,
                    )
                    Text(it.translate())
                }
            }
        }
    }

    Column(modifier = Modifier.navigationBarsPadding()) {
        DragLine()
        Toolbar(
            onClose = handleClose,
            onSave = handleSave,
            isSavable = isSavable,
            onDelete = { isDeleteDialogVisible = true },
            isDeletable = entry.id != 0,
        )
        Divider()
        Box(Modifier.clickable { isDateDialogVisible = true }) {
            Box(Modifier.padding(bottom = 12.dp, start = 20.dp, top = 12.dp, end = 20.dp)) {
                val dateStr = dateTimeFormatter.format(
                    entry.datetime.toJavaLocalDate()
                )
                UnitRow(dateStr, icon = painterResource(R.drawable.ic_today))
            }
        }
        if (isCreditEnabled) {
            Divider()
            Box(Modifier.clickable { isEntryKindDialogVisible = true }) {
                Box(Modifier.padding(bottom = 12.dp, start = 20.dp, top = 12.dp, end = 20.dp)) {
                    UnitRow(
                        entry.type.translate(),
                        icon = entry.type.icon(),
                    )
                }
            }
        }
        Divider()
        Column(
            Modifier
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp)
                .fillMaxWidth()
        ) {
            UnitRow("Hours", icon = painterResource(R.drawable.ic_schedule)) {
                NumberPicker(entry.hours) {
                    handleChangeHours(it)
                }

            }
            UnitRow("Minutes") {
                NumberPicker(entry.minutes, step = 5) {
                    handleChangeMinutes(it)
                }
            }

            if (hasLoaded) {
                AnimatedVisibility(visible = entry.type == EntryType.Ministry) {
                    Column {
                        UnitRow(
                            "Placements",
                            description = "Printed and Electronic",
                            icon = painterResource(R.drawable.ic_article)
                        ) {
                            NumberPicker(entry.placements) {
                                handleChangePlacements(it)
                            }
                        }
                        UnitRow("Videos", icon = painterResource(R.drawable.ic_play_circle)) {
                            NumberPicker(entry.videoShowings) {
                                handleChangeVideos(it)
                            }
                        }
                        UnitRow("Return visits", icon = painterResource(R.drawable.ic_group)) {
                            NumberPicker(entry.returnVisits) {
                                handleChangeReturnVisits(it)
                            }
                        }
                    }
                }
            }
        }
    }
}