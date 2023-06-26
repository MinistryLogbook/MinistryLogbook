package app.ministrylogbook.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ministrylogbook.R
import app.ministrylogbook.data.Role
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.settings.viewmodel.SettingsViewModel
import app.ministrylogbook.ui.shared.ToolbarAction
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun GoalPage(viewModel: SettingsViewModel = koinViewModel()) {
    val navController = LocalAppNavController.current
    val scrollState = rememberScrollState()
    val goal by viewModel.goal.collectAsStateWithLifecycle()
    val manuallySetGoal by viewModel.manuallySetGoal.collectAsStateWithLifecycle()
    val role by viewModel.role.collectAsStateWithLifecycle()
    val goalValue by remember(goal, manuallySetGoal, role) {
        derivedStateOf {
            // publishers don't have a goal but internally it's 1
            // we don't want to show that to the user
            // if it was set manually by the user we will show it
            if (role == Role.Publisher) {
                return@derivedStateOf manuallySetGoal
            }
            goal
        }
    }
    val coroutineScope = rememberCoroutineScope()
    var tempGoal by remember(goalValue) {
        mutableStateOf(goalValue)
    }
    val isSavable = (tempGoal ?: 0) > 0 || tempGoal == null

    val handleSave = handleSave@{
        if (!isSavable) {
            return@handleSave
        }
        coroutineScope.launch {
            viewModel.setGoal(tempGoal)
        }
        navController.popBackStack()
    }

    val handleReset = {
        coroutineScope.launch {
            viewModel.resetGoal()
        }
        navController.popBackStack()
    }

    BaseSettingsPage(
        title = stringResource(R.string.goal),
        toolbarElevation = scrollState.canScrollBackward,
        actions = {
            ToolbarAction(onClick = { handleSave() }, disabled = !isSavable) {
                Icon(
                    painterResource(R.drawable.ic_done),
                    contentDescription = null // TODO: contentDescription
                )
            }
        }
    ) {
        Column(
            Modifier
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            GoalTextField(value = goalValue, onChange = { tempGoal = it })

            Spacer(Modifier.height(16.dp))

            TextButton(modifier = Modifier.align(Alignment.End), onClick = {
                handleReset()
            }) {
                Text(stringResource(R.string.reset_goal))
            }
        }
    }
}

@Composable
fun GoalTextField(
    value: Int?,
    onChange: (value: Int?) -> Unit = {},
    onDone: () -> Unit = {},
    requestFocus: Boolean = true
) {
    var textFieldValue by remember(value) {
        val valueString = value?.toString() ?: ""
        mutableStateOf(
            TextFieldValue(
                text = valueString,
                selection = TextRange(valueString.length)
            )
        )
    }
    val isSavable = (textFieldValue.text.toIntOrNull() ?: 0) > 0 || textFieldValue.text.isEmpty()
    val focusRequester = remember { FocusRequester() }

    val handleValueChange: (value: TextFieldValue) -> Unit = {
        if (it.text.toIntOrNull() != null || it.text.isEmpty()) {
            textFieldValue = it
            onChange(it.text.toIntOrNull())
        }
    }

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
        }
    }

    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            keyboardType = KeyboardType.Number
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        singleLine = true,
        label = { Text(stringResource(R.string.set_goal)) },
        supportingText = {
            if (!isSavable) {
                Text(stringResource(R.string.set_goal_error))
            }
        },
        value = textFieldValue,
        onValueChange = handleValueChange,
        isError = !isSavable
    )
}
