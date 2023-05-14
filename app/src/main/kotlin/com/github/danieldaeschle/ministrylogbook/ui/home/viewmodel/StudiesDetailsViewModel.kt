package com.github.danieldaeschle.ministrylogbook.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.danieldaeschle.ministrylogbook.data.BibleStudyEntry
import com.github.danieldaeschle.ministrylogbook.data.BibleStudyEntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class StudiesDetailsViewModel(
    private val month: LocalDate,
    private val _Bible_studyEntryRepository: BibleStudyEntryRepository
) : ViewModel() {

    private val _studyEntry = MutableStateFlow<BibleStudyEntry?>(null)

    val studyEntry = _studyEntry.asStateFlow()

    fun save(count: Int) = viewModelScope.launch {
        _studyEntry.value?.copy(count = count)?.let { studyEntry ->
            _Bible_studyEntryRepository.save(studyEntry)
            _studyEntry.update { studyEntry }
        }
    }

    init {
        viewModelScope.launch {
            _studyEntry.value = _Bible_studyEntryRepository.getOfMonth(month)
        }
    }
}