package app.ministrylogbook.ui.home.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.ministrylogbook.R
import app.ministrylogbook.data.Entry
import app.ministrylogbook.ui.home.backup.viewmodel.BackupFile
import app.ministrylogbook.ui.theme.extendedColorScheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.datetime.toJavaLocalDateTime

@Composable
fun BackupImportDialog(
    selectedBackupFile: BackupFile,
    latestEntry: Entry?,
    onImport: () -> Unit,
    onDismiss: () -> Unit
) {
    val formattedDateTime = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .format(selectedBackupFile.metadata?.datetime?.toJavaLocalDateTime())
    val metadataDatetime = selectedBackupFile.metadata?.datetime
    val isOlderThanLatestEntry =
        if (latestEntry != null && metadataDatetime != null) {
            metadataDatetime < latestEntry.datetime
        } else {
            false
        }

    AlertDialog(
        title = {
            Text(stringResource(R.string.import_backup_file))
        },
        text = {
            Column {
                if (isOlderThanLatestEntry) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.extendedColorScheme.warning)
                            .padding(horizontal = 8.dp)
                            .defaultMinSize(minHeight = 40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_warning),
                            contentDescription = null,
                            tint = MaterialTheme.extendedColorScheme.onWarning
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.backup_older),
                            color = MaterialTheme.extendedColorScheme.onWarning
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
                Text(stringResource(R.string.import_backup_dialog_description, formattedDateTime))
            }
        },
        onDismissRequest = { onDismiss() },
        confirmButton = {
            TextButton(onClick = { onImport() }) {
                Text(stringResource(R.string.yes))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
