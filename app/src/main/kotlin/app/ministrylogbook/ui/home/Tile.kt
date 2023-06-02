package app.ministrylogbook.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.ministrylogbook.R

@Composable
fun Tile(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    actions: @Composable () -> Unit,
    onDismiss: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        Modifier
            .padding(horizontal = 16.dp)
            .then(modifier),
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Box(Modifier.fillMaxWidth()) {
            onDismiss?.let {
                IconButton(modifier = Modifier.align(Alignment.TopEnd), onClick = it) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Close", // TODO: translation
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Column(Modifier.padding(top = 16.dp, end = 16.dp, start = 16.dp, bottom = 4.dp)) {
                    title()
                    Spacer(Modifier.height(8.dp))
                    ProvideTextStyle(
                        TextStyle(
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                            lineHeight = 20.sp
                        )
                    ) {
                        content()
                    }
                }

                Row(Modifier.padding(vertical = 4.dp, horizontal = 8.dp)) {
                    Spacer(Modifier.weight(1f))
                    actions()
                }
            }
        }
    }
}
