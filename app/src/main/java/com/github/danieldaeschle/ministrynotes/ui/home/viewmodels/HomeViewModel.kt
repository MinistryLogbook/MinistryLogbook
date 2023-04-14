package com.github.danieldaeschle.ministrynotes.ui.home.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.danieldaeschle.ministrynotes.data.BibleStudyEntry
import com.github.danieldaeschle.ministrynotes.data.BibleStudyEntryRepository
import com.github.danieldaeschle.ministrynotes.data.Entry
import com.github.danieldaeschle.ministrynotes.data.EntryRepository
import com.github.danieldaeschle.ministrynotes.data.EntryType
import com.github.danieldaeschle.ministrynotes.data.SettingsDataStore
import com.github.danieldaeschle.ministrynotes.lib.Time
import com.github.danieldaeschle.ministrynotes.lib.ministryTimeSum
import com.github.danieldaeschle.ministrynotes.lib.placements
import com.github.danieldaeschle.ministrynotes.lib.returnVisits
import com.github.danieldaeschle.ministrynotes.lib.theocraticAssignmentTimeSum
import com.github.danieldaeschle.ministrynotes.lib.theocraticSchoolTimeSum
import com.github.danieldaeschle.ministrynotes.lib.videoShowings
import com.github.danieldaeschle.ministrynotes.ui.home.share.FieldServiceReport
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import java.time.format.TextStyle
import java.util.Locale

class HomeViewModel(
    val month: LocalDate,
    application: Application,
    private val _entryRepository: EntryRepository,
    private val _bibleStudyEntryRepository: BibleStudyEntryRepository,
    settingsDataStore: SettingsDataStore,
) : AndroidViewModel(application) {

    private val _bibleStudyEntry = MutableStateFlow<BibleStudyEntry?>(null)
    private val _entries = MutableStateFlow<List<Entry>>(listOf())
    private val _restLastMonth = MutableStateFlow(Time.Empty)
    private val _transferred = MutableStateFlow<List<Entry>>(listOf())

    val entries = _entries.asStateFlow()
    val bibleStudies = _bibleStudyEntry.asStateFlow().map { it?.count ?: 0 }
    val restLastMonth = _restLastMonth.asStateFlow()
    val transferred = _transferred.asStateFlow()
    val rest = _entries.combine(_transferred) { entries, transferred ->
        entries.ministryTimeSum() - transferred.ministryTimeSum()
    }

    val fieldServiceReport =
        combine(_entries, settingsDataStore.name, _bibleStudyEntry) { entries, name, studyEntry ->
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
            val locale = application.resources.configuration.locales.get(0)

            FieldServiceReport(
                name = name,
                month = getMonthTitle(locale),
                placements = entries.placements(),
                hours = entries.ministryTimeSum().hours,
                returnVisits = entries.returnVisits(),
                videoShowings = entries.videoShowings(),
                bibleStudies = studyEntry?.count ?: 0,
                comments = comments,
            )
        }

    fun getMonthTitle(locale: Locale): String = month.run {
        val monthName = this.month.getDisplayName(TextStyle.FULL, locale)
        val currentYear = Clock.System.todayIn(TimeZone.currentSystemDefault()).year
        if (this.year != currentYear) "$monthName ${this.year}" else monthName
    }

    fun transferToNextMonth(minutes: Int) {
        val firstOfMonth = LocalDate(month.year, month.month, 1)
        val nextMonth = firstOfMonth + DatePeriod(months = 1)
        val lastOfMonth = nextMonth - DatePeriod(days = 1)
        val transfer = Entry(
            datetime = nextMonth,
            minutes = minutes,
            type = EntryType.Transfer,
            transferredFrom = lastOfMonth,
        )
        viewModelScope.launch {
            val id = _entryRepository.save(transfer)
            _transferred.value += transfer.copy(id = id)
        }
    }

    fun undoTransfer(transfer: Entry) {
        viewModelScope.launch {
            _entryRepository.delete(transfer)
            _transferred.value = _transferred.value.filter { it.id != transfer.id }
            _entries.value = _entries.value.filter { it.id != transfer.id }
        }
    }

    /** Transferring 0 minutes dismisses the message and won't show a history item. */
    fun transferFromLastMonth(minutes: Int) {
        val firstOfMonth = LocalDate(month.year, month.month, 1)
        val lastMonth = firstOfMonth - DatePeriod(days = 1)
        val transfer = Entry(
            datetime = firstOfMonth,
            minutes = minutes,
            type = EntryType.Transfer,
            transferredFrom = lastMonth
        )
        viewModelScope.launch {
            val id = _entryRepository.save(transfer)
            _entries.value += transfer.copy(id = id)
        }
    }

    fun load() = viewModelScope.launch {
        val lastMonth = month.minus(DatePeriod(months = 1))

        val allDefer = async { _entryRepository.getAllOfMonth(month) }
        val lastMonthTimeDefer = async {
            val entriesLastMonth = _entryRepository.getAllOfMonth(lastMonth)
            entriesLastMonth.ministryTimeSum()
        }
        val transferredDefer = async { _entryRepository.getTransferredFrom(month) }
        val studyEntryDefer = async { _bibleStudyEntryRepository.getOfMonth(month) }

        _bibleStudyEntry.value = studyEntryDefer.await()
        _restLastMonth.value = Time(minutes = lastMonthTimeDefer.await().minutes)
        _transferred.value = transferredDefer.await()
        _entries.value = allDefer.await()
    }
}