package app.ministrylogbook.ui.home.backup.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.R
import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryRepository
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
    data object UnselectBackupFile : BackupIntent()
    data object ImportBackup : BackupIntent()
}

data class BackupFile(val uri: Uri, val metadata: Metadata?)

data class BackupState(
    val selectedBackupFile: BackupFile? = null,
    val lastBackup: LocalDateTime? = null,
    val isBackupValid: Boolean = false,
    val latestEntry: Entry? = null,
    val importFinished: Boolean = false
)

class BackupViewModel(
    private val _application: Application,
    private val _backupService: BackupService,
    private val _settingsService: SettingsService,
    entryRepository: EntryRepository
) : AndroidViewModel(_application),
    IntentViewModel<BackupState, BackupIntent> {

    private val selectedBackupFile = MutableStateFlow<BackupFile?>(null)

    private val importFinished = MutableStateFlow(false)

    override val state = combine(
        selectedBackupFile,
        _settingsService.lastBackup,
        entryRepository.latest,
        importFinished
    ) { selectedBackupFile, lastBackup, latestEntry, importFinished ->
        BackupState(
            selectedBackupFile = selectedBackupFile,
            lastBackup = lastBackup,
            isBackupValid = selectedBackupFile?.run {
                _backupService.validateBackup(this.uri) && this.metadata != null
            } == true,
            latestEntry = latestEntry,
            importFinished = importFinished
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
            _settingsService.setLastBackup(now)
            _backupService.createBackup(uri)
        }
    }

    private fun importBackup() {
        viewModelScope.launch {
            val backupFile = state.value.selectedBackupFile ?: return@launch
            val imported = _backupService.importBackup(backupFile.uri)
            if (!imported) {
                val context = _application.applicationContext
                Toast.makeText(
                    context,
                    context.getString(R.string.backup_is_invalid),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                importFinished.update { true }
            }
        }
        unselectBackupFile()
    }

    private fun selectBackupFile(uri: Uri) {
        val metadata = _backupService.getBackupMetadata(uri)
        selectedBackupFile.update {
            BackupFile(uri, metadata)
        }
    }

    private fun unselectBackupFile() {
        selectedBackupFile.update { null }
    }
}
