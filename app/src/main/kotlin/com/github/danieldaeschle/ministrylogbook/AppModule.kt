package com.github.danieldaeschle.ministrylogbook

import android.app.Application
import androidx.room.Room
import com.github.danieldaeschle.ministrylogbook.data.AppDatabase
import com.github.danieldaeschle.ministrylogbook.data.BibleStudyEntryRepository
import com.github.danieldaeschle.ministrylogbook.data.EntryRepository
import com.github.danieldaeschle.ministrylogbook.data.SettingsDataStore
import com.github.danieldaeschle.ministrylogbook.ui.home.viewmodel.EntryDetailsViewModel
import com.github.danieldaeschle.ministrylogbook.ui.home.viewmodel.HomeViewModel
import com.github.danieldaeschle.ministrylogbook.ui.home.viewmodel.StudiesDetailsViewModel
import com.github.danieldaeschle.ministrylogbook.ui.share.viewmodel.ShareViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "db").build()
    }
    single { get<AppDatabase>().bibleStudyEntryDao() }
    single { get<AppDatabase>().entryDao() }
    single { EntryRepository(get()) }
    single { BibleStudyEntryRepository(get()) }
    single { SettingsDataStore(androidContext()) }
    viewModel { params ->
        HomeViewModel(params.get(), androidContext() as Application, get(), get())
    }
    viewModel { params -> StudiesDetailsViewModel(params.get(), get()) }
    viewModel { params -> EntryDetailsViewModel(params.get(), params.getOrNull(), get()) }
    viewModel { params ->
        ShareViewModel(
            params.get(),
            androidContext() as Application,
            get(),
            get(),
            get(),
        )
    }
}