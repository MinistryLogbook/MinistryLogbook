package app.ministrylogbook.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.data.Design
import app.ministrylogbook.data.MonthlyInformationRepository
import app.ministrylogbook.data.Role
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.shared.services.ReminderManager
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
    private val _settingsService: SettingsService,
    private val _monthlyInformationRepository: MonthlyInformationRepository,
    private val _reminderManager: ReminderManager
) : ViewModel() {
    private val currentMonth = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private val monthlyInfo = _monthlyInformationRepository.getOfMonth(currentMonth)

    val name = _settingsService.name.stateIn(
        scope = viewModelScope,
        initialValue = "",
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val design = _settingsService.design.stateIn(
        scope = viewModelScope,
        initialValue = Design.System,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val useSystemColors = _settingsService.useSystemColors.stateIn(
        scope = viewModelScope,
        initialValue = true,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val role = _settingsService.role.stateIn(
        scope = viewModelScope,
        initialValue = Role.Publisher,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val pioneerSince = _settingsService.pioneerSince.stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val roleGoal = _settingsService.roleGoal.stateIn(
        scope = viewModelScope,
        initialValue = 0,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val manuallySetGoal = monthlyInfo.map { it.goal }.stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val goal = roleGoal.combine(manuallySetGoal) { rg, msg -> msg ?: rg }.stateIn(
        scope = viewModelScope,
        initialValue = null,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val precisionMode = _settingsService.precisionMode.stateIn(
        scope = viewModelScope,
        initialValue = false,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val sendReportReminder = _settingsService.sendReportReminder.stateIn(
        scope = viewModelScope,
        initialValue = false,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )

    fun setPioneerSince(date: LocalDate) = viewModelScope.launch {
        _settingsService.setPioneerSince(date)
    }

    fun setPrecisionMode(value: Boolean) = viewModelScope.launch {
        _settingsService.setPrecisionMode(value)
    }

    fun setSendReportReminders(value: Boolean) = viewModelScope.launch {
        if (value) {
            _reminderManager.scheduleReminder()
        } else {
            _reminderManager.cancelReminder()
        }
        _settingsService.setSendReportReminders(value)
    }

    fun setGoal(value: Int?) = viewModelScope.launch {
        val rg = roleGoal.firstOrNull()
        val role = role.firstOrNull()

        if (value == null || (value == rg && role != Role.Publisher)) {
            resetGoal()
            return@launch
        }
        monthlyInfo.firstOrNull()?.let {
            _monthlyInformationRepository.save(it.copy(goal = value))
        }
    }

    fun resetGoal() = viewModelScope.launch {
        monthlyInfo.firstOrNull()?.let {
            _monthlyInformationRepository.save(it.copy(goal = null))
        }
    }

    fun setDesign(d: Design) = viewModelScope.launch {
        _settingsService.setDesign(d)
    }

    fun setUseSystemColors(value: Boolean) = viewModelScope.launch {
        _settingsService.setUseSystemColors(value)
    }

    fun setRole(r: Role) = viewModelScope.launch {
        val msg = manuallySetGoal.firstOrNull()
        if (msg == role.firstOrNull()?.goal && role.firstOrNull() != Role.Publisher) {
            resetGoal()
        }
        if (isAnyPioneer(r) && !isAnyPioneer(role.value)) {
            setPioneerSince(currentMonth)
        } else if (!isAnyPioneer(r)) {
            resetPioneerSince()
        }
        _settingsService.setRole(r)
    }

    fun setName(text: String) = viewModelScope.launch {
        _settingsService.setName(text)
    }

    private fun isAnyPioneer(role: Role) = role == Role.RegularPioneer || role == Role.SpecialPioneer

    private fun resetPioneerSince() = viewModelScope.launch {
        _settingsService.setPioneerSince(null)
    }
}

private const val DEFAULT_TIMEOUT = 5000L
