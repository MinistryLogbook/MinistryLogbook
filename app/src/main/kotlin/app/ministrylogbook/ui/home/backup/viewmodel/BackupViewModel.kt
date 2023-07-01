package app.ministrylogbook.ui.home.backup.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.R
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.shared.services.BackupService
import app.ministrylogbook.shared.services.Metadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class BackupViewModel(
    private val application: Application,
    private val backupService: BackupService,
    private val settingsService: SettingsService
) : AndroidViewModel(application) {
    private val _selectedBackupFile = MutableStateFlow<BackupFile?>(null)

    val selectedBackupFile = _selectedBackupFile.asStateFlow()

    val lastBackup = settingsService.lastBackup.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null
    )

    val isBackupValid = selectedBackupFile.map {
        it?.let { backupService.validateBackup(it.uri) && it.metadata != null } ?: false
    }

    fun createBackup(uri: Uri) {
        viewModelScope.launch {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            settingsService.setLastBackup(now)
            backupService.createBackup(uri)
        }
    }

    fun importBackup() {
        viewModelScope.launch {
            val backupFile = selectedBackupFile.value ?: return@launch
            val imported = backupService.importBackup(backupFile.uri)
            if (!imported) {
                val context = application.applicationContext
                Toast.makeText(
                    context,
                    context.getString(R.string.backup_is_invalid), Toast.LENGTH_LONG
                ).show()
            }
        }
        unselectBackupFile()
    }

    fun selectBackupFile(uri: Uri) {
        val metadata = backupService.getBackupMetadata(uri)
        _selectedBackupFile.update {
            BackupFile(uri, metadata)
        }
    }

    fun unselectBackupFile() {
        _selectedBackupFile.update { null }
    }

    data class BackupFile(val uri: Uri, val metadata: Metadata?)
}
