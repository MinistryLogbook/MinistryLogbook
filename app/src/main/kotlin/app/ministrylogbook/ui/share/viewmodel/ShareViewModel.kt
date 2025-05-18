package app.ministrylogbook.ui.share.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.ministrylogbook.R
import app.ministrylogbook.data.BibleStudyRepository
import app.ministrylogbook.data.EntryRepository
import app.ministrylogbook.data.MonthlyInformationRepository
import app.ministrylogbook.data.Role
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.shared.utilities.ministryTimeSum
import app.ministrylogbook.shared.utilities.theocraticAssignmentTimeSum
import app.ministrylogbook.shared.utilities.theocraticSchoolTimeSum
import app.ministrylogbook.ui.share.FieldServiceReport
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class ShareViewModel(
    val month: LocalDate,
    application: Application,
    entryRepository: EntryRepository,
    bibleStudiesRepository: BibleStudyRepository,
    private val _monthlyInfoRepository: MonthlyInformationRepository,
    settingsDataStore: SettingsService
) : AndroidViewModel(application) {

    private val _monthlyInformation = _monthlyInfoRepository.getOfMonth(month)
    private val _bibleStudies = bibleStudiesRepository.getAllOfMonth(month)
    private val _entries = entryRepository.getAllOfMonth(month)

    val initialComments = _monthlyInformation.map { it.reportComment }.take(1).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT),
        initialValue = ""
    )

    val fieldServiceReport =
        combine(
            _entries,
            settingsDataStore.name,
            _bibleStudies,
            settingsDataStore.role
        ) { entries, name, bibleStudies, role ->
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
                hours = ministryTimeSum.hours,
                bibleStudies = bibleStudies.filter { it.checked }.size,
                comments = comments,
                reportsHours = role == Role.AuxiliaryPioneer ||
                    role == Role.RegularPioneer ||
                    role == Role.SpecialPioneer
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT),
            initialValue = FieldServiceReport()
        )

    fun updateComments(text: String) = viewModelScope.launch {
        _monthlyInformation.firstOrNull()?.let {
            _monthlyInfoRepository.save(it.copy(reportComment = text))
        }
    }

    fun markMonthlyReportSent() = viewModelScope.launch {
        _monthlyInformation.firstOrNull()?.let {
            _monthlyInfoRepository.save(it.copy(reportSent = true))
        }
    }

    private fun getMonthTitle(locale: Locale): String = month.run {
        val monthName = this.month.getDisplayName(TextStyle.FULL, locale)
        val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
        if (this.year != currentYear) "$monthName ${this.year}" else monthName
    }
}

private const val DEFAULT_TIMEOUT = 5000L
