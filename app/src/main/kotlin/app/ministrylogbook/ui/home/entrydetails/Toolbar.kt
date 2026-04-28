package app.ministrylogbook.ui.home.entrydetails

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.ministrylogbook.R
import app.ministrylogbook.ui.shared.DescriptiveIconButton

@Composable
fun Toolbar(
    onClose: () -> Unit = {},
    onSave: () -> Unit = {},
    isSavable: Boolean = true,
    onDelete: () -> Unit = {},
    isDeletable: Boolean = false,
    showSave: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val closeDescription = stringResource(R.string.close)
        DescriptiveIconButton(description = closeDescription, onClick = onClose) {
            Icon(
                painterResource(R.drawable.ic_close),
                contentDescription = closeDescription
            )
        }
        Row {
            if (isDeletable) {
                val deleteDescription = stringResource(R.string.delete_entry)
                DescriptiveIconButton(description = deleteDescription, onClick = onDelete) {
                    Icon(
                        painterResource(R.drawable.ic_delete_forever),
                        contentDescription = deleteDescription
                    )
                }
            }
            if (showSave) {
                if (isDeletable) {
                    Spacer(Modifier.width(8.dp))
                }
                Button(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100))
                        .defaultMinSize(minWidth = 58.dp, minHeight = 20.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                    enabled = isSavable,
                    onClick = { onSave() }
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}
