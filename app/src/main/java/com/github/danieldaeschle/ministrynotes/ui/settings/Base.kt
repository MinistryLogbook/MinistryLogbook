package com.github.danieldaeschle.ministrynotes.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.danieldaeschle.ministrynotes.R
import com.github.danieldaeschle.ministrynotes.lib.condition
import com.github.danieldaeschle.ministrynotes.ui.LocalAppNavController
import com.github.danieldaeschle.ministrynotes.ui.shared.Toolbar
import com.github.danieldaeschle.ministrynotes.ui.shared.ToolbarAction
import com.github.danieldaeschle.ministrynotes.ui.theme.MinistryNotesTheme


@Composable
fun BaseSettingsPage(
    title: String,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit = {}
) {
    val navController = LocalAppNavController.current

    val handleBack: () -> Unit = {
        navController.popBackStack()
    }

    MinistryNotesTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box {
                Toolbar(padding = PaddingValues(horizontal = 12.dp)) {
                    ToolbarAction(onClick = handleBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(title, fontSize = MaterialTheme.typography.titleLarge.fontSize)

                    Spacer(Modifier.weight(1f))
                    actions?.invoke()
                }
                Column(
                    Modifier
                        .statusBarsPadding()
                        .padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Spacer(Modifier.height(56.dp))
                    content()
                }
            }
        }
    }
}

@Composable
fun Title(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp, start = 20.dp, end = 20.dp),
    )
}

@Composable
fun Setting(
    title: String,
    icon: Painter? = null,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    value: @Composable () -> Unit = {}
) {
    Row(
        Modifier
            .condition(onClick != null) {
                clickable(onClick = onClick!!)
            }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(20.dp))
        }
        Column {
            Text(title, fontSize = MaterialTheme.typography.titleMedium.fontSize)
            description?.let {
                Text(
                    description,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                )
            }
        }
        Spacer(Modifier.weight(1f))
        value()
    }
}