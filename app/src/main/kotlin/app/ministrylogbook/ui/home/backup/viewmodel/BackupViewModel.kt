package app.ministrylogbook.ui.home.backup.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.shared.services.BackupService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class BackupViewModel(
    private val application: Application,
    private val backupService: BackupService,
    private val settingsService: SettingsService
) : AndroidViewModel(application) {

    val lastBackup = settingsService.lastBackup.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null
    )

    fun createBackup(uri: Uri) {
        viewModelScope.launch {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            settingsService.setLastBackup(now)
            backupService.createBackup(uri)
        }
    }

    fun isBackupValid(uri: Uri) = backupService.validateBackup(uri)

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            val imported = backupService.importBackup(uri)
            if (!imported) {
                Toast.makeText(application.applicationContext, "Backup is invalid", Toast.LENGTH_LONG).show()
            }
        }
    }
}
