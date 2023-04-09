package com.github.danieldaeschle.ministrynotes

import android.app.Application
import com.github.danieldaeschle.ministrynotes.data.settingsDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val settingsDataStore = settingsDataStore()

        runBlocking {
            launch {
                settingsDataStore.design.firstOrNull()?.apply()
            }
        }

        startKoin {
            androidLogger()
            androidContext(this@AppApplication)
            modules(appModule)
        }
    }
}