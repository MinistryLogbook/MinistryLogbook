package app.ministrylogbook.ui.home.viewmodel

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
import app.ministrylogbook.ui.home.backup.viewmodel.BackupFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class HomeIntent {
    object ImportBackup : HomeIntent()

    object DismissImportBackup : HomeIntent()
}

data class HomeState(
    val name: String = "",
    val selectedBackupFile: BackupFile? = null,
    val isBackupValid: Boolean = false,
    val latestEntry: Entry? = null,
    val importFinished: Boolean = false
)

class HomeViewModel(
    private val _uri: Uri? = null,
    private val _application: Application,
    private val _backupService: BackupService,
    settingsService: SettingsService,
    entryRepository: EntryRepository
) : AndroidViewModel(_application), IntentViewModel<HomeState, HomeIntent> {

    private val _selectedBackupFile =
        MutableStateFlow(_uri?.run { BackupFile(this, _backupService.getBackupMetadata(_uri)) })

    private val _importFinished = MutableStateFlow(false)

    override val state = combine(
        settingsService.name,
        entryRepository.latest,
        _selectedBackupFile,
        _importFinished
    ) { name, latestEntry, selectedBackupFile, importFinished ->
        HomeState(
            name = name,
            latestEntry = latestEntry,
            selectedBackupFile = selectedBackupFile,
            isBackupValid = selectedBackupFile?.run {
                _backupService.validateBackup(this.uri) && this.metadata != null
            } ?: false,
            importFinished = importFinished
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT),
        initialValue = HomeState()
    )

    override fun dispatch(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.ImportBackup -> importBackup()
            is HomeIntent.DismissImportBackup -> _selectedBackupFile.update { null }
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
                _importFinished.update { true }
            }
        }
    }
}

private const val DEFAULT_TIMEOUT = 5000L
