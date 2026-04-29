package app.ministrylogbook.shared.services

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
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

    @Test
    fun scheduleReminder_replacesExistingReminderForSameId() {
        val reminderManager = ReminderManager(context)

        reminderManager.cancelReminder(REMINDER_ID)
        assertNull(findReminderPendingIntent())

        reminderManager.scheduleReminder(
            dateTime = LocalDateTime(2026, 4, 30, 20, 0),
            id = REMINDER_ID
        )
        reminderManager.scheduleReminder(
            dateTime = LocalDateTime(2026, 5, 31, 20, 0),
            id = REMINDER_ID
        )

        if (canScheduleExactAlarms()) {
            assertNotNull(findReminderPendingIntent())
        } else {
            assertNull(findReminderPendingIntent())
        }
        reminderManager.cancelReminder(REMINDER_ID)
        assertNull(findReminderPendingIntent())
    }

    @Test
    fun bootReceiverSchedulesReminderOnBootCompletedOnly() {
        val reminderManager = ReminderManager(context)
        reminderManager.cancelReminder(ReminderManager.REMINDER_NOTIFICATION_REQUEST_CODE)
        assertNull(findDefaultReminderPendingIntent())

        BootReceiver().onReceive(context, Intent(Intent.ACTION_TIME_CHANGED))
        assertNull(findDefaultReminderPendingIntent())

        BootReceiver().onReceive(context, Intent(Intent.ACTION_BOOT_COMPLETED))

        if (canScheduleExactAlarms()) {
            assertNotNull(findDefaultReminderPendingIntent())
        } else {
            assertNull(findDefaultReminderPendingIntent())
        }
        reminderManager.cancelReminder(ReminderManager.REMINDER_NOTIFICATION_REQUEST_CODE)
    }

    @Test
    fun reminderReceiverShowsReminderAndSchedulesNextReminder() {
        val reminderManager = ReminderManager(context)
        reminderManager.cancelReminder(ReminderManager.REMINDER_NOTIFICATION_REQUEST_CODE)
        createReminderChannel()

        ReminderReceiver().onReceive(context, Intent(context, ReminderReceiver::class.java))

        if (canScheduleExactAlarms()) {
            assertNotNull(findDefaultReminderPendingIntent())
        } else {
            assertNull(findDefaultReminderPendingIntent())
        }
        reminderManager.cancelReminder(ReminderManager.REMINDER_NOTIFICATION_REQUEST_CODE)
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

    private fun findDefaultReminderPendingIntent(): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            ReminderManager.REMINDER_NOTIFICATION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createReminderChannel() {
        if (Build.VERSION.SDK_INT < 26) {
            return
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    private companion object {
        const val REMINDER_ID = 10_427
    }
}
