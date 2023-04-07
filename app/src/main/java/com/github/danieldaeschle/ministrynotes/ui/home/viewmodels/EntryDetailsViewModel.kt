package com.github.danieldaeschle.ministrynotes.ui.home.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.danieldaeschle.ministrynotes.data.Entry
import com.github.danieldaeschle.ministrynotes.data.EntryRepository
import com.github.danieldaeschle.ministrynotes.data.EntryType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class EntryDetailsViewModel(val id: Int?, private val _entryRepository: EntryRepository) :
    ViewModel() {
    private val _entry = MutableStateFlow(Entry())

    val entry = _entry.asStateFlow()

    init {
        id?.let {
            viewModelScope.launch {
                _entry.value = _entryRepository.get(it)
            }
        }
    }

    fun update(
        datetime: LocalDate? = null,
        placements: Int? = null,
        videoShowings: Int? = null,
        hours: Int? = null,
        minutes: Int? = null,
        returnVisits: Int? = null,
        kind: EntryType? = null,
    ) {
        _entry.value = _entry.value.copy(
            datetime = datetime ?: _entry.value.datetime,
            placements = placements ?: _entry.value.placements,
            videoShowings = videoShowings ?: _entry.value.videoShowings,
            hours = hours ?: _entry.value.hours,
            minutes = minutes ?: _entry.value.minutes,
            returnVisits = returnVisits ?: _entry.value.returnVisits,
            type = kind ?: _entry.value.type,
        )
    }

    fun save() = viewModelScope.launch {
        val entryId = _entryRepository.save(_entry.value)
        _entry.value = _entry.value.copy(id = entryId)
    }

    fun delete() = viewModelScope.launch {
        _entryRepository.delete(_entry.value)
    }
}