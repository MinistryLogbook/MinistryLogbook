package app.ministrylogbook.ui.home.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.ministrylogbook.R
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
    val lastBackup by viewModel.lastBackup.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val createDocumentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/mlbak")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            viewModel.createBackup(uri)
        }
    val openDocumentLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            viewModel.importBackup(uri)
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
            val formattedLastBackup = if (lastBackup != null) {
                dateTimeFormatter.format(lastBackup?.toJavaLocalDateTime())
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
                val fileName = context.getString(R.string.backup_file_name, year, month, dayOfMonth) + ".mlbak"
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
