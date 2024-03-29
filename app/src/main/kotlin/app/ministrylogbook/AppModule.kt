package app.ministrylogbook

import android.app.Application
import androidx.room.Room
import app.ministrylogbook.data.AppDatabase
import app.ministrylogbook.data.BibleStudyRepository
import app.ministrylogbook.data.EntryRepository
import app.ministrylogbook.data.MonthlyInformationRepository
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.shared.services.BackupService
import app.ministrylogbook.shared.services.ReminderManager
import app.ministrylogbook.ui.home.backup.viewmodel.BackupViewModel
import app.ministrylogbook.ui.home.viewmodel.EntryDetailsViewModel
import app.ministrylogbook.ui.home.viewmodel.HomeViewModel
import app.ministrylogbook.ui.intro.viewmodel.IntroViewModel
import app.ministrylogbook.ui.settings.viewmodel.SettingsViewModel
import app.ministrylogbook.ui.share.viewmodel.ShareViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "db").build()
    }
    single { get<AppDatabase>().monthlyInformationDao() }
    single { get<AppDatabase>().entryDao() }
    single { get<AppDatabase>().studyDao() }
    single { EntryRepository(get()) }
    single { BibleStudyRepository(get()) }
    single { MonthlyInformationRepository(get()) }
    single { SettingsService(androidContext()) }
    single { ReminderManager() }
    single { BackupService(androidContext(), get(), get()) }
    viewModel { params -> EntryDetailsViewModel(params.get(), params.getOrNull(), get(), get()) }
    viewModel { params ->
        ShareViewModel(
            params.get(),
            androidContext() as Application,
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel { BackupViewModel(androidContext() as Application, get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { params ->
        HomeViewModel(
            params.get(),
            params.getOrNull(),
            androidContext() as Application,
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel { IntroViewModel(get(), get(), get()) }
}
