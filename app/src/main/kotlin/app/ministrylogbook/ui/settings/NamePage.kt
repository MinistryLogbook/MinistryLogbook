package app.ministrylogbook.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ministrylogbook.R
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.settings.viewmodel.SettingsViewModel
import app.ministrylogbook.ui.shared.ToolbarAction
import org.koin.androidx.compose.koinViewModel

@Composable
fun NamePage(viewModel: SettingsViewModel = koinViewModel()) {
    val navController = LocalAppNavController.current
    val scrollState = rememberScrollState()
    val name by viewModel.name.collectAsStateWithLifecycle()
    var textFieldValueState by remember(name) {
        mutableStateOf(TextFieldValue(text = name, selection = TextRange(name.length)))
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val handleSave = {
        viewModel.setName(textFieldValueState.text)
        navController.popBackStack()
    }

    BaseSettingsPage(
        title = stringResource(R.string.name),
        toolbarElevation = scrollState.canScrollBackward,
        actions = {
            ToolbarAction(onClick = { handleSave() }) {
                Icon(
                    painterResource(R.drawable.ic_done),
                    contentDescription = null // TODO: contentDescription
                )
            }
        }
    ) {
        Box(Modifier.verticalScroll(scrollState).padding(16.dp)) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                keyboardActions = KeyboardActions(onDone = { handleSave() }),
                singleLine = true,
                label = {
                    Text(stringResource(R.string.set_name))
                },
                value = textFieldValueState,
                onValueChange = { textFieldValueState = it }
            )
        }
    }
}
