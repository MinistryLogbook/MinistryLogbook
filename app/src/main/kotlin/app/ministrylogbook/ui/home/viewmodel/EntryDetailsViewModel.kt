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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class EntryDetailsViewModel(
    month: LocalDate,
    val id: Int?,
    settingsDataStore: SettingsService,
    private val _entryRepository: EntryRepository
) : ViewModel() {

    private val _initialEntry = Entry(id = id ?: 0, datetime = month)
    private val _entry =
        if (id != null) {
            _entryRepository.get(id).mutableStateIn(viewModelScope, _initialEntry)
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

    fun update(
        datetime: LocalDate? = null,
        placements: Int? = null,
        videoShowings: Int? = null,
        hours: Int? = null,
        minutes: Int? = null,
        returnVisits: Int? = null,
        type: EntryType? = null
    ) {
        _entry.update { old ->
            old.copy(
                datetime = datetime ?: old.datetime,
                placements = placements ?: old.placements,
                videoShowings = videoShowings ?: old.videoShowings,
                hours = hours ?: old.hours,
                minutes = minutes ?: old.minutes,
                returnVisits = returnVisits ?: old.returnVisits,
                type = type ?: old.type
            )
        }
    }

    fun save() = viewModelScope.launch {
        _entryRepository.save(entry.value)
    }

    fun delete() = viewModelScope.launch {
        _entryRepository.delete(entry.value)
    }
}

private const val DEFAULT_TIMEOUT = 5000L
