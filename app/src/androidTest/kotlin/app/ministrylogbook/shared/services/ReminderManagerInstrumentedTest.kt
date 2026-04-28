package app.ministrylogbook.shared.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderManagerInstrumentedTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun scheduleAndCancelReminder_manageExpectedPendingIntent() {
        val reminderManager = ReminderManager(context)

        reminderManager.cancelReminder(REMINDER_ID)
        assertNull(findReminderPendingIntent())

        reminderManager.scheduleReminder(
            dateTime = LocalDateTime(2026, 4, 30, 20, 0),
            id = REMINDER_ID
        )

        if (canScheduleExactAlarms()) {
            assertNotNull(findReminderPendingIntent())
            reminderManager.cancelReminder(REMINDER_ID)
            assertNull(findReminderPendingIntent())
        } else {
            assertNull(findReminderPendingIntent())
        }
    }

    private fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < 31) {
            return true
        }
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    private fun findReminderPendingIntent(): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REMINDER_ID,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private companion object {
        const val REMINDER_ID = 10_427
    }
}
