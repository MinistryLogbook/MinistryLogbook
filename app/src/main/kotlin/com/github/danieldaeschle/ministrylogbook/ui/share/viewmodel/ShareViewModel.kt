package com.github.danieldaeschle.ministrylogbook.ui.share.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.danieldaeschle.ministrylogbook.R
import com.github.danieldaeschle.ministrylogbook.data.BibleStudyEntry
import com.github.danieldaeschle.ministrylogbook.data.BibleStudyEntryRepository
import com.github.danieldaeschle.ministrylogbook.data.Entry
import com.github.danieldaeschle.ministrylogbook.data.EntryRepository
import com.github.danieldaeschle.ministrylogbook.data.SettingsDataStore
import com.github.danieldaeschle.ministrylogbook.lib.ministryTimeSum
import com.github.danieldaeschle.ministrylogbook.lib.placements
import com.github.danieldaeschle.ministrylogbook.lib.returnVisits
import com.github.danieldaeschle.ministrylogbook.lib.theocraticAssignmentTimeSum
import com.github.danieldaeschle.ministrylogbook.lib.theocraticSchoolTimeSum
import com.github.danieldaeschle.ministrylogbook.lib.videoShowings
import com.github.danieldaeschle.ministrylogbook.ui.share.FieldServiceReport
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.time.format.TextStyle
import java.util.Locale

class ShareViewModel(
    val month: LocalDate,
    application: Application,
    private val _entryRepository: EntryRepository,
    private val _bibleStudyEntryRepository: BibleStudyEntryRepository,
    settingsDataStore: SettingsDataStore,
) : AndroidViewModel(application) {

    private val _bibleStudyEntry = MutableStateFlow<BibleStudyEntry?>(null)
    private val _entries = MutableStateFlow<List<Entry>>(listOf())

    val fieldServiceReport =
        combine(_entries, settingsDataStore.name, _bibleStudyEntry) { entries, name, studyEntry ->
            val theocraticAssignmentTime = entries.theocraticAssignmentTimeSum()
            val theocraticSchoolTime = entries.theocraticSchoolTimeSum()
            val commentTheocraticAssignment =
                application.resources.getQuantityString(
                    R.plurals.hours_spent_on_theocratic_assignments,
                    theocraticAssignmentTime.hours,
                    theocraticAssignmentTime.hours,
                )
            val commentTheocraticSchool =
                application.resources.getQuantityString(
                    R.plurals.hours_spent_on_theocratic_schools,
                    theocraticSchoolTime.hours,
                    theocraticSchoolTime.hours,
                )
            val comments = listOfNotNull(
                commentTheocraticAssignment.takeIf { theocraticAssignmentTime.hours > 0 },
                commentTheocraticSchool.takeIf { theocraticSchoolTime.hours > 0 },
            ).joinToString("\n")
            val locale = application.resources.configuration.locales.get(0)

            val ministryTimeSum = entries.ministryTimeSum()
            val hours = if (ministryTimeSum.hours > 0) {
                ministryTimeSum.hours.toFloat()
            } else {
                ministryTimeSum.minutes.toFloat() / 60
            }

            FieldServiceReport(
                name = name,
                month = getMonthTitle(locale),
                placements = entries.placements(),
                hours = hours,
                returnVisits = entries.returnVisits(),
                videoShowings = entries.videoShowings(),
                bibleStudies = studyEntry?.count ?: 0,
                comments = comments,
            )
        }

    fun load() = viewModelScope.launch {
        val allDefer = async { _entryRepository.getAllOfMonth(month) }
        val studyEntryDefer = async { _bibleStudyEntryRepository.getOfMonth(month) }

        _bibleStudyEntry.value = studyEntryDefer.await()
        _entries.value = allDefer.await()
    }

    private fun getMonthTitle(locale: Locale): String = month.run {
        val monthName = this.month.getDisplayName(TextStyle.FULL, locale)
        val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
        if (this.year != currentYear) "$monthName ${this.year}" else monthName
    }
}