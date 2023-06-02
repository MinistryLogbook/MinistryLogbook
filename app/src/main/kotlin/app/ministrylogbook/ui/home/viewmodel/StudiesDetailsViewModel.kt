package app.ministrylogbook.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.data.MonthlyInformation
import app.ministrylogbook.data.MonthlyInformationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class StudiesDetailsViewModel(
    private val month: LocalDate,
    private val _Bible_studyEntryRepository: MonthlyInformationRepository
) : ViewModel() {

    private val _studyEntry = MutableStateFlow<MonthlyInformation?>(null)

    val studyEntry = _studyEntry.asStateFlow()

    fun save(count: Int) = viewModelScope.launch {
        _studyEntry.value?.copy(bibleStudies = count)?.let { studyEntry ->
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
