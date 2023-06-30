package app.ministrylogbook

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import app.ministrylogbook.data.AppDatabase
import app.ministrylogbook.data.EntryRepository
import app.ministrylogbook.data.MonthlyInformationRepository
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.shared.services.ReminderManager
import app.ministrylogbook.shared.services.BackupService
import app.ministrylogbook.ui.home.backup.viewmodel.BackupViewModel
import app.ministrylogbook.ui.home.viewmodel.EntryDetailsViewModel
import app.ministrylogbook.ui.home.viewmodel.HomeViewModel
import app.ministrylogbook.ui.home.viewmodel.OverviewViewModel
import app.ministrylogbook.ui.home.viewmodel.StudiesDetailsViewModel
import app.ministrylogbook.ui.intro.viewmodel.IntroViewModel
import app.ministrylogbook.ui.settings.viewmodel.SettingsViewModel
import app.ministrylogbook.ui.share.viewmodel.ShareViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "db").addCallback(object : RoomDatabase.Callback() {
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
            }
        }).build()
    }
    single { get<AppDatabase>().bibleStudyEntryDao() }
    single { get<AppDatabase>().entryDao() }
    single { EntryRepository(get()) }
    single { MonthlyInformationRepository(get()) }
    single { SettingsService(androidContext()) }
    single { ReminderManager() }
    single { BackupService(androidContext(), get(), get()) }
    viewModel { params ->
        OverviewViewModel(params.get(), androidContext() as Application, get(), get(), get())
    }
    viewModel { params -> StudiesDetailsViewModel(params.get(), get()) }
    viewModel { params -> EntryDetailsViewModel(params.get(), params.getOrNull(), get(), get()) }
    viewModel { params ->
        ShareViewModel(
            params.get(),
            androidContext() as Application,
            get(),
            get(),
            get()
        )
    }
    viewModel { BackupViewModel(androidContext() as Application, get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { IntroViewModel(get(), get(), get()) }
}
