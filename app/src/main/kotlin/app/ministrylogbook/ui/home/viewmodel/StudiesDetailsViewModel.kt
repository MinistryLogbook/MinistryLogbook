package app.ministrylogbook.ui.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.data.MonthlyInformationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class StudiesDetailsViewModel(
    month: LocalDate,
    private val _monthlyInfoRepository: MonthlyInformationRepository
) : ViewModel() {

    private val _monthlyInfo = _monthlyInfoRepository.getOfMonth(month)

    val bibleStudies = _monthlyInfo.map { it.bibleStudies ?: 0 }.stateIn(
        scope = viewModelScope,
        initialValue = 0,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT)
    )

    fun save(count: Int) = viewModelScope.launch {
        _monthlyInfo.first().copy(bibleStudies = count).let { studyEntry ->
            _monthlyInfoRepository.save(studyEntry)
        }
    }
}

private const val DEFAULT_TIMEOUT = 5000L
