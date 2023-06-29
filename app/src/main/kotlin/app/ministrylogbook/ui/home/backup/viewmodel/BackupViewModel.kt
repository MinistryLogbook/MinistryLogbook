package app.ministrylogbook.ui.home.backup.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
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
    private val backupService: BackupService,
    private val settingsService: SettingsService
) : ViewModel() {

    val lastBackup = settingsService.lastBackup.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null
    )

    fun createBackup(uri: Uri) {
        viewModelScope.launch {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            settingsService.setLastBackup(now)

            val settings = settingsService.toYaml()
            backupService.createBackup(uri, settings)
        }
    }

    fun importBackup(uri: Uri) {
        val settings = backupService.importBackup(uri)
        viewModelScope.launch {
            if (settings != null) {
                settingsService.fromYaml(settings)
            }
        }
    }
}
