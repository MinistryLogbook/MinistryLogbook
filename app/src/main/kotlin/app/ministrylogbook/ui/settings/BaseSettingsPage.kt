package app.ministrylogbook.ui.settings

import android.R.attr.maxLines
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ministrylogbook.R
import app.ministrylogbook.shared.condition
import app.ministrylogbook.ui.LocalAppNavController
import app.ministrylogbook.ui.shared.Toolbar
import app.ministrylogbook.ui.shared.ToolbarAction
import app.ministrylogbook.ui.theme.MinistryLogbookTheme

@Composable
fun BaseSettingsPage(
    title: String,
    toolbarElevation: Boolean = false,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit = {}
) {
    val navController = LocalAppNavController.current

    val handleBack: () -> Unit = {
        navController.popBackStack()
    }

    MinistryLogbookTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box {
                Toolbar(
                    padding = PaddingValues(horizontal = 12.dp),
                    elevation = if (toolbarElevation) 4.dp else 0.dp
                ) {
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
                }
                Column {
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
        modifier = Modifier.padding(vertical = 10.dp, horizontal = 20.dp)
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Icon(icon, contentDescription = null) // TODO: contentDescription
            Spacer(Modifier.width(20.dp))
        }
        Column(Modifier.weight(1f).padding(end = 8.dp)) {
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
