package app.ministrylogbook.ui.intro.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.data.MonthlyInformationRepository
import app.ministrylogbook.data.Role
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.notifications.ReminderManager
import app.ministrylogbook.shared.IntentViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class IntroState(
    val name: String?,
    val role: Role,
    val reminders: Boolean,
    val goal: Int?,
)

sealed class IntroIntent {
    data class NameChange(val name: String) : IntroIntent()
    data class RoleChange(val role: Role) : IntroIntent()
    data class RemindersToggle(val enabled: Boolean) : IntroIntent()
    data class GoalChange(val goal: Int?) : IntroIntent()
}

class IntroViewModel(
    private val _settingsService: SettingsService,
    private val _monthlyInfoRepository: MonthlyInformationRepository,
    private val _reminderManager: ReminderManager
) : ViewModel(), IntentViewModel<IntroState, IntroIntent> {

    private val _today by lazy {
        Clock.System.todayIn(TimeZone.currentSystemDefault())
    }

    override val state = combine(
        _settingsService.name,
        _settingsService.role,
        _settingsService.sendReportReminder,
        _monthlyInfoRepository.getOfMonth(_today)
    ) { name, role, reminders, monthlyInfo ->
        val goal = if (role == Role.Publisher) {
            monthlyInfo.goal
        } else {
            monthlyInfo.goal ?: role.goal
        }
        IntroState(name, role, reminders, goal)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        IntroState(null, Role.Publisher, false, null),
    )

    override fun dispatch(intent: IntroIntent) {
        when (intent) {
            is IntroIntent.NameChange -> viewModelScope.launch {
                _settingsService.setName(intent.name)
            }

            is IntroIntent.RoleChange -> viewModelScope.launch {
                _settingsService.setRole(intent.role)
            }

            is IntroIntent.RemindersToggle -> viewModelScope.launch {
                if (intent.enabled) {
                    _reminderManager.scheduleReminder()
                } else {
                    _reminderManager.cancelReminder()
                }
                _settingsService.setSendReportReminders(intent.enabled)
            }

            is IntroIntent.GoalChange -> viewModelScope.launch {
                _monthlyInfoRepository.update(_today) {
                    it.copy(goal = intent.goal)
                }
            }
        }
    }
}