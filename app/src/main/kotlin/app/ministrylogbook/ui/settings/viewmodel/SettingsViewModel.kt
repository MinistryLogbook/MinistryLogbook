package app.ministrylogbook.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.data.Design
import app.ministrylogbook.data.MonthlyInformation
import app.ministrylogbook.data.MonthlyInformationRepository
import app.ministrylogbook.data.Role
import app.ministrylogbook.data.SettingsDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class SettingsViewModel(
    private val _settingsDataStore: SettingsDataStore,
    private val _monthlyInformationRepository: MonthlyInformationRepository
) : ViewModel() {
    private val _monthlyInfo = MutableStateFlow<MonthlyInformation?>(null)

    val name = _settingsDataStore.name
    val design = _settingsDataStore.design
    val role = _settingsDataStore.role
    val roleGoal = _settingsDataStore.roleGoal
    val manuallySetGoal = _monthlyInfo.map { it?.goal }
    val goal = roleGoal.combine(manuallySetGoal) { rg, msg -> msg ?: rg }

    fun load() {
        val currentMonth = Clock.System.todayIn(TimeZone.currentSystemDefault())
        viewModelScope.launch {
            _monthlyInfo.value = _monthlyInformationRepository.getOfMonth(currentMonth)
        }
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
        _settingsDataStore.setRole(r)
    }

    fun setName(text: String) = viewModelScope.launch {
        _settingsDataStore.setName(text)
    }
}