package com.github.danieldaeschle.ministrynotes.ui.home.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.danieldaeschle.ministrynotes.data.Entry
import com.github.danieldaeschle.ministrynotes.data.EntryRepository
import com.github.danieldaeschle.ministrynotes.data.StudyEntry
import com.github.danieldaeschle.ministrynotes.data.StudyEntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class HomeViewModel(
    private val _entryRepository: EntryRepository,
    private val _studyEntryRepository: StudyEntryRepository
) : ViewModel() {

    private val _studyEntry = MutableStateFlow<StudyEntry?>(null)
    private val _entries = MutableStateFlow<List<Entry>>(listOf())
    private val _currentDate by lazy {
        val current = Clock.System.todayIn(TimeZone.currentSystemDefault())
        LocalDate(current.year, current.monthNumber, 1)
    }
    private val _selectedMonth = MutableStateFlow(_currentDate)

    val entries = _entries.asStateFlow()
    val selectedMonth = _selectedMonth.asStateFlow()
    val studies = _studyEntry.asStateFlow().map { it?.count ?: 0 }

    fun load(year: Int, monthNumber: Int) = viewModelScope.launch {
        _entries.value = _entryRepository.getAllOfMonth(year, monthNumber)
        _studyEntry.value = _studyEntryRepository.getOfMonth(year, monthNumber)
        _selectedMonth.value = LocalDate(year, monthNumber, 1)
    }
}