package com.github.danieldaeschle.ministrynotes.ui.home.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.danieldaeschle.ministrynotes.data.StudyEntry
import com.github.danieldaeschle.ministrynotes.data.StudyEntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StudiesDetailsViewModel(
    private val _studyEntryRepository: StudyEntryRepository
) : ViewModel() {

    private val _studyEntry = MutableStateFlow<StudyEntry?>(null)

    val studyEntry = _studyEntry.asStateFlow()

    fun save(count: Int) = viewModelScope.launch {
        _studyEntry.value?.copy(count = count)?.let { studyEntry ->
            _studyEntryRepository.save(studyEntry)
            _studyEntry.update { studyEntry }
        }
    }

    fun load(year: Int, monthNumber: Int) = viewModelScope.launch {
        _studyEntry.value = _studyEntryRepository.getOfMonth(year, monthNumber)
    }
}