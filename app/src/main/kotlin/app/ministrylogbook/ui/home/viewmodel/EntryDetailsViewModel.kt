package app.ministrylogbook.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryRepository
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.data.Role
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.shared.utilities.mutableStateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toLocalDateTime

class EntryDetailsViewModel(
    month: LocalDate,
    val id: Int?,
    settingsDataStore: SettingsService,
    private val _entryRepository: EntryRepository
) : ViewModel() {

    private val _initialEntry = Entry(id = id ?: 0, datetime = month.atTime(0, 0))
    private val _originalEntry = if (id != null) {
        _entryRepository.get(id)
    } else {
        MutableStateFlow(_initialEntry)
    }

    private val _entry =
        if (id != null) {
            _originalEntry.filterNotNull().mutableStateIn(viewModelScope, _initialEntry)
        } else {
            MutableStateFlow(_initialEntry)
        }

    val role = settingsDataStore.role.stateIn(
        scope = viewModelScope,
        initialValue = Role.Publisher,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val entry = _entry.stateIn(
        scope = viewModelScope,
        initialValue = _initialEntry,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val precisionMode = settingsDataStore.precisionMode.stateIn(
        scope = viewModelScope,
        initialValue = false,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )
    val hasChanges = combine(_entry, _originalEntry) { entry, original -> entry != original }.stateIn(
        scope = viewModelScope,
        initialValue = false,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )

    fun update(datetime: LocalDateTime? = null, hours: Int? = null, minutes: Int? = null, type: EntryType? = null) {
        _entry.update { old ->
            val currentDatetime = if (datetime == null) {
                old.datetime
            } else {
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                datetime.date.atTime(now.hour, now.minute)
            }
            old.copy(
                datetime = currentDatetime,
                hours = hours ?: old.hours,
                minutes = minutes ?: old.minutes,
                type = type ?: old.type
            )
        }
    }

    fun save() = viewModelScope.launch {
        val currentEntry = entry.value
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentDatetime = if (currentEntry.id == 0) {
            currentEntry.datetime.date.atTime(now.hour, now.minute)
        } else {
            currentEntry.datetime
        }

        if (currentEntry.type == EntryType.Ministry) {
            _entryRepository.save(currentEntry.copy(datetime = currentDatetime))
        } else {
            _entryRepository.save(
                currentEntry.copy(
                    datetime = currentDatetime,
                    placements = 0,
                    videoShowings = 0,
                    returnVisits = 0
                )
            )
        }
    }

    fun delete() = viewModelScope.launch {
        _entryRepository.delete(entry.value)
    }
}

private const val DEFAULT_TIMEOUT = 5000L
