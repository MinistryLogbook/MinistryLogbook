package app.ministrylogbook

import android.app.Application
import app.ministrylogbook.data.SettingsDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val settingsDataStore = SettingsDataStore(this)

        runBlocking {
            settingsDataStore.design.firstOrNull()?.apply()
        }

        startKoin {
            androidLogger()
            androidContext(this@AppApplication)
            modules(appModule)
        }
    }
}