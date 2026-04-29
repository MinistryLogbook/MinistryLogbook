package app.ministrylogbook

import app.ministrylogbook.shared.utilities.isInFirstWeekOfMonth
import app.ministrylogbook.shared.utilities.isLeapYear
import app.ministrylogbook.shared.utilities.lastDayOfMonth
import app.ministrylogbook.shared.utilities.weekNumber
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalDateExtensionsTest {
    @Test
    fun lastDayOfMonth_handlesLeapYearFebruary() {
        assertEquals(LocalDate(2024, 2, 29), LocalDate(2024, 2, 1).lastDayOfMonth)
    }

    @Test
    fun lastDayOfMonth_handlesNonLeapYearFebruary() {
        assertEquals(LocalDate(2025, 2, 28), LocalDate(2025, 2, 1).lastDayOfMonth)
    }

    @Test
    fun lastDayOfMonth_handlesThirtyAndThirtyOneDayMonths() {
        assertEquals(LocalDate(2026, 4, 30), LocalDate(2026, 4, 15).lastDayOfMonth)
        assertEquals(LocalDate(2026, 5, 31), LocalDate(2026, 5, 15).lastDayOfMonth)
    }

    @Test
    fun isLeapYear_followsGregorianCenturyRules() {
        assertTrue(LocalDate(2000, 1, 1).isLeapYear)
        assertFalse(LocalDate(1900, 1, 1).isLeapYear)
        assertTrue(LocalDate(2024, 1, 1).isLeapYear)
        assertFalse(LocalDate(2025, 1, 1).isLeapYear)
    }

    @Test
    fun weekNumber_handlesYearBoundaryWeek() {
        assertEquals(1, LocalDate(2026, 1, 1).weekNumber)
    }

    @Test
    fun weekNumber_countsWeeksAfterYearBoundaryAdjustment() {
        assertEquals(2, LocalDate(2026, 1, 5).weekNumber)
        assertEquals(53, LocalDate(2026, 12, 31).weekNumber)
    }

    @Test
    fun isInFirstWeekOfMonth_usesCalendarWeekBoundary() {
        assertTrue(LocalDate(2026, 4, 1).isInFirstWeekOfMonth)
        assertFalse(LocalDate(2026, 4, 8).isInFirstWeekOfMonth)
    }

    @Test
    fun isInFirstWeekOfMonth_includesSundayEndingFirstCalendarWeek() {
        assertTrue(LocalDate(2026, 4, 5).isInFirstWeekOfMonth)
        assertFalse(LocalDate(2026, 4, 6).isInFirstWeekOfMonth)
    }
}
