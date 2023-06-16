package app.ministrylogbook.ui.home.backup.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.data.SettingsDataStore
import app.ministrylogbook.shared.BackupService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class BackupViewModel(
    private val backupService: BackupService,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    val lastBackup = settingsDataStore.lastBackup.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null
    )

    fun createBackup(uri: Uri) {
        backupService.createBackup(uri)
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        viewModelScope.launch {
            settingsDataStore.setLastBackup(now)
        }
    }
}
