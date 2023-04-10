package com.github.danieldaeschle.ministrynotes

import android.app.Application
import com.github.danieldaeschle.ministrynotes.data.BibleStudyEntryRepository
import com.github.danieldaeschle.ministrynotes.data.EntryRepository
import com.github.danieldaeschle.ministrynotes.data.SettingsDataStore
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.EntryDetailsViewModel
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.HomeViewModel
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.StudiesDetailsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { EntryRepository(androidContext()) }
    single { BibleStudyEntryRepository(androidContext()) }
    single { SettingsDataStore(androidContext()) }
    viewModel { params ->
        HomeViewModel(params.get(), androidContext() as Application, get(), get(), get())
    }
    viewModel { params -> StudiesDetailsViewModel(params.get(), get()) }
    viewModel { params -> EntryDetailsViewModel(params.getOrNull(), get()) }
}