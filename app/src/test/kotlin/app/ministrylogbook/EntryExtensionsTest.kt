package app.ministrylogbook

import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.shared.Time
import app.ministrylogbook.shared.utilities.ministries
import app.ministrylogbook.shared.utilities.ministryTimeSum
import app.ministrylogbook.shared.utilities.splitIntoMonths
import app.ministrylogbook.shared.utilities.theocraticAssignmentTimeSum
import app.ministrylogbook.shared.utilities.theocraticSchoolTimeSum
import app.ministrylogbook.shared.utilities.timeSum
import app.ministrylogbook.shared.utilities.transfers
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EntryExtensionsTest {
    @Test
    fun timeSum_normalizesMinutesAcrossEntries() {
        val entries = listOf(
            entry(hours = 1, minutes = 45),
            entry(hours = 0, minutes = 30)
        )

        assertEquals(Time(hours = 2, minutes = 15), entries.timeSum())
    }

    @Test
    fun typeSpecificSums_filterExpectedEntryTypes() {
        val entries = listOf(
            entry(hours = 1, type = EntryType.Ministry),
            entry(hours = 2, type = EntryType.Transfer),
            entry(hours = 3, type = EntryType.TheocraticAssignment),
            entry(hours = 4, type = EntryType.TheocraticSchool)
        )

        assertEquals(Time(hours = 3, minutes = 0), entries.ministryTimeSum())
        assertEquals(Time(hours = 3, minutes = 0), entries.theocraticAssignmentTimeSum())
        assertEquals(Time(hours = 4, minutes = 0), entries.theocraticSchoolTimeSum())
    }

    @Test
    fun ministries_includeMinistryAndTransferEntriesOnly() {
        val ministry = entry(id = 1, type = EntryType.Ministry)
        val transfer = entry(id = 2, type = EntryType.Transfer)
        val assignment = entry(id = 3, type = EntryType.TheocraticAssignment)
        val school = entry(id = 4, type = EntryType.TheocraticSchool)

        assertEquals(listOf(ministry, transfer), listOf(ministry, transfer, assignment, school).ministries())
    }

    @Test
    fun transfers_includeTransferEntriesOnly() {
        val ministry = entry(id = 1, type = EntryType.Ministry)
        val transfer = entry(id = 2, type = EntryType.Transfer)

        assertEquals(listOf(transfer), listOf(ministry, transfer).transfers())
    }

    @Test
    fun splitIntoMonths_sortsEntriesAndGroupsByYearAndMonth() {
        val march = entry(id = 1, date = LocalDate(2026, 3, 31))
        val january = entry(id = 2, date = LocalDate(2026, 1, 1))
        val nextJanuary = entry(id = 3, date = LocalDate(2027, 1, 1))
        val januaryLater = entry(id = 4, date = LocalDate(2026, 1, 15))

        val result = listOf(march, january, nextJanuary, januaryLater).splitIntoMonths()

        assertEquals(listOf(january, januaryLater), result[0])
        assertEquals(listOf(march), result[1])
        assertEquals(listOf(nextJanuary), result[2])
    }

    @Test
    fun splitIntoMonths_returnsEmptyListForEmptyInput() {
        assertTrue(emptyList<Entry>().splitIntoMonths().isEmpty())
    }

    private fun entry(
        id: Int = 0,
        hours: Int = 0,
        minutes: Int = 0,
        type: EntryType = EntryType.Ministry,
        date: LocalDate = LocalDate(2026, 4, 1)
    ) = Entry(
        id = id,
        datetime = LocalDateTime(date.year, date.month, date.day, 12, 0),
        hours = hours,
        minutes = minutes,
        type = type
    )
}
