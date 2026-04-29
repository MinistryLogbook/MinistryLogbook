package app.ministrylogbook.ui.home.time

import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryType
import app.ministrylogbook.data.Role
import app.ministrylogbook.shared.Time
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeTimeCalculatorTest {
    @Test
    fun calculateAccumulatedTime_appliesCreditCapForCreditRoles() {
        val entries = listOf(
            entry(hours = 50),
            entry(hours = 10, type = EntryType.TheocraticAssignment),
            entry(hours = 2, type = EntryType.TheocraticSchool)
        )

        val result = HomeTimeCalculator.calculateAccumulatedTime(
            entries = entries,
            transferredTime = Time(3, 0),
            role = Role.RegularPioneer,
            roleGoal = 50
        )

        assertEquals(Time(57, 0), result)
    }

    @Test
    fun calculateAccumulatedTime_ignoresCreditForNonCreditRoles() {
        val entries = listOf(
            entry(hours = 10),
            entry(hours = 3, type = EntryType.TheocraticAssignment),
            entry(hours = 2, type = EntryType.TheocraticSchool)
        )

        val result = HomeTimeCalculator.calculateAccumulatedTime(
            entries = entries,
            role = Role.Publisher,
            roleGoal = null
        )

        assertEquals(Time(10, 0), result)
    }

    @Test
    fun calculateSummary_showsTodaySegmentOnlyForCurrentMonthWithAddedTime() {
        val today = LocalDate(2026, 4, 29)
        val entries = listOf(
            entry(hours = 1, date = LocalDate(2026, 4, 28)),
            entry(hours = 2, date = today)
        )

        val currentMonth = HomeTimeCalculator.calculateSummary(
            month = LocalDate(2026, 4, 1),
            today = today,
            entries = entries,
            entriesLastMonth = emptyList(),
            transferred = emptyList(),
            role = Role.Publisher,
            goal = 10,
            roleGoal = null
        )
        val previousMonth = HomeTimeCalculator.calculateSummary(
            month = LocalDate(2026, 3, 1),
            today = today,
            entries = entries,
            entriesLastMonth = emptyList(),
            transferred = emptyList(),
            role = Role.Publisher,
            goal = 10,
            roleGoal = null
        )

        assertEquals(Time(2, 0), currentMonth.todayAdded)
        assertTrue(currentMonth.showTodaySegment)
        assertFalse(previousMonth.showTodaySegment)
    }

    @Test
    fun calculateSummary_usesFieldServiceTimeForPublisherRemainingHours() {
        val today = LocalDate(2026, 4, 29)
        val entries = listOf(
            entry(hours = 5, type = EntryType.Ministry),
            entry(hours = 3, type = EntryType.TheocraticAssignment)
        )

        val result = HomeTimeCalculator.calculateSummary(
            month = LocalDate(2026, 4, 1),
            today = today,
            entries = entries,
            entriesLastMonth = emptyList(),
            transferred = emptyList(),
            role = Role.Publisher,
            goal = 10,
            roleGoal = null
        )

        assertEquals(5, result.remainingHours)
        assertEquals(Time(5, 0), result.fieldServiceTime)
        assertEquals(Time(3, 0), result.credit)
    }

    @Test
    fun calculateSummary_usesAccumulatedTimeForCreditRoleRemainingHours() {
        val today = LocalDate(2026, 4, 29)
        val entries = listOf(
            entry(hours = 5, type = EntryType.Ministry),
            entry(hours = 3, type = EntryType.TheocraticAssignment)
        )

        val result = HomeTimeCalculator.calculateSummary(
            month = LocalDate(2026, 4, 1),
            today = today,
            entries = entries,
            entriesLastMonth = emptyList(),
            transferred = emptyList(),
            role = Role.RegularPioneer,
            goal = 10,
            roleGoal = 50
        )

        assertEquals(2, result.remainingHours)
        assertEquals(Time(8, 0), result.accumulatedTime)
    }

    @Test
    fun calculateSummary_subtractsTransferredTimeFromFieldServiceTime() {
        val today = LocalDate(2026, 4, 29)
        val transfer = entry(hours = 2, type = EntryType.Transfer)

        val result = HomeTimeCalculator.calculateSummary(
            month = LocalDate(2026, 4, 1),
            today = today,
            entries = listOf(
                entry(hours = 8),
                transfer
            ),
            entriesLastMonth = emptyList(),
            transferred = listOf(transfer),
            role = Role.Publisher,
            goal = 10,
            roleGoal = null
        )

        assertEquals(Time(8, 0), result.fieldServiceTime)
        assertEquals(Time(8, 0), result.accumulatedTime)
        assertEquals(2, result.remainingHours)
    }

    @Test
    fun calculateSummary_includesLastMonthEntriesInWeeklyProgressDuringFirstWeek() {
        val today = LocalDate(2026, 4, 1)

        val result = HomeTimeCalculator.calculateSummary(
            month = LocalDate(2026, 4, 1),
            today = today,
            entries = listOf(entry(hours = 1, date = today)),
            entriesLastMonth = listOf(entry(hours = 1, date = LocalDate(2026, 3, 30))),
            transferred = emptyList(),
            role = Role.Publisher,
            goal = 52,
            roleGoal = null
        )

        assertEquals(Time(2, 0), result.weeklyTime)
        assertEquals(2f / 12f, result.weeklyProgress, 0.001f)
    }

    @Test
    fun calculateSummary_excludesLastMonthEntriesAfterFirstWeek() {
        val today = LocalDate(2026, 4, 8)

        val result = HomeTimeCalculator.calculateSummary(
            month = LocalDate(2026, 4, 1),
            today = today,
            entries = listOf(entry(hours = 1, date = today)),
            entriesLastMonth = listOf(entry(hours = 1, date = LocalDate(2026, 3, 30))),
            transferred = emptyList(),
            role = Role.Publisher,
            goal = 52,
            roleGoal = null
        )

        assertEquals(Time(1, 0), result.weeklyTime)
        assertEquals(1f / 12f, result.weeklyProgress, 0.001f)
    }

    @Test
    fun calculateYearlyProgress_appliesMonthlyCreditCaps() {
        val entries = listOf(
            entry(hours = 50, date = LocalDate(2026, 1, 1)),
            entry(hours = 10, type = EntryType.TheocraticAssignment, date = LocalDate(2026, 1, 2)),
            entry(hours = 2, type = EntryType.TheocraticSchool, date = LocalDate(2026, 1, 3)),
            entry(hours = 4, date = LocalDate(2026, 2, 1))
        )

        val result = HomeTimeCalculator.calculateYearlyProgress(
            entriesInServiceYear = entries,
            role = Role.RegularPioneer,
            roleGoal = 50
        )

        assertEquals(Time(61, 0), result)
    }

    private fun entry(
        hours: Int,
        minutes: Int = 0,
        type: EntryType = EntryType.Ministry,
        date: LocalDate = LocalDate(2026, 4, 1)
    ) = Entry(
        datetime = LocalDateTime(date.year, date.month, date.day, 12, 0),
        hours = hours,
        minutes = minutes,
        type = type
    )
}
