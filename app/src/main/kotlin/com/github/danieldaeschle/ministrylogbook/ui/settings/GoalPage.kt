package com.github.danieldaeschle.ministrylogbook.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrylogbook.R
import com.github.danieldaeschle.ministrylogbook.data.Role
import com.github.danieldaeschle.ministrylogbook.data.rememberSettingsDataStore
import com.github.danieldaeschle.ministrylogbook.ui.LocalAppNavController
import com.github.danieldaeschle.ministrylogbook.ui.shared.ToolbarAction
import kotlinx.coroutines.launch

@Composable
fun GoalPage() {
    val settingsDataStore = rememberSettingsDataStore()
    val navController = LocalAppNavController.current
    val goal by settingsDataStore.goal.collectAsState(null)
    val manuallySetGoal by settingsDataStore.manuallySetGoal.collectAsState(null)
    val role by settingsDataStore.role.collectAsState(null)
    val roleGoal by settingsDataStore.roleGoal.collectAsState(null)
    val coroutineScope = rememberCoroutineScope()
    var textFieldValueState by remember(goal, role, roleGoal, manuallySetGoal) {
        // publishers don't have a goal but internally it's 1
        // we don't want to show that to the user
        // if it was set manually by the user we will show it
        val goalToShow = if (role == Role.Publisher) {
            manuallySetGoal?.toString() ?: ""
        } else {
            goal?.toString() ?: ""
        }
        mutableStateOf(
            TextFieldValue(
                text = goalToShow,
                selection = TextRange(goal.toString().length)
            )
        )
    }
    val isSavable =
        (textFieldValueState.text.toIntOrNull() ?: 0) > 0 || textFieldValueState.text.isEmpty()
    val focusRequester = remember { FocusRequester() }

    val handleSave = handleSave@{
        if (!isSavable) {
            return@handleSave
        }
        coroutineScope.launch {
            settingsDataStore.setGoal(textFieldValueState.text.toIntOrNull())
        }
        navController.popBackStack()
    }

    val handleReset = {
        coroutineScope.launch {
            settingsDataStore.resetGoal()
        }
        textFieldValueState = TextFieldValue(
            text = "",
            selection = TextRange(0)
        )
        navController.popBackStack()
    }

    val handleValueChange: (value: TextFieldValue) -> Unit = {
        if (it.text.toIntOrNull() != null || it.text.isEmpty()) {
            textFieldValueState = it
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BaseSettingsPage(stringResource(R.string.goal), actions = {
        ToolbarAction(onClick = { handleSave() }, disabled = !isSavable) {
            Icon(
                painterResource(R.drawable.ic_done),
                contentDescription = null, // TODO: contentDescription
            )
        }
    }) {
        Column(Modifier.padding(16.dp)) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    keyboardType = KeyboardType.Number
                ),
                keyboardActions = KeyboardActions(onDone = { handleSave() }),
                singleLine = true,
                label = { Text(stringResource(R.string.set_goal)) },
                supportingText = {
                    if (!isSavable) {
                        Text(stringResource(R.string.set_goal_error))
                    }
                },
                value = textFieldValueState,
                onValueChange = handleValueChange,
                isError = !isSavable
            )

            Spacer(Modifier.height(16.dp))

            TextButton(modifier = Modifier.align(Alignment.End), onClick = {
                handleReset()
            }) {
                Text(stringResource(R.string.reset_goal))
            }
        }
    }
}