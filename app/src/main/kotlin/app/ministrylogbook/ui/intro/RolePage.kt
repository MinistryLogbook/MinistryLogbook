package app.ministrylogbook.ui.intro

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ministrylogbook.R
import app.ministrylogbook.data.Role
import app.ministrylogbook.shared.layouts.ExpandAnimatedVisibility
import app.ministrylogbook.shared.layouts.MonthPickerDialog
import app.ministrylogbook.ui.intro.viewmodel.IntroState
import java.time.format.DateTimeFormatter
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.todayIn

@Composable
fun PioneerSinceSection(state: IntroState, onPioneerSinceSet: (LocalDate) -> Unit) {
    val source = remember { MutableInteractionSource() }
    val isPressed by source.collectIsPressedAsState()
    val pattern = stringResource(R.string.start_of_pioneering_month_pattern)
    val formatter = DateTimeFormatter.ofPattern(pattern)
    val monthText = if (state.pioneerSince != null) {
        formatter.format(state.pioneerSince.toJavaLocalDate())
    } else {
        stringResource(R.string.no_date_set)
    }
    var isDialogOpen by remember { mutableStateOf(false) }
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    val handlePioneerSinceClick = {
        isDialogOpen = true
    }

    MonthPickerDialog(
        isOpen = isDialogOpen,
        initialMonth = state.pioneerSince ?: today,
        onDismissRequest = { isDialogOpen = false },
        onSelect = {
            onPioneerSinceSet(it)
            isDialogOpen = false
        }
    )

    Column {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            stringResource(R.string.intro_pioneer_since_question),
            color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))

        LaunchedEffect(isPressed) {
            if (isPressed) {
                handlePioneerSinceClick()
            }
        }

        TextField(
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            value = monthText,
            onValueChange = { },
            label = { Text(stringResource(R.string.select_date)) },
            trailingIcon = {
                Icon(painterResource(R.drawable.ic_today), contentDescription = null)
            },
            interactionSource = source
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolePage(
    state: IntroState,
    onChange: (role: Role) -> Unit,
    onPioneerSinceSet: (date: LocalDate) -> Unit,
    scrollState: ScrollState = rememberScrollState()
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(32.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.intro_role_title, state.name ?: ""),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 40.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            stringResource(R.string.intro_role_description),
            color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        ExposedDropdownMenuBox(modifier = Modifier.fillMaxWidth(), expanded = expanded, onExpandedChange = {
            expanded = !expanded
        }) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                value = state.role.translate(),
                onValueChange = { },
                label = { Text(stringResource(R.string.select_role)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )
            ExposedDropdownMenu(
                modifier = Modifier.fillMaxWidth(),
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                }
            ) {
                Role.entries.map { r ->
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onChange(r)
                            expanded = false
                        },
                        text = { Text(text = r.translate()) }
                    )
                }
            }
        }

        val isPioneer = state.role == Role.RegularPioneer || state.role == Role.SpecialPioneer
        ExpandAnimatedVisibility(show = isPioneer) {
            PioneerSinceSection(state, onPioneerSinceSet)
        }
    }
}
