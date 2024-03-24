package app.ministrylogbook.ui.home.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.R
import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryRepository
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.data.MonthlyInformation
import app.ministrylogbook.data.MonthlyInformationRepository
import app.ministrylogbook.data.MonthlyInformationWithStudies
import app.ministrylogbook.data.Role
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.data.Study
import app.ministrylogbook.data.StudyRepository
import app.ministrylogbook.shared.IntentViewModel
import app.ministrylogbook.shared.Time
import app.ministrylogbook.shared.services.BackupService
import app.ministrylogbook.shared.utilities.ministryTimeSum
import app.ministrylogbook.ui.home.backup.viewmodel.BackupFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus

sealed class HomeIntent {
    data class TransferToTextMonth(val minutes: Int) : HomeIntent()
    data class UndoTransfer(val transfer: Entry) : HomeIntent()
    data class TransferFromLastMonth(val minutes: Int) : HomeIntent()
    data class CreateBibleStudy(val name: String) : HomeIntent()
    data class DeleteBibleStudy(val study: Study) : HomeIntent()
    data class CheckBibleStudy(val id: Int) : HomeIntent()
    data class UncheckBibleStudy(val id: Int) : HomeIntent()
    data object ImportBackup : HomeIntent()
    data object DismissImportBackup : HomeIntent()
    data object DismissBibleStudyHint : HomeIntent()
}

data class HomeState(
    val month: LocalDate,
    val name: String = "",
    val goal: Int? = null,
    val hasGoal: Boolean? = null,
    val roleGoal: Int = 1,
    val yearlyGoal: Int = 1,
    val role: Role = Role.Publisher,
    val entries: List<Entry> = emptyList(),
    val entriesInServiceYear: List<Entry> = emptyList(),
    val bibleStudies: List<Study> = emptyList(),
    val restLastMonth: Time = Time.Empty,
    val transferred: List<Entry> = emptyList(),
    val rest: Time = Time.Empty,
    val beginOfPioneeringInServiceYear: LocalDate? = null,
    val selectedBackupFile: BackupFile? = null,
    val isBackupValid: Boolean = false,
    val latestEntry: Entry? = null,
    val importFinished: Boolean = false,
    val monthlyInformation: MonthlyInformationWithStudies = MonthlyInformationWithStudies(
        MonthlyInformation(),
        emptyList()
    )
)

