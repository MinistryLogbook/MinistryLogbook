package app.ministrylogbook.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ministrylogbook.R
import app.ministrylogbook.shared.layouts.ToolbarLayout
import app.ministrylogbook.shared.utilities.condition
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.shared.ToolbarAction

@Composable
fun BaseSettingsPage(
    title: String,
    toolbarElevation: Boolean = false,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val navController = LocalAppNavController.current

    val handleBack: () -> Unit = {
        navController.popBackStack()
    }

    ToolbarLayout(elevation = toolbarElevation, toolbarContent = {
        ToolbarAction(onClick = handleBack) {
            Icon(
                painterResource(R.drawable.ic_arrow_back),
                contentDescription = null // TODO: contentDescription
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )

        Spacer(Modifier.weight(1f))
        actions?.invoke()
    }, content = content)
}

@Composable
fun Title(text: String) {
    Text(
        text,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp)
    )
}

@Composable
fun Setting(
    title: String,
    icon: Painter? = null,
    paddingValues: PaddingValues = PaddingValues(20.dp, 14.dp),
    description: String? = null,
    onClick: (() -> Unit)? = null,
    value: @Composable () -> Unit = {}
) {
    Row(
        Modifier
            .condition(onClick != null) {
                clickable(onClick = onClick!!)
            }
            .padding(paddingValues),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Icon(icon, contentDescription = null) // TODO: contentDescription
            Spacer(Modifier.width(20.dp))
        }
        Column(
            Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(title, fontSize = MaterialTheme.typography.titleMedium.fontSize)
            description?.let {
                Text(
                    description,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.8f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        }
        value()
    }
}
