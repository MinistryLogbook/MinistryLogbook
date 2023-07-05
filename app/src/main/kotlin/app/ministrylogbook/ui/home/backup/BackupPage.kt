package app.ministrylogbook.ui.home.backup

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ministrylogbook.R
import app.ministrylogbook.ui.home.backup.viewmodel.BackupIntent
import app.ministrylogbook.ui.home.backup.viewmodel.BackupViewModel
import app.ministrylogbook.ui.settings.BaseSettingsPage
import app.ministrylogbook.ui.settings.Setting
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.todayIn
import org.koin.androidx.compose.koinViewModel

@Composable
fun BackupPage(viewModel: BackupViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val fileExtension = "mlbak"

    val createDocumentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/mlbak")) { uri ->
            if (uri == null) {
                return@rememberLauncherForActivityResult
            }
            viewModel.dispatch(BackupIntent.CreateBackup(uri))
        }
    val openDocumentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) {
                return@rememberLauncherForActivityResult
            }
            viewModel.dispatch(BackupIntent.SelectBackupFile(uri))
        }

    LaunchedEffect(state.selectedBackupFile, state.isBackupValid) {
        if (state.selectedBackupFile != null && !state.isBackupValid) {
            Toast.makeText(
                context,
                context.getString(R.string.backup_is_invalid),
                Toast.LENGTH_LONG
            ).show()
            viewModel.dispatch(BackupIntent.UnselectBackupFile)
        }
    }

    if (state.selectedBackupFile != null && state.isBackupValid) {
        val formattedDateTime = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .format(state.selectedBackupFile?.metadata?.datetime?.toJavaLocalDateTime())
        val latest = state.latest
        val metadataDatetime = state.selectedBackupFile?.metadata?.datetime
        val isOlderThanLatestEntry =
            if (latest != null && metadataDatetime != null) metadataDatetime < latest.datetime else false

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
                                .background(MaterialTheme.colorScheme.error)
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_warning),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onError
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                stringResource(R.string.backup_older),
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.import_backup_dialog_description, formattedDateTime))
                }
            },
            onDismissRequest = {
                viewModel.dispatch(BackupIntent.UnselectBackupFile)
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dispatch(BackupIntent.ImportBackup) }) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dispatch(BackupIntent.UnselectBackupFile) }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    BaseSettingsPage(title = stringResource(R.string.backup)) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            Image(
                painterResource(R.drawable.ic_settings_backup_restore),
                contentDescription = null, // TODO: contentDescription
                modifier = Modifier.size(128.dp)
            )
            Text(stringResource(R.string.last_backup), fontSize = 14.sp)
            val formattedLastBackup = if (state.lastBackup != null) {
                dateTimeFormatter.format(state.lastBackup?.toJavaLocalDateTime())
            } else {
                "-"
            }
            Text(formattedLastBackup, color = MaterialTheme.colorScheme.onSurface.copy(0.7f), fontSize = 14.sp)
        }

        Setting(
            icon = painterResource(R.drawable.ic_publish),
            title = stringResource(R.string.create_backup_file),
            onClick = {
                val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val year = currentDate.year.toString()
                val month = currentDate.monthNumber.toString().padStart(2, '0')
                val dayOfMonth = currentDate.dayOfMonth.toString().padStart(2, '0')
                val fileName =
                    context.getString(R.string.backup_file_name, year, month, dayOfMonth) + ".$fileExtension"
                createDocumentLauncher.launch(fileName)
            }
        )

        Setting(
            icon = painterResource(R.drawable.ic_place_item),
            title = stringResource(R.string.import_backup_file),
            onClick = {
                openDocumentLauncher.launch(arrayOf("application/*"))
            }
        )
    }
}
