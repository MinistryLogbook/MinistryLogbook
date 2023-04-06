package com.github.danieldaeschle.ministrynotes.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.data.rememberSettingsDataStore
import com.github.danieldaeschle.ministrynotes.ui.LocalAppNavController
import com.github.danieldaeschle.ministrynotes.ui.shared.ToolbarAction
import kotlinx.coroutines.launch

@Composable
fun NamePage() {
    val settingsDataStore = rememberSettingsDataStore()
    val navController = LocalAppNavController.current
    val name by settingsDataStore.name.collectAsState("")
    val coroutineScope = rememberCoroutineScope()
    var nameValue by remember(name) { mutableStateOf(name) }

    BaseSettingsPage("Name", actions = {
        ToolbarAction(onClick = {
            coroutineScope.launch {
                settingsDataStore.setName(nameValue)
            }
            navController.popBackStack()
        }) {
            Icon(painterResource(R.drawable.ic_done), contentDescription = null)
        }
    }) {
        Box(Modifier.padding(16.dp)) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
                label = {
                    Text("Set name")
                },
                value = nameValue,
                onValueChange = { nameValue = it })
        }
    }
}