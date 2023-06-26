package app.ministrylogbook.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.data.Design
import app.ministrylogbook.data.MonthlyInformationRepository
import app.ministrylogbook.data.Role
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.notifications.ReminderManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class SettingsViewModel(
    private val _settingsDataStore: SettingsService,
    private val _monthlyInformationRepository: MonthlyInformationRepository,
    private val _reminderManager: ReminderManager,
) : ViewModel() {
    private val _currentMonth = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private val _monthlyInfo = _monthlyInformationRepository.getOfMonth(_currentMonth)

    val name = _settingsDataStore.name.stateIn(
        scope = viewModelScope,
        initialValue = "",
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val design = _settingsDataStore.design.stateIn(
        scope = viewModelScope,
        initialValue = Design.System,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val role = _settingsDataStore.role.stateIn(
        scope = viewModelScope,
        initialValue = Role.Publisher,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val startOfPioneering = _settingsDataStore.startOfPioneering.stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val roleGoal = _settingsDataStore.roleGoal.stateIn(
        scope = viewModelScope,
        initialValue = 0,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val manuallySetGoal = _monthlyInfo.map { it.goal }.stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val goal = roleGoal.combine(manuallySetGoal) { rg, msg -> msg ?: rg }.stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val precisionMode = _settingsDataStore.precisionMode.stateIn(
        scope = viewModelScope,
        initialValue = false,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val sendReportReminder = _settingsDataStore.sendReportReminder.stateIn(
        scope = viewModelScope,
        initialValue = false,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )

    fun setPioneerSince(date: LocalDate) = viewModelScope.launch {
        _settingsDataStore.setPioneerSince(date)
    }

    fun setPrecisionMode(value: Boolean) = viewModelScope.launch {
        _settingsDataStore.setPrecisionMode(value)
    }

    fun setSendReportReminders(value: Boolean) = viewModelScope.launch {
        if (value) {
            _reminderManager.scheduleReminder()
        } else {
            _reminderManager.cancelReminder()
        }
        _settingsDataStore.setSendReportReminders(value)
    }

    fun setGoal(value: Int?) = viewModelScope.launch {
        val rg = roleGoal.firstOrNull()
        val role = role.firstOrNull()

        if (value == null || (value == rg && role != Role.Publisher)) {
            resetGoal()
            return@launch
        }
        _monthlyInfo.firstOrNull()?.let {
            _monthlyInformationRepository.save(it.copy(goal = value))
        }
    }

    fun resetGoal() = viewModelScope.launch {
        _monthlyInfo.firstOrNull()?.let {
            _monthlyInformationRepository.save(it.copy(goal = null))
        }
    }

    fun setDesign(d: Design) = viewModelScope.launch {
        _settingsDataStore.setDesign(d)
    }

    fun setRole(r: Role) = viewModelScope.launch {
        val msg = manuallySetGoal.firstOrNull()
        if (msg == role.firstOrNull()?.goal && role.firstOrNull() != Role.Publisher) {
            resetGoal()
        }
        if (isAnyPioneer(r) && !isAnyPioneer(role.value)) {
            setPioneerSince(_currentMonth)
        } else if (!isAnyPioneer(r)) {
            resetPioneerSince()
        }
        _settingsDataStore.setRole(r)
    }

    fun setName(text: String) = viewModelScope.launch {
        _settingsDataStore.setName(text)
    }

    private fun isAnyPioneer(role: Role) = role == Role.RegularPioneer || role == Role.SpecialPioneer

    private fun resetPioneerSince() = viewModelScope.launch {
        _settingsDataStore.setPioneerSince(null)
    }
}

private const val DEFAULT_TIMEOUT = 5000L
