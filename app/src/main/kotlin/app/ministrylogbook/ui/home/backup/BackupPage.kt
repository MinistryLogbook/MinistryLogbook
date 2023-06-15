package app.ministrylogbook.ui.home.backup

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import app.ministrylogbook.R
import app.ministrylogbook.ui.settings.BaseSettingsPage
import app.ministrylogbook.ui.settings.Setting

@Composable
fun BackupPage() {
    BaseSettingsPage(title = "Backup") {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painterResource(R.drawable.ic_settings_backup_restore),
                contentDescription = null, // TODO: contentDescription
                modifier = Modifier.size(128.dp)
            )
            Text("Last backup")
            Text("-")
        }

        Setting(icon = painterResource(R.drawable.ic_publish), title = "Create backup file", onClick = {})

        Setting(icon = painterResource(R.drawable.ic_place_item), title = "Import backup file", onClick = {})
    }
}
