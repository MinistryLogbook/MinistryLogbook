package app.ministrylogbook.shared.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.ministrylogbook.shared.utilities.lastDayOfMonth
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.todayIn

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            ReminderManager(context.applicationContext).scheduleReminder(today.lastDayOfMonth.atTime(20, 0))
        }
    }
}
