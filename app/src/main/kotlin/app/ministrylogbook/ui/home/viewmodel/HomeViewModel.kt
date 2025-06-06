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
import app.ministrylogbook.shared.sum
import app.ministrylogbook.shared.toTime
import app.ministrylogbook.shared.utilities.lastDayOfMonth
import app.ministrylogbook.shared.utilities.ministryTimeSum
import app.ministrylogbook.shared.utilities.splitIntoMonths
import app.ministrylogbook.shared.utilities.theocraticAssignmentTimeSum
import app.ministrylogbook.shared.utilities.theocraticSchoolTimeSum
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
    data object MonthlyPartyFinished : HomeIntent()
    data object YearlyPartyFinished : HomeIntent()
}

data class HomeState(
    val month: LocalDate,
    val goal: Int? = null,
    val hasGoal: Boolean? = null,
    val roleGoal: Int? = null,
    val yearlyGoal: Int = 1,
    val role: Role = Role.Publisher,
    val entries: List<Entry> = emptyList(),
    val entriesLastMonth: List<Entry> = emptyList(),
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
    val monthlyParties: List<Party> = emptyList(),
    val yearlyProgress: Time = Time.Empty,
    val yearlyParties: List<Party> = emptyList()
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
) : AndroidViewModel(_application),
    IntentViewModel<HomeState, HomeIntent> {

    private val selectedBackupFile =
        MutableStateFlow(_uri?.run { BackupFile(this, _backupService.getBackupMetadata(_uri)) })

    private val importFinished = MutableStateFlow(false)

    private val pioneerSince = settingsService.pioneerSince
    private val serviceYearBegin = when {
        // special case after corona pandemic; pioneering began in march
        month.year == 2023 && month.monthNumber < 9 -> LocalDate(month.year, 3, 1)
        month.monthNumber >= 9 -> LocalDate(month.year, 9, 1)
        else -> LocalDate(month.year - 1, 9, 1)
    }
    private val beginOfPioneeringInServiceYear = pioneerSince.map { pioneerSince ->
        if (pioneerSince != null && pioneerSince >= serviceYearBegin) {
            pioneerSince
        } else {
            serviceYearBegin
        }
    }
    private val lastMonth = month.minus(DatePeriod(months = 1))
    private val monthlyInformation = _monthlyInformationRepository.getOfMonth(month)
    private val lastMonthMonthlyInformation = _monthlyInformationRepository.getOfMonth(lastMonth)
    private val entries = _entryRepository.getAllOfMonth(month)
    private val entriesHistory = entries.runningFold(
        initial = null as (History<List<Entry>>?),
        operation = { previous, new -> History(previous?.current, new) }
    )
    private val entriesLastMonth = _entryRepository.getAllOfMonth(lastMonth)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val entriesInServiceYear = beginOfPioneeringInServiceYear.flatMapLatest {
        _entryRepository.getAllInRange(it, month.lastDayOfMonth)
    }
    private val transferred =
        _entryRepository.getTransferredFrom(month).map { transferred -> transferred.filter { it.time.isNotEmpty } }
    private val roleGoal = settingsService.roleGoal
    private val manuallySetGoal = monthlyInformation.map { it.goal }
    private val goal = roleGoal.combine(manuallySetGoal) { rg, msg -> msg ?: rg }
    private val hasGoal = combine(settingsService.role, manuallySetGoal) { role, manuallySetGoal ->
        manuallySetGoal != null || role != Role.Publisher
    }
    private val yearlyGoal = roleGoal.combine(beginOfPioneeringInServiceYear) { rl, beginOfPioneering ->
        val lastMonthInServiceYear = when {
            serviceYearBegin.monthNumber == 9 -> serviceYearBegin + DatePeriod(months = 12)
            serviceYearBegin.monthNumber >= 9 -> LocalDate(serviceYearBegin.year + 1, 9, 1)
            else -> LocalDate(serviceYearBegin.year, 9, 1)
        }
        (rl ?: 0) * beginOfPioneering.monthsUntil(lastMonthInServiceYear)
    }
    private val lastMonthEntries = _entryRepository.getAllOfMonth(lastMonth)
    private val restLastMonth = lastMonthEntries.transform {
        val lastMonthTime = it.ministryTimeSum()
        if (!lastMonthTime.isNegative) {
            emit(Time(hours = 0, minutes = lastMonthTime.minutes))
        } else {
            emit(Time.Empty)
        }
    }
    private val rest = entries.combine(transferred) { entries, transferred ->
        val result = entries.ministryTimeSum() - transferred.ministryTimeSum()
        return@combine if (!result.isNegative) {
            result
        } else {
            Time.Empty
        }
    }
    private val bibleStudies = _bibleStudyRepository.getAllOfMonth(month)
    private val maxHoursWithCredit = roleGoal.map { Time(it?.plus(5) ?: 0, 0) }
    private val yearlyProgress =
        entriesInServiceYear.combine(maxHoursWithCredit) { entriesInServiceYear, maxHoursWithCredit ->
            entriesInServiceYear.splitIntoMonths().map {
                val ministryTimeSum = it.ministryTimeSum().hours.toTime()
                val theocraticSchoolTimeSum = it.theocraticSchoolTimeSum().hours.toTime()
                val theocraticAssignmentTimeSum = it.theocraticAssignmentTimeSum().hours.toTime()
                val max = maxOf(ministryTimeSum, maxHoursWithCredit)
                minOf(max, ministryTimeSum + theocraticAssignmentTimeSum) + theocraticSchoolTimeSum
            }.sum()
        }
    private val yearlyProgressHistory = yearlyProgress.runningFold(
        initial = null as (History<Time>?),
        operation = { previous, new -> History(previous?.current, new) }
    )

    private val isYearlyPartyFinished = MutableStateFlow(false)
    private val yearlyParties =
        combine(yearlyGoal, yearlyProgressHistory, isYearlyPartyFinished) { goal, progress, isFinished ->
            if (isFinished || progress?.previous == null || (goal != 0 && progress.previous.hours >= goal)) {
                return@combine emptyList()
            }
            val isYearlyGoalReached = goal != 0 && progress.current.hours >= goal
            if (isYearlyGoalReached) {
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
        }

    private val isMonthlyPartyFinished = MutableStateFlow(false)
    private val monthlyParties =
        combine(goal, entriesHistory, isMonthlyPartyFinished, isYearlyPartyFinished) {
                goal,
                entries,
                isFinished,
                isYearFinished
            ->
            if (isFinished ||
                isYearFinished ||
                entries?.previous == null ||
                (goal != null && entries.previous.timeSum().hours >= goal)
            ) {
                return@combine emptyList()
            }
            val time = entries.current.timeSum()
            val isMonthlyGoalReached = goal != null && time.hours >= goal
            if (isMonthlyGoalReached) {
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
        }

    init {
        viewModelScope.launch {
            transferBibleStudies()
        }
        viewModelScope.launch {
            entries.collect {
                isMonthlyPartyFinished.value = false
            }
        }
        viewModelScope.launch {
            entriesInServiceYear.collect {
                isYearlyPartyFinished.value = false
            }
        }
    }

    private fun finishYearlyParty() {
        isYearlyPartyFinished.value = true
    }

    private fun finishMonthlyParty() {
        isMonthlyPartyFinished.value = true
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
        val monthlyInfo = monthlyInformation.first()
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
                importFinished.update { true }
            }
        }
    }

    private fun dismissSendReportHint() {
        viewModelScope.launch {
            val monthlyInfo = lastMonthMonthlyInformation.first()
            val newMonthlyInfo = monthlyInfo.copy(reportSent = true)
            _monthlyInformationRepository.save(newMonthlyInfo)
        }
    }

    override val state = combine(
        goal,
        hasGoal,
        roleGoal,
        yearlyGoal,
        settingsService.role,
        entries,
        entriesInServiceYear,
        bibleStudies,
        restLastMonth,
        transferred,
        rest,
        beginOfPioneeringInServiceYear,
        monthlyInformation,
        lastMonthEntries,
        lastMonthMonthlyInformation,
        monthlyParties,
        entriesLastMonth,
        yearlyProgress,
        yearlyParties
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        HomeState(
            month = month,
            goal = values[0] as Int?,
            hasGoal = values[1] as Boolean,
            roleGoal = values[2] as Int?,
            yearlyGoal = values[3] as Int,
            role = values[4] as Role,
            entries = values[5] as List<Entry>,
            entriesInServiceYear = values[6] as List<Entry>,
            bibleStudies = values[7] as List<BibleStudy>,
            restLastMonth = values[8] as Time,
            transferred = values[9] as List<Entry>,
            rest = values[10] as Time,
            beginOfPioneeringInServiceYear = values[11] as LocalDate?,
            monthlyInformation = values[12] as MonthlyInformation,
            lastMonthReportSent = (values[13] as List<Entry>).isEmpty() ||
                (values[14] as MonthlyInformation).reportSent,
            monthlyParties = values[15] as List<Party>,
            entriesLastMonth = values[16] as List<Entry>,
            yearlyProgress = values[17] as Time,
            yearlyParties = values[18] as List<Party>
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
            is HomeIntent.DismissImportBackup -> selectedBackupFile.update { null }
            is HomeIntent.CreateBibleStudy -> createBibleStudy(intent.name)
            is HomeIntent.CheckBibleStudy -> checkBibleStudy(intent.bibleStudy)
            is HomeIntent.UncheckBibleStudy -> uncheckBibleStudy(intent.bibleStudy)
            is HomeIntent.DeleteBibleStudy -> deleteBibleStudy(intent.bibleStudy)
            is HomeIntent.DismissBibleStudyHint -> dismissBibleStudyHint()
            is HomeIntent.DismissSendReportHint -> dismissSendReportHint()
            is HomeIntent.MonthlyPartyFinished -> finishMonthlyParty()
            is HomeIntent.YearlyPartyFinished -> finishYearlyParty()
        }
    }
}

private const val DEFAULT_TIMEOUT = 5000L
