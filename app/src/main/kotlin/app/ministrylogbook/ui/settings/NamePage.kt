package app.ministrylogbook.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import app.ministrylogbook.R
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.settings.viewmodel.SettingsViewModel
import app.ministrylogbook.ui.shared.ToolbarAction
import org.koin.androidx.compose.koinViewModel

@Composable
fun NamePage(viewModel: SettingsViewModel = koinViewModel()) {
    val navController = LocalAppNavController.current
    val name by viewModel.name.collectAsState("")
    var textFieldValueState by remember(name) {
        mutableStateOf(TextFieldValue(text = name, selection = TextRange(name.length)))
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        viewModel.load()
    }

    val handleSave = {
        viewModel.setName(textFieldValueState.text)
        navController.popBackStack()
    }

    BaseSettingsPage(stringResource(R.string.name), actions = {
        ToolbarAction(onClick = { handleSave() }) {
            Icon(
                painterResource(R.drawable.ic_done),
                contentDescription = null // TODO: contentDescription
            )
        }
    }) {
        Box(Modifier.padding(16.dp)) {
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
                onValueChange = { textFieldValueState = it },
            )
        }
    }
}