package com.github.danieldaeschle.ministrynotes.ui.home.recorddetails

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.data.Role
import com.github.danieldaeschle.ministrynotes.data.rememberSettingsDataStore
import com.github.danieldaeschle.ministrynotes.ui.LocalAppNavController
import com.github.danieldaeschle.ministrynotes.ui.home.HomeGraph
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.EntryDetailsViewModel
import com.marosseleng.compose.material3.datetimepickers.date.ui.dialog.DatePickerDialog
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EntryDetailsBottomSheetContent(
    id: Int? = null,
    entryDetailsViewModel: EntryDetailsViewModel = koinViewModel(),
) {
    val navController = LocalAppNavController.current
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry.value?.destination?.route
    val entry = entryDetailsViewModel.entry.collectAsState()
    val isSavable = entry.value.let {
        it.hours > 0 || it.minutes > 0 || it.returnVisits > 0 || it.placements > 0 || it.videoShowings > 0 || it.creditHours > 0 || it.creditMinutes > 0
    }
    var isDateDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }
    val settingsDataStore = rememberSettingsDataStore()
    val role = settingsDataStore.role.collectAsState(Role.Publisher)
    val dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

    val handleClose: () -> Unit = {
        isDateDialogVisible = false
        navController.popBackStack()
    }
    val handleSave = {
        entryDetailsViewModel.save()
        handleClose()
    }
    val handleDelete = {
        entryDetailsViewModel.delete()
        handleClose()
    }
    val handleChangeHours: (newValue: Int) -> Unit = {
        if (it in 0..99) {
            entryDetailsViewModel.update(hours = it)
        }
    }
    val handleChangeMinutes: (newValue: Int) -> Unit = {
        if (entry.value.hours > 0 && it < 0) {
            entryDetailsViewModel.update(
                hours = entry.value.hours - 1, minutes = 60 + it
            )
        } else if (it > 59) {
            entryDetailsViewModel.update(
                hours = entry.value.hours + 1, minutes = it - 60
            )
        } else if (it in 0..55) {
            entryDetailsViewModel.update(minutes = it)
        }
    }
    val handleChangePlacements: (newValue: Int) -> Unit = {
        if (it in 0..999) {
            entryDetailsViewModel.update(placements = it)
        }
    }

    val handleChangeReturnVisits: (newValue: Int) -> Unit = {
        if (it in 0..99) {
            entryDetailsViewModel.update(returnVisits = it)
        }
    }
    val handleChangeVideos: (newValue: Int) -> Unit = {
        if (it in 0..99) {
            entryDetailsViewModel.update(videoShowings = it)
        }
    }
    val handleChangeDate: (newValue: LocalDate) -> Unit = {
        entryDetailsViewModel.update(datetime = it)
    }
    val handleChangeCreditHours: (newValue: Int) -> Unit = {
        if (it in 0..99) {
            entryDetailsViewModel.update(creditHours = it)
        }
    }
    val handleChangeCreditMinutes: (newValue: Int) -> Unit = {
        if (entry.value.hours > 0 && it < 0) {
            entryDetailsViewModel.update(
                creditHours = entry.value.hours - 1, creditMinutes = 60 + it
            )
        } else if (it > 59) {
            entryDetailsViewModel.update(
                creditHours = entry.value.hours + 1, creditMinutes = it - 60
            )
        } else if (it in 0..55) {
            entryDetailsViewModel.update(creditMinutes = it)
        }
    }

    LaunchedEffect(id, currentRoute) {
        if (currentRoute == HomeGraph.EntryDetails.route) {
            entryDetailsViewModel.load(id)
        }
    }

    if (isDateDialogVisible) {
        val onDismissRequest = {
            isDateDialogVisible = false
        }
        val onDateChange: (newDate: java.time.LocalDate) -> Unit = {
            val date = LocalDate(it.year, it.monthValue, it.dayOfMonth)
            handleChangeDate(date)
            isDateDialogVisible = false
        }
        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            onDateChange = onDateChange,
            initialDate = entry.value.datetime.toJavaLocalDate(),
            shape = RoundedCornerShape(16.dp),
        )
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

    Column(modifier = Modifier.navigationBarsPadding()) {
        DragLine()
        Toolbar(
            onClose = handleClose,
            onSave = handleSave,
            isSavable = isSavable,
            onDelete = { isDeleteDialogVisible = true },
            isDeletable = entry.value.id != 0,
        )
        Divider()
        Box(Modifier.clickable { isDateDialogVisible = true }) {
            Box(Modifier.padding(bottom = 16.dp, start = 20.dp, top = 16.dp, end = 20.dp)) {
                val dateStr = dateTimeFormatter.format(
                    entry.value.datetime.toJavaLocalDate()
                )
                UnitRow(dateStr, icon = painterResource(R.drawable.ic_today))
            }
        }
        Divider()
        Column(
            Modifier
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)
                .fillMaxWidth()
        ) {
            UnitRow("Hours", icon = painterResource(R.drawable.ic_schedule)) {
                NumberPicker(entry.value.hours) {
                    handleChangeHours(it)
                }

            }
            UnitRow("Minutes") {
                NumberPicker(entry.value.minutes, step = 5) {
                    handleChangeMinutes(it)
                }
            }
            UnitRow(
                "Placements",
                description = "Printed and Electronic",
                icon = painterResource(R.drawable.ic_article)
            ) {
                NumberPicker(entry.value.placements) {
                    handleChangePlacements(it)
                }
            }
            UnitRow("Videos", icon = painterResource(R.drawable.ic_play_circle)) {
                NumberPicker(entry.value.videoShowings) {
                    handleChangeVideos(it)
                }
            }
            UnitRow("Return visits", icon = painterResource(R.drawable.ic_group)) {
                NumberPicker(entry.value.returnVisits) {
                    handleChangeReturnVisits(it)
                }
            }
        }

        if (role.value in arrayOf(
                Role.RegularPioneer, Role.SpecialPioneer
            ) || entry.value.creditHours > 0 || entry.value.creditMinutes > 0
        ) {
            Divider()

            Column(
                Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "Credit".uppercase(),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                UnitRow("Hours", icon = painterResource(R.drawable.ic_volunteer_activism)) {
                    NumberPicker(entry.value.creditHours) {
                        handleChangeCreditHours(it)
                    }
                }
                UnitRow("Minutes") {
                    NumberPicker(entry.value.creditMinutes, step = 5) {
                        handleChangeCreditMinutes(it)
                    }
                }
            }
        }
    }
}