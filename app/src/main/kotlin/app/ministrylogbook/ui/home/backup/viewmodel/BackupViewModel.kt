package app.ministrylogbook.ui.home.backup.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.R
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.shared.IntentViewModel
import app.ministrylogbook.shared.services.BackupService
import app.ministrylogbook.shared.services.Metadata
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

sealed class BackupIntent {
    data class CreateBackup(val uri: Uri) : BackupIntent()
    data class SelectBackupFile(val uri: Uri) : BackupIntent()
    object UnselectBackupFile : BackupIntent()
    object ImportBackup : BackupIntent()
}

data class BackupFile(val uri: Uri, val metadata: Metadata?)

data class BackupState(
    val selectedBackupFile: BackupFile? = null,
    val lastBackup: LocalDateTime? = null,
    val isBackupValid: Boolean = false
)

class BackupViewModel(
    private val uri: Uri? = null,
    private val application: Application,
    private val backupService: BackupService,
    private val settingsService: SettingsService
) : AndroidViewModel(application), IntentViewModel<BackupState, BackupIntent> {

    private val _selectedBackupFile =
        MutableStateFlow(uri?.run { BackupFile(this, backupService.getBackupMetadata(uri)) })

    override val state = combine(
        _selectedBackupFile,
        settingsService.lastBackup
    ) { selectedBackupFile, lastBackup ->
        BackupState(
            selectedBackupFile = selectedBackupFile,
            lastBackup = lastBackup,
            isBackupValid = selectedBackupFile?.run {
                backupService.validateBackup(this.uri) && this.metadata != null
            } ?: false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = BackupState()
    )

    override fun dispatch(intent: BackupIntent) {
        when (intent) {
            is BackupIntent.CreateBackup -> createBackup(intent.uri)
            is BackupIntent.SelectBackupFile -> selectBackupFile(intent.uri)
            is BackupIntent.UnselectBackupFile -> unselectBackupFile()
            is BackupIntent.ImportBackup -> importBackup()
        }
    }

    private fun createBackup(uri: Uri) {
        viewModelScope.launch {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            settingsService.setLastBackup(now)
            backupService.createBackup(uri)
        }
    }

    private fun importBackup() {
        viewModelScope.launch {
            val backupFile = state.value.selectedBackupFile ?: return@launch
            val imported = backupService.importBackup(backupFile.uri)
            if (!imported) {
                val context = application.applicationContext
                Toast.makeText(
                    context,
                    context.getString(R.string.backup_is_invalid),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        unselectBackupFile()
    }

    private fun selectBackupFile(uri: Uri) {
        val metadata = backupService.getBackupMetadata(uri)
        _selectedBackupFile.update {
            BackupFile(uri, metadata)
        }
    }

    private fun unselectBackupFile() {
        _selectedBackupFile.update { null }
    }
}