class HomeViewModel(
    val month: LocalDate,
    private val _uri: Uri? = null,
    private val _application: Application,
    private val _entryRepository: EntryRepository,
    private val _backupService: BackupService,
    private val _studyRepository: StudyRepository,
    private val _monthlyInformationRepository: MonthlyInformationRepository,
    settingsService: SettingsService
) : AndroidViewModel(_application), IntentViewModel<HomeState, HomeIntent> {

    private val _selectedBackupFile =
        MutableStateFlow(_uri?.run { BackupFile(this, _backupService.getBackupMetadata(_uri)) })

    private val _importFinished = MutableStateFlow(false)

    private val _pioneerSince = settingsService.pioneerSince
    private val _serviceYearBegin = when {
        // special case after corona pandemic; pioneering began in march
        month.year == 2023 && month.monthNumber < 9 -> LocalDate(month.year, 3, 1)
        month.monthNumber >= 9 -> LocalDate(month.year, 9, 1)
        else -> LocalDate(month.year - 1, 9, 1)
    }
    private val _serviceYearEnd = if (_serviceYearBegin.monthNumber >= 9) {
        LocalDate(_serviceYearBegin.year + 1, 8, 31)
    } else {
        LocalDate(_serviceYearBegin.year, 8, 31)
    }
    private val _beginOfPioneeringInServiceYear = _pioneerSince.map { pioneerSince ->
        if (pioneerSince != null && pioneerSince >= _serviceYearBegin) {
            pioneerSince
        } else {
            _serviceYearBegin
        }
    }
    private val _lastMonth = month.minus(DatePeriod(months = 1))
    private val _monthlyInformationWithStudies = _monthlyInformationRepository.getOfMonth(month)
    private val _entries = _entryRepository.getAllOfMonth(month)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _entriesInServiceYear = _beginOfPioneeringInServiceYear.flatMapLatest {
        _entryRepository.getAllInRange(it, _serviceYearEnd)
    }
    private val _transferred =
        _entryRepository.getTransferredFrom(month).map { transferred -> transferred.filter { it.time.isNotEmpty } }
    private val _roleGoal = settingsService.roleGoal
    private val _manuallySetGoal = _monthlyInformationWithStudies.map { it.info.goal }
    private val _goal = _roleGoal.combine(_manuallySetGoal) { rg, msg -> msg ?: rg }
    private val _hasGoal = combine(settingsService.role, _manuallySetGoal) { role, manuallySetGoal ->
        manuallySetGoal != null || role != Role.Publisher
    }
    private val _yearlyGoal = _roleGoal.combine(_beginOfPioneeringInServiceYear) { rl, beginOfPioneering ->
        val lastMonthInServiceYear = when {
            _serviceYearBegin.monthNumber == 9 -> _serviceYearBegin + DatePeriod(months = 12)
            _serviceYearBegin.monthNumber >= 9 -> LocalDate(_serviceYearBegin.year + 1, 9, 1)
            else -> LocalDate(_serviceYearBegin.year, 9, 1)
        }
        rl * beginOfPioneering.monthsUntil(lastMonthInServiceYear)
    }
    private val _restLastMonth = _entryRepository.getAllOfMonth(_lastMonth).transform {
        val lastMonthTime = it.ministryTimeSum()
        if (!lastMonthTime.isNegative) {
            emit(Time(hours = 0, minutes = lastMonthTime.minutes))
        } else {
            emit(Time.Empty)
        }
    }
    private val _rest = _entries.combine(_transferred) { entries, transferred ->
        val result = entries.ministryTimeSum() - transferred.ministryTimeSum()
        return@combine if (!result.isNegative) {
            result
        } else {
            Time.Empty
        }
    }
    private val _bibleStudies = _studyRepository.getAll()

    private fun dismissBibleStudyHint() {
        viewModelScope.launch {
            val monthlyInfo = state.value.monthlyInformation.info
            val newMonthlyInfo = monthlyInfo.copy(dismissedBibleStudiesHint = true)
            _monthlyInformationRepository.save(newMonthlyInfo)
        }
    }

    private fun deleteBibleStudy(study: Study) {
        viewModelScope.launch {
            _studyRepository.delete(study)
        }
    }

    private fun uncheckBibleStudy(id: Int) {
        viewModelScope.launch {
            val monthlyInfo = state.value.monthlyInformation.info
            _monthlyInformationRepository.removeCheckedStudy(id, monthlyInfo.id)
        }
    }

    private fun checkBibleStudy(id: Int) {
        viewModelScope.launch {
            val monthlyInfo = state.value.monthlyInformation.info
            _monthlyInformationRepository.addCheckedStudy(id, monthlyInfo.id)
        }
    }

    private fun createBibleStudy(name: String) {
        viewModelScope.launch {
            val id = _studyRepository.save(Study(name = name))
            val monthlyInfo = state.value.monthlyInformation.info
            _monthlyInformationRepository.addCheckedStudy(id, monthlyInfo.id)
        }
    }

    private fun transferToNextMonth(minutes: Int) {
        val firstOfMonth = LocalDate(month.year, month.month, 1)
        val nextMonth = firstOfMonth + DatePeriod(months = 1)
        val lastOfMonth = nextMonth - DatePeriod(days = 1)
        val transfer = Entry(
            datetime = nextMonth.atTime(0, 0),
            minutes = minutes,
            type = EntryType.Transfer,
            transferredFrom = lastOfMonth.atTime(0, 0)
        )
        viewModelScope.launch {
            _entryRepository.save(transfer)
        }
    }

    private fun undoTransfer(transfer: Entry) {
        viewModelScope.launch {
            _entryRepository.delete(transfer)
        }
    }

    /** Transferring 0 minutes dismisses the message and won't show a history item. */
    private fun transferFromLastMonth(minutes: Int) {
        val firstOfMonth = LocalDate(month.year, month.month, 1)
        val lastMonth = firstOfMonth - DatePeriod(days = 1)
        val transfer = Entry(
            datetime = firstOfMonth.atTime(0, 0),
            minutes = minutes,
            type = EntryType.Transfer,
            transferredFrom = lastMonth.atTime(0, 0)
        )
        viewModelScope.launch {
            _entryRepository.save(transfer)
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

    override val state = combine(
        settingsService.name,
        _goal,
        _hasGoal,
        _roleGoal,
        _yearlyGoal,
        settingsService.role,
        _entries,
        _entriesInServiceYear,
        _bibleStudies,
        _restLastMonth,
        _transferred,
        _rest,
        _beginOfPioneeringInServiceYear,
        _monthlyInformationWithStudies
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        HomeState(
            month = month,
            name = values[0] as String,
            goal = values[1] as Int,
            hasGoal = values[2] as Boolean,
            roleGoal = values[3] as Int,
            yearlyGoal = values[4] as Int,
            role = values[5] as Role,
            entries = values[6] as List<Entry>,
            entriesInServiceYear = values[7] as List<Entry>,
            bibleStudies = values[8] as List<Study>,
            restLastMonth = values[9] as Time,
            transferred = values[10] as List<Entry>,
            rest = values[11] as Time,
            beginOfPioneeringInServiceYear = values[12] as LocalDate?,
            monthlyInformation = values[13] as MonthlyInformationWithStudies
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT),
        initialValue = HomeState(month)
    )

    override fun dispatch(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.TransferToTextMonth -> transferToNextMonth(intent.minutes)
            is HomeIntent.UndoTransfer -> undoTransfer(intent.transfer)
            is HomeIntent.TransferFromLastMonth -> transferFromLastMonth(intent.minutes)
            is HomeIntent.ImportBackup -> importBackup()
            is HomeIntent.DismissImportBackup -> _selectedBackupFile.update { null }
            is HomeIntent.CreateBibleStudy -> createBibleStudy(intent.name)
            is HomeIntent.CheckBibleStudy -> checkBibleStudy(intent.id)
            is HomeIntent.UncheckBibleStudy -> uncheckBibleStudy(intent.id)
            is HomeIntent.DeleteBibleStudy -> deleteBibleStudy(intent.study)
            is HomeIntent.DismissBibleStudyHint -> dismissBibleStudyHint()
        }
    }
}

private const val DEFAULT_TIMEOUT = 5000L
