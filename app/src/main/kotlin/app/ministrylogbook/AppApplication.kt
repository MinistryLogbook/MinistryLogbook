package app.ministrylogbook

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.notifications.ReminderChannelId
import app.ministrylogbook.notifications.ReminderManager
import app.ministrylogbook.shared.lastDayOfMonth
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.todayIn
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AppApplication : Application() {

    companion object {
        var applicationScope = MainScope()
    }

    override fun onCreate() {
        super.onCreate()

        val koinApplication = startKoin {
            androidLogger()
            androidContext(this@AppApplication)
            modules(appModule)
        }

        val settingsDataStore = koinApplication.koin.get<SettingsService>()
        runBlocking {
            settingsDataStore.design.firstOrNull()?.apply()
        }

        createNotificationsChannels()
        applicationScope.launch {
            val shouldSendReportReminder = settingsDataStore.sendReportReminder.first()

            if (shouldSendReportReminder) {
                val reminderManager = koinApplication.koin.get<ReminderManager>()
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                reminderManager.scheduleReminder(today.lastDayOfMonth().atTime(20, 0))
            }
        }
    }

    private fun createNotificationsChannels() {
        val channel = NotificationChannel(
            ReminderChannelId,
            getString(R.string.reminders),
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
