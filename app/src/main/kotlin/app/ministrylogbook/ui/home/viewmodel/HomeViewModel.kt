package app.ministrylogbook.ui.home.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.R
import app.ministrylogbook.data.BibleStudy
import app.ministrylogbook.data.BibleStudyRepository
import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryRepository
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.data.MonthlyInformation
import app.ministrylogbook.data.MonthlyInformationRepository
import app.ministrylogbook.data.Role
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.shared.IntentViewModel
import app.ministrylogbook.shared.Time
import app.ministrylogbook.shared.services.BackupService
import app.ministrylogbook.shared.utilities.ministryTimeSum
import app.ministrylogbook.shared.utilities.timeSum
import app.ministrylogbook.ui.home.backup.viewmodel.BackupFile
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.runningFold
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
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter

sealed class HomeIntent {
    data class TransferToTextMonth(val minutes: Int) : HomeIntent()
    data class UndoTransfer(val transfer: Entry) : HomeIntent()
    data class TransferFromLastMonth(val minutes: Int) : HomeIntent()
    data class CreateBibleStudy(val name: String) : HomeIntent()
    data class DeleteBibleStudy(val bibleStudy: BibleStudy) : HomeIntent()
    data class CheckBibleStudy(val bibleStudy: BibleStudy) : HomeIntent()
    data class UncheckBibleStudy(val bibleStudy: BibleStudy) : HomeIntent()
    data object ImportBackup : HomeIntent()
    data object DismissImportBackup : HomeIntent()
    data object DismissBibleStudyHint : HomeIntent()
    data object DismissSendReportHint : HomeIntent()
    data object PartyFinished : HomeIntent()
}

data class HomeState(
    val month: LocalDate,
    val name: String = "",
    val goal: Int? = null,
    val hasGoal: Boolean? = null,
    val roleGoal: Int? = null,
    val yearlyGoal: Int = 1,
    val role: Role = Role.Publisher,
    val entries: List<Entry> = emptyList(),
    val entriesInServiceYear: List<Entry> = emptyList(),
    val bibleStudies: List<BibleStudy> = emptyList(),
    val restLastMonth: Time = Time.Empty,
    val transferred: List<Entry> = emptyList(),
    val rest: Time = Time.Empty,
    val beginOfPioneeringInServiceYear: LocalDate? = null,
    val selectedBackupFile: BackupFile? = null,
    val isBackupValid: Boolean = false,
    val latestEntry: Entry? = null,
    val importFinished: Boolean = false,
    val monthlyInformation: MonthlyInformation = MonthlyInformation(),
    val lastMonthReportSent: Boolean? = null,
    val parties: List<Party> = emptyList()
)

data class History<T>(val previous: T?, val current: T)

