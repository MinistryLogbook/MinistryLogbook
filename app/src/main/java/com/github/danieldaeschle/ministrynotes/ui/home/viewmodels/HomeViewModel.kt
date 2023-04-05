package com.github.danieldaeschle.ministrynotes.ui.home.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.danieldaeschle.ministrynotes.data.Entry
import com.github.danieldaeschle.ministrynotes.data.EntryRepository
import com.github.danieldaeschle.ministrynotes.data.SettingsDataStore
import com.github.danieldaeschle.ministrynotes.data.StudyEntry
import com.github.danieldaeschle.ministrynotes.data.StudyEntryRepository
import com.github.danieldaeschle.ministrynotes.data.ministryTimeSum
import com.github.danieldaeschle.ministrynotes.data.placements
import com.github.danieldaeschle.ministrynotes.data.returnVisits
import com.github.danieldaeschle.ministrynotes.data.theocraticAssignmentTimeSum
import com.github.danieldaeschle.ministrynotes.data.theocraticSchoolTimeSum
import com.github.danieldaeschle.ministrynotes.data.videoShowings
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
    private val year: Int,
    private val monthNumber: Int,
    private val _entryRepository: EntryRepository,
    private val _studyEntryRepository: StudyEntryRepository,
    settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private val _studyEntry = MutableStateFlow<StudyEntry?>(null)
    private val _entries = MutableStateFlow<List<Entry>>(listOf())

    val entries = _entries.asStateFlow()
    val selectedMonth = LocalDate(year, monthNumber, 1)
    val studies = _studyEntry.asStateFlow().map { it?.count ?: 0 }

    val monthTitle: String = selectedMonth.run {
        // TODO: locale based on user settings
        val monthName = this.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
        if (this.year != currentYear) "$monthName $this.year" else monthName
    }

    val fieldServiceReport =
        _entries.combine(settingsDataStore.name) { entries, name -> Pair(entries, name) }
            .map { pair ->
                val entries = pair.first
                val name = pair.second
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
                    month = monthTitle,
                    placements = _entries.value.placements(),
                    hours = _entries.value.ministryTimeSum().hours,
                    returnVisits = _entries.value.returnVisits(),
                    videoShowings = _entries.value.videoShowings(),
                    bibleStudies = _studyEntry.value?.count ?: 0,
                    comments = comments,
                )
            }

    fun load() = viewModelScope.launch {
        _entries.value = _entryRepository.getAllOfMonth(year, monthNumber)
        _studyEntry.value = _studyEntryRepository.getOfMonth(year, monthNumber)
    }
}