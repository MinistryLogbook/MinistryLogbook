package app.ministrylogbook.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import app.ministrylogbook.MainActivity
import app.ministrylogbook.R
import app.ministrylogbook.shared.lastDayOfMonth
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

class ReminderReceiver : BroadcastReceiver() {

    private val reminderManager by lazy { ReminderManager() }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.sendReminderNotification(context, ReminderChannelId)

        // schedule next reminder
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val nextMonth = today + DatePeriod(months = 1)
        reminderManager.scheduleReminder(nextMonth.lastDayOfMonth().atTime(20, 0))
    }
}

private fun NotificationManager.sendReminderNotification(context: Context, channelId: String) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val contentIntent =
        Intent(
            Intent.ACTION_VIEW,
            "ministrylogbook://share/?year=${today.year}&monthNumber=${today.monthNumber}".toUri(),
            context,
            MainActivity::class.java
        )
    val pendingIntent = TaskStackBuilder.create(context).run {
        addNextIntentWithParentStack(contentIntent)
        getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }
    val builder = Notification.Builder(context, channelId)
        .setContentTitle(context.getString(R.string.send_report))
        .setContentText(context.getString(R.string.send_report_reminder_description))
        .setSmallIcon(R.drawable.ic_event)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)

    notify(NotificationId, builder.build())
}

const val NotificationId = 1