class HomeViewModel(
    val month: LocalDate,
    private val _uri: Uri? = null,
    private val _application: Application,
    private val _entryRepository: EntryRepository,
    private val _backupService: BackupService,
    private val _bibleStudyRepository: BibleStudyRepository,
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
    private val _monthlyInformation = _monthlyInformationRepository.getOfMonth(month)
    private val _lastMonthMonthlyInformation = _monthlyInformationRepository.getOfMonth(_lastMonth)
    private val _entries = _entryRepository.getAllOfMonth(month)
    private val _entriesHistory = _entries.runningFold(
        initial = null as (History<List<Entry>>?),
        operation = { previous, new -> History(previous?.current, new) }
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _entriesInServiceYear = _beginOfPioneeringInServiceYear.flatMapLatest {
        _entryRepository.getAllInRange(it, _serviceYearEnd)
    }
    private val _transferred =
        _entryRepository.getTransferredFrom(month).map { transferred -> transferred.filter { it.time.isNotEmpty } }
    private val _roleGoal = settingsService.roleGoal
    private val _manuallySetGoal = _monthlyInformation.map { it.goal }
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
        (rl ?: 0) * beginOfPioneering.monthsUntil(lastMonthInServiceYear)
    }
    private val _lastMonthEntries = _entryRepository.getAllOfMonth(_lastMonth)
    private val _restLastMonth = _lastMonthEntries.transform {
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
    private val _bibleStudies = _bibleStudyRepository.getAllOfMonth(month)

    private val _parties = combine(_goal, _entriesHistory) { goal, entries ->
        if (entries?.previous == null || (goal != null && entries.previous.timeSum().hours >= goal)) {
            return@combine false
        }
        val time = entries.current.timeSum()
        goal != null && time.hours >= goal
    }.transform { isGoalReachedJustNow ->
        val result = if (isGoalReachedJustNow) {
            val party = Party(
                speed = 10f,
                maxSpeed = 30f,
                damping = 0.9f,
                angle = Angle.RIGHT - 45,
                spread = Spread.SMALL * 2,
                emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(30),
                position = Position.Relative(0.0, 0.3)
            )
            listOf(party, party.copy(angle = party.angle - 90, position = Position.Relative(1.0, 0.3)))
        } else {
            emptyList()
        }
        emit(result)
    }

    init {
        viewModelScope.launch {
            transferBibleStudies()
        }
    }

    private fun dismissBibleStudyHint() {
        viewModelScope.launch {
            val monthlyInfo = state.value.monthlyInformation
            val newMonthlyInfo = monthlyInfo.copy(dismissedBibleStudiesHint = true)
            _monthlyInformationRepository.save(newMonthlyInfo)
        }
    }

    private fun deleteBibleStudy(bibleStudy: BibleStudy) {
        viewModelScope.launch {
            _bibleStudyRepository.delete(bibleStudy)
        }
    }

    private fun uncheckBibleStudy(bibleStudy: BibleStudy) {
        viewModelScope.launch {
            _bibleStudyRepository.save(bibleStudy.copy(checked = false))
        }
    }

    private fun checkBibleStudy(bibleStudy: BibleStudy) {
        viewModelScope.launch {
            _bibleStudyRepository.save(bibleStudy.copy(checked = true))
        }
    }

    private fun createBibleStudy(name: String) {
        viewModelScope.launch {
            _bibleStudyRepository.save(BibleStudy(name = name, month = month))
        }
    }

    private suspend fun transferBibleStudies() {
        val monthlyInfo = _monthlyInformation.first()
        val isTransferred = monthlyInfo.bibleStudiesTransferred
        if (isTransferred) {
            return
        }
        val lastMonth = month.minus(DatePeriod(months = 1))
        _bibleStudyRepository.transfer(lastMonth, month)
        _monthlyInformationRepository.save(monthlyInfo.copy(bibleStudiesTransferred = true))
    }

    private fun transferTimeToNextMonth(minutes: Int) {
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

    private fun undoTransferTime(transfer: Entry) {
        viewModelScope.launch {
            _entryRepository.delete(transfer)
        }
    }

    /** Transferring 0 minutes dismisses the message and won't show a history item. */
    private fun transferTimeFromLastMonth(minutes: Int) {
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

    private fun dismissSendReportHint() {
        viewModelScope.launch {
            val monthlyInfo = _lastMonthMonthlyInformation.first()
            val newMonthlyInfo = monthlyInfo.copy(reportSent = true)
            _monthlyInformationRepository.save(newMonthlyInfo)
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
        _monthlyInformation,
        _lastMonthEntries,
        _lastMonthMonthlyInformation,
        _parties
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        HomeState(
            month = month,
            name = values[0] as String,
            goal = values[1] as Int?,
            hasGoal = values[2] as Boolean,
            roleGoal = values[3] as Int?,
            yearlyGoal = values[4] as Int,
            role = values[5] as Role,
            entries = values[6] as List<Entry>,
            entriesInServiceYear = values[7] as List<Entry>,
            bibleStudies = values[8] as List<BibleStudy>,
            restLastMonth = values[9] as Time,
            transferred = values[10] as List<Entry>,
            rest = values[11] as Time,
            beginOfPioneeringInServiceYear = values[12] as LocalDate?,
            monthlyInformation = values[13] as MonthlyInformation,
            lastMonthReportSent = (values[14] as List<Entry>).isEmpty() || (values[15] as MonthlyInformation).reportSent,
            parties = values[16] as List<Party>
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT),
        initialValue = HomeState(month)
    )

    override fun dispatch(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.TransferToTextMonth -> transferTimeToNextMonth(intent.minutes)
            is HomeIntent.UndoTransfer -> undoTransferTime(intent.transfer)
            is HomeIntent.TransferFromLastMonth -> transferTimeFromLastMonth(intent.minutes)
            is HomeIntent.ImportBackup -> importBackup()
            is HomeIntent.DismissImportBackup -> _selectedBackupFile.update { null }
            is HomeIntent.CreateBibleStudy -> createBibleStudy(intent.name)
            is HomeIntent.CheckBibleStudy -> checkBibleStudy(intent.bibleStudy)
            is HomeIntent.UncheckBibleStudy -> uncheckBibleStudy(intent.bibleStudy)
            is HomeIntent.DeleteBibleStudy -> deleteBibleStudy(intent.bibleStudy)
            is HomeIntent.DismissBibleStudyHint -> dismissBibleStudyHint()
            is HomeIntent.DismissSendReportHint -> dismissSendReportHint()
            is HomeIntent.PartyFinished -> {}
        }
    }
}

private const val DEFAULT_TIMEOUT = 5000L
