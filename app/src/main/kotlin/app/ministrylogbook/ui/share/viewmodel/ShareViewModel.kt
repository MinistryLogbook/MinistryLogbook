package app.ministrylogbook.ui.share.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.R
import app.ministrylogbook.data.EntryRepository
import app.ministrylogbook.data.MonthlyInformationRepository
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.shared.ministryTimeSum
import app.ministrylogbook.shared.placements
import app.ministrylogbook.shared.returnVisits
import app.ministrylogbook.shared.theocraticAssignmentTimeSum
import app.ministrylogbook.shared.theocraticSchoolTimeSum
import app.ministrylogbook.shared.videoShowings
import app.ministrylogbook.ui.share.FieldServiceReport
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class ShareViewModel(
    val month: LocalDate,
    application: Application,
    entryRepository: EntryRepository,
    private val monthlyInfoRepository: MonthlyInformationRepository,
    settingsDataStore: SettingsService
) : AndroidViewModel(application) {

    private val _monthlyInformation = monthlyInfoRepository.getOfMonth(month)
    private val _entries = entryRepository.getAllOfMonth(month)

    val comments = _monthlyInformation.map { it.reportComment }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT),
        initialValue = ""
    )

    val fieldServiceReport =
        combine(
            _entries,
            settingsDataStore.name,
            _monthlyInformation
        ) { entries, name, studyEntry ->
            val theocraticAssignmentTime = entries.theocraticAssignmentTimeSum()
            val theocraticSchoolTime = entries.theocraticSchoolTimeSum()
            val commentTheocraticAssignment =
                application.resources.getQuantityString(
                    R.plurals.hours_spent_on_theocratic_assignments,
                    theocraticAssignmentTime.hours,
                    theocraticAssignmentTime.hours
                )
            val commentTheocraticSchool =
                application.resources.getQuantityString(
                    R.plurals.hours_spent_on_theocratic_schools,
                    theocraticSchoolTime.hours,
                    theocraticSchoolTime.hours
                )
            val comments = listOfNotNull(
                commentTheocraticAssignment.takeIf { theocraticAssignmentTime.hours > 0 },
                commentTheocraticSchool.takeIf { theocraticSchoolTime.hours > 0 }
            ).joinToString("\n")
            val locale = application.resources.configuration.locales.get(0)

            val ministryTimeSum = entries.ministryTimeSum()

            FieldServiceReport(
                name = name,
                month = getMonthTitle(locale),
                placements = entries.placements(),
                hours = ministryTimeSum.hours,
                returnVisits = entries.returnVisits(),
                videoShowings = entries.videoShowings(),
                bibleStudies = studyEntry.bibleStudies ?: 0,
                comments = comments
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT),
            initialValue = FieldServiceReport()
        )

    fun updateComments(text: String) = viewModelScope.launch {
        _monthlyInformation.firstOrNull()?.let {
            monthlyInfoRepository.save(it.copy(reportComment = text))
        }
    }

    private fun getMonthTitle(locale: Locale): String = month.run {
        val monthName = this.month.getDisplayName(TextStyle.FULL, locale)
        val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
        if (this.year != currentYear) "$monthName ${this.year}" else monthName
    }
}

private const val DEFAULT_TIMEOUT = 5000L
