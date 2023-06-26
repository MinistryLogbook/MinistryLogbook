package app.ministrylogbook.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.ministrylogbook.ui.shared.Toolbar

@Composable
fun ToolbarLayout(
    elevation: Boolean = false,
    toolbarContent: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            Toolbar(
                padding = PaddingValues(horizontal = 12.dp),
                elevation = if (elevation) 4.dp else 0.dp,
                content = toolbarContent
            )
            Column(Modifier.statusBarsPadding().fillMaxSize()) {
                Spacer(Modifier.height(56.dp))
                content()
            }
        }
    }
}
