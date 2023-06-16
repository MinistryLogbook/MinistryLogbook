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
import androidx.compose.ui.res.painterResource
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

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/mlbak")) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult
            viewModel.createBackup(uri)
        }

    BaseSettingsPage(title = "Backup") {
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
            Text("Last backup", fontSize = 14.sp)
            val formattedLastBackup = if (lastBackup != null) {
                dateTimeFormatter.format(lastBackup?.toJavaLocalDateTime())
            } else {
                "-"
            }
            Text(formattedLastBackup, color = MaterialTheme.colorScheme.onSurface.copy(0.7f), fontSize = 14.sp)
        }

        Setting(icon = painterResource(R.drawable.ic_publish), title = "Create backup file", onClick = {
            val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val fileName = "MinistryLogbookBackup_${currentDate.year}-${
                currentDate.monthNumber.toString().padStart(2, '0')
            }-${currentDate.dayOfMonth.toString().padStart(2, '0')}.mlbak"
            launcher.launch(fileName)
        })

        Setting(icon = painterResource(R.drawable.ic_place_item), title = "Import backup file", onClick = {})
    }
}
