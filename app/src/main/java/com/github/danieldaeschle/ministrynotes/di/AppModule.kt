package com.github.danieldaeschle.ministrynotes.di

import com.github.danieldaeschle.ministrynotes.data.EntryRepository
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
    viewModel { params ->
        HomeViewModel(year = params[0], monthNumber = params[1], get(), get())
    }
    viewModel { params -> StudiesDetailsViewModel(params[0], params[1], get()) }
    viewModel { EntryDetailsViewModel(get()) }
}