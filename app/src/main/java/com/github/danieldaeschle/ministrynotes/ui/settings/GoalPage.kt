package com.github.danieldaeschle.ministrynotes.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.data.rememberSettingsDataStore
import com.github.danieldaeschle.ministrynotes.ui.LocalAppNavController
import com.github.danieldaeschle.ministrynotes.ui.shared.ToolbarAction
import kotlinx.coroutines.launch

@Composable
fun GoalPage() {
    val settingsDataStore = rememberSettingsDataStore()
    val navController = LocalAppNavController.current
    val manuallySetGoal by settingsDataStore.manuallySetGoal.collectAsState(null)
    val coroutineScope = rememberCoroutineScope()

    var textFieldValueState by remember(manuallySetGoal) {
        mutableStateOf(
            TextFieldValue(
                text = manuallySetGoal?.toString() ?: "",
                selection = TextRange(manuallySetGoal.toString().length)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BaseSettingsPage("Goal", actions = {
        ToolbarAction(onClick = {
            coroutineScope.launch {
                settingsDataStore.setGoal(textFieldValueState.text.toIntOrNull())
            }
            navController.popBackStack()
        }) {
            Icon(painterResource(R.drawable.ic_done), contentDescription = null)
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
                singleLine = true,
                label = {
                    Text("Set goal")
                },
                value = textFieldValueState,
                onValueChange = { textFieldValueState = it },
            )

            Spacer(Modifier.height(16.dp))

            TextButton(modifier = Modifier.align(Alignment.End), onClick = {
                coroutineScope.launch {
                    textFieldValueState = TextFieldValue(
                        text = "",
                        selection = TextRange(0)
                    )
                    settingsDataStore.resetGoal()
                }
            }) {
                Text("Reset goal")
            }
        }
    }
}