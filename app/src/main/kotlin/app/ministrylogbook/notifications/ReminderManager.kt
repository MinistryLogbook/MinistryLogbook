package app.ministrylogbook.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import java.util.Calendar
import kotlinx.datetime.LocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ReminderManager : KoinComponent {

    private val context: Context by inject()

    companion object {
        const val REMINDER_NOTIFICATION_REQUEST_CODE = 1
    }

    fun scheduleReminder(dateTime: LocalDateTime, id: Int = REMINDER_NOTIFICATION_REQUEST_CODE) {
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

        val calendar = Calendar.getInstance(locale).apply {
            set(Calendar.MONTH, dateTime.monthNumber - 1)
            set(Calendar.DAY_OF_MONTH, dateTime.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, dateTime.hour)
            set(Calendar.MINUTE, dateTime.minute)
            set(Calendar.SECOND, dateTime.second)
        }

        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(calendar.timeInMillis, intent), intent)
    }

    fun cancelReminder(id: Int = REMINDER_NOTIFICATION_REQUEST_CODE) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        alarmManager.cancel(intent)
    }
}

const val ReminderChannelId = "reminder_channel"
