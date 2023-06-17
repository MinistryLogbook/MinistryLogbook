@file:JvmName("OverviewViewModelKt")

package app.ministrylogbook.ui.home.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryRepository
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.data.MonthlyInformationRepository
import app.ministrylogbook.data.Role
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.shared.Time
import app.ministrylogbook.shared.ministryTimeSum
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus

class OverviewViewModel(
    val month: LocalDate,
    application: Application,
    settingsDataStore: SettingsService,
    private val _entryRepository: EntryRepository,
    monthlyInformationRepository: MonthlyInformationRepository
) : AndroidViewModel(application) {

    private val _pioneerSince = settingsDataStore.startOfPioneering
    private val _serviceYearBegin = when {
        // special case after corona pandemic; pioneering began in march
        month.year == 2023 && month.monthNumber <= 9 -> LocalDate(month.year, 3, 1)
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
    private val _monthlyInformation = monthlyInformationRepository.getOfMonth(month)
    private val _entries = _entryRepository.getAllOfMonth(month)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _entriesInServiceYear = _beginOfPioneeringInServiceYear.flatMapLatest {
        _entryRepository.getAllInRange(it, _serviceYearEnd)
    }
    private val _restLastMonth = _entryRepository.getAllOfMonth(_lastMonth).transform<List<Entry>, Time> {
        val lastMonthTime = it.ministryTimeSum()
        if (!lastMonthTime.isNegative) {
            Time(hours = 0, minutes = lastMonthTime.minutes)
        } else {
            Time.Empty
        }
    }
    private val _transferred = _entryRepository.getTransferredFrom(month)
    private val _roleGoal = settingsDataStore.roleGoal
    private val _manuallySetGoal = _monthlyInformation.map { it.goal }
    private val _goal = _roleGoal.combine(_manuallySetGoal) { rg, msg -> msg ?: rg }

    val name = settingsDataStore.name.stateIn(
        scope = viewModelScope,
        initialValue = "",
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val goal = _goal.stateIn(
        scope = viewModelScope,
        initialValue = 1,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val roleGoal = _roleGoal.stateIn(
        scope = viewModelScope,
        initialValue = 1,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val yearlyGoal = _roleGoal.combine(_beginOfPioneeringInServiceYear) { rl, beginOfPioneeringInServiceYear ->
        val lastMonthInServiceYear = when {
            _serviceYearBegin.monthNumber == 9 -> _serviceYearBegin + DatePeriod(months = 12)
            _serviceYearBegin.monthNumber >= 9 -> LocalDate(_serviceYearBegin.year + 1, 9, 1)
            else -> LocalDate(_serviceYearBegin.year, 9, 1)
        }
        rl * beginOfPioneeringInServiceYear.monthsUntil(lastMonthInServiceYear)
    }.stateIn(
        scope = viewModelScope,
        initialValue = 1,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val role = settingsDataStore.role.stateIn(
        scope = viewModelScope,
        initialValue = Role.Publisher,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val entries = _entries.stateIn(
        scope = viewModelScope,
        initialValue = listOf(),
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val entriesInServiceYear = _entriesInServiceYear.stateIn(
        scope = viewModelScope,
        initialValue = listOf(),
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val bibleStudies = _monthlyInformation.map { it.bibleStudies ?: 0 }.stateIn(
        scope = viewModelScope,
        initialValue = 0,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val restLastMonth = _restLastMonth.stateIn(
        scope = viewModelScope,
        initialValue = Time.Empty,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val transferred = _transferred.stateIn(
        scope = viewModelScope,
        initialValue = listOf(),
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val rest = _entries.combine(_transferred) { entries, transferred ->
        val result = entries.ministryTimeSum() - transferred.ministryTimeSum()
        if (!result.isNegative) {
            result
        } else {
            Time.Empty
        }
    }.stateIn(
        scope = viewModelScope,
        initialValue = Time.Empty,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val pioneerSince = _pioneerSince.stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )

    fun transferToNextMonth(minutes: Int) {
        val firstOfMonth = LocalDate(month.year, month.month, 1)
        val nextMonth = firstOfMonth + DatePeriod(months = 1)
        val lastOfMonth = nextMonth - DatePeriod(days = 1)
        val transfer = Entry(
            datetime = nextMonth,
            minutes = minutes,
            type = EntryType.Transfer,
            transferredFrom = lastOfMonth
        )
        viewModelScope.launch {
            _entryRepository.save(transfer)
        }
    }

    fun undoTransfer(transfer: Entry) {
        viewModelScope.launch {
            _entryRepository.delete(transfer)
        }
    }

    /** Transferring 0 minutes dismisses the message and won't show a history item. */
    fun transferFromLastMonth(minutes: Int) {
        val firstOfMonth = LocalDate(month.year, month.month, 1)
        val lastMonth = firstOfMonth - DatePeriod(days = 1)
        val transfer = Entry(
            datetime = firstOfMonth,
            minutes = minutes,
            type = EntryType.Transfer,
            transferredFrom = lastMonth
        )
        viewModelScope.launch {
            _entryRepository.save(transfer)
        }
    }
}

private const val DEFAULT_TIMEOUT = 5000L
