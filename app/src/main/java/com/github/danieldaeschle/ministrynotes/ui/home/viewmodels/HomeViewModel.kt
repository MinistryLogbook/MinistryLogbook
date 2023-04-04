package com.github.danieldaeschle.ministrynotes.ui.home.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.danieldaeschle.ministrynotes.data.Entry
import com.github.danieldaeschle.ministrynotes.data.EntryRepository
import com.github.danieldaeschle.ministrynotes.data.StudyEntry
import com.github.danieldaeschle.ministrynotes.data.StudyEntryRepository
import com.github.danieldaeschle.ministrynotes.data.ministryTimeSum
import com.github.danieldaeschle.ministrynotes.data.theocraticAssignmentTimeSum
import com.github.danieldaeschle.ministrynotes.data.theocraticSchoolTimeSum
import com.github.danieldaeschle.ministrynotes.ui.home.share.FieldServiceReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.time.format.TextStyle
import java.util.Locale

class HomeViewModel(
    private val _entryRepository: EntryRepository,
    private val _studyEntryRepository: StudyEntryRepository,
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

    val monthTitle = selectedMonth.map {
        // TODO: locale based on user settings
        val monthName = it.month.getDisplayName(
            TextStyle.FULL, Locale.ENGLISH,
        )
        val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
        if (it.year != currentYear) "$monthName $it.year" else monthName
    }

    val fieldServiceReport =
        monthTitle.combine(_entries) { title, entries -> Pair(title, entries) }.map { combination ->
            // TODO: get name from user
            val name = "Your Name"
            val title = combination.first
            val entries = combination.second
            val theocraticAssignmentTime = entries.theocraticAssignmentTimeSum()
            val theocraticSchoolTime = entries.theocraticSchoolTimeSum()
            val commentTheocraticAssignment =
                "${theocraticAssignmentTime.hours} hours spent on theocratic assignments."
            val commentTheocraticSchool =
                "${theocraticSchoolTime.hours} hours spent on theocratic schools."
            val comments = listOfNotNull(
                commentTheocraticAssignment.takeIf { theocraticAssignmentTime.hours > 0 },
                commentTheocraticSchool.takeIf { theocraticSchoolTime.hours > 0 },
            ).joinToString("\n")

            FieldServiceReport(
                name = name,
                month = title,
                placements = _entries.value.sumOf { it.placements },
                hours = _entries.value.ministryTimeSum().hours,
                returnVisits = _entries.value.sumOf { it.returnVisits },
                videoShowings = _entries.value.sumOf { it.videoShowings },
                bibleStudies = _studyEntry.value?.count ?: 0,
                comments = comments,
            )
        }

    fun load(year: Int, monthNumber: Int) = viewModelScope.launch {
        _entries.value = _entryRepository.getAllOfMonth(year, monthNumber)
        _studyEntry.value = _studyEntryRepository.getOfMonth(year, monthNumber)
        _selectedMonth.value = LocalDate(year, monthNumber, 1)
    }
}