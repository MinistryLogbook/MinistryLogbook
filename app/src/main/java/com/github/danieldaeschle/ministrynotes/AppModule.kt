package com.github.danieldaeschle.ministrynotes

import com.github.danieldaeschle.ministrynotes.data.EntryRepository
import com.github.danieldaeschle.ministrynotes.data.SettingsDataStore
import com.github.danieldaeschle.ministrynotes.data.StudyEntryRepository
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.EntryDetailsViewModel
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.HomeViewModel
import com.github.danieldaeschle.ministrynotes.ui.home.viewmodels.StudiesDetailsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { EntryRepository(androidContext()) }
    single { StudyEntryRepository(androidContext()) }
    single { SettingsDataStore(androidContext()) }
    viewModel { params ->
        HomeViewModel(params.get(), get(), get(), get())
    }
    viewModel { params -> StudiesDetailsViewModel(params.get(), get()) }
    viewModel { params -> EntryDetailsViewModel(params.getOrNull(), get()) }
}