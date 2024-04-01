package app.ministrylogbook.ui.home.biblestudies

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.ministrylogbook.R

@Composable
fun BibleStudyItem(
    name: String,
    checked: Boolean = false,
    onDelete: (() -> Unit)?,
    onCheckedChange: ((checked: Boolean) -> Unit)? = null
) {
    var isDialogOpen by rememberSaveable { mutableStateOf(false) }

    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = { isDialogOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    isDialogOpen = false
                    onDelete?.invoke()
                }) {
                    Text(stringResource(R.string.yes))
                }
            },
            title = { Text(stringResource(R.string.delete_bible_study)) },
            text = { Text(stringResource(R.string.delete_bible_study_description, name)) },
            dismissButton = {
                TextButton(onClick = { isDialogOpen = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Row(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(16.dp))
        Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        IconButton(onClick = { isDialogOpen = true }) {
            Icon(painterResource(R.drawable.ic_delete_forever), contentDescription = null)
        }
    }
}
