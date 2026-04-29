package app.ministrylogbook.shared.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import app.ministrylogbook.shared.utilities.lastDayOfMonth
import java.util.Calendar
import java.util.Locale
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.todayIn

interface ReminderScheduler {
    fun scheduleReminder()

    fun cancelReminder()
}

class ReminderManager(private val context: Context) : ReminderScheduler {

    companion object {
        const val REMINDER_NOTIFICATION_REQUEST_CODE = 1
    }

    override fun scheduleReminder() {
        scheduleReminder(defaultReminderTime(), REMINDER_NOTIFICATION_REQUEST_CODE)
    }

    fun scheduleReminder(
        dateTime: LocalDateTime = defaultReminderTime(),
        id: Int = REMINDER_NOTIFICATION_REQUEST_CODE
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) {
            return
        }

        val intent = Intent(context.applicationContext, ReminderReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(
                context.applicationContext,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val locale = ConfigurationCompat.getLocales(context.resources.configuration).get(0)
            ?: LocaleListCompat.getDefault()[0]!!

        alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTriggerTimeInMillis(dateTime, locale), intent)
    }

    override fun cancelReminder() {
        cancelReminder(REMINDER_NOTIFICATION_REQUEST_CODE)
    }

    fun cancelReminder(id: Int = REMINDER_NOTIFICATION_REQUEST_CODE) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        alarmManager.cancel(intent)
        intent.cancel()
    }

    private fun defaultReminderTime(): LocalDateTime {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return today.lastDayOfMonth.atTime(20, 0)
    }
}

internal fun reminderTriggerTimeInMillis(dateTime: LocalDateTime, locale: Locale): Long =
    Calendar.getInstance(locale).apply {
        clear()
        set(
            dateTime.year,
            dateTime.month.ordinal,
            dateTime.day,
            dateTime.hour,
            dateTime.minute,
            dateTime.second
        )
    }.timeInMillis

const val REMINDER_CHANNEL_ID = "reminder_channel"
