package app.ministrylogbook.shared.services

import java.util.Calendar
import java.util.Locale
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderManagerTest {
    @Test
    fun reminderTriggerTimeInMillis_preservesYearAcrossYearBoundary() {
        val triggerTime = reminderTriggerTimeInMillis(
            LocalDateTime(2027, 1, 31, 20, 0),
            Locale.US
        )

        val calendar = Calendar.getInstance(Locale.US).apply {
            timeInMillis = triggerTime
        }

        assertEquals(2027, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH))
        assertEquals(31, calendar.get(Calendar.DAY_OF_MONTH))
        assertEquals(20, calendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, calendar.get(Calendar.MINUTE))
        assertEquals(0, calendar.get(Calendar.SECOND))
        assertEquals(0, calendar.get(Calendar.MILLISECOND))
    }
}
