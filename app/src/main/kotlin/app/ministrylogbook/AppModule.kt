package app.ministrylogbook

import android.app.Application
import androidx.room.Room
import app.ministrylogbook.data.AppDatabase
import app.ministrylogbook.data.AppMonthlyInformationRepository
import app.ministrylogbook.data.BibleStudyRepository
import app.ministrylogbook.data.DatabaseChangeNotifier
import app.ministrylogbook.data.EntryDetailsRepository
import app.ministrylogbook.data.EntryDetailsSettings
import app.ministrylogbook.data.EntryRepository
import app.ministrylogbook.data.HomeBibleStudyRepository
import app.ministrylogbook.data.HomeEntryRepository
import app.ministrylogbook.data.HomeMonthlyInformationRepository
import app.ministrylogbook.data.HomeSettings
import app.ministrylogbook.data.MonthlyInformationRepository
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.data.UserSettings
import app.ministrylogbook.shared.services.BackupService
import app.ministrylogbook.shared.services.HomeBackupService
import app.ministrylogbook.shared.services.ReminderManager
import app.ministrylogbook.shared.services.ReminderScheduler
import app.ministrylogbook.ui.home.backup.viewmodel.BackupViewModel
import app.ministrylogbook.ui.home.backup.viewmodel.BackupViewModelOptions
import app.ministrylogbook.ui.home.viewmodel.EntryDetailsViewModel
import app.ministrylogbook.ui.home.viewmodel.HomeViewModel
import app.ministrylogbook.ui.intro.viewmodel.IntroViewModel
import app.ministrylogbook.ui.settings.viewmodel.SettingsViewModel
import app.ministrylogbook.ui.share.viewmodel.ShareViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "db").build()
    }
    single { get<AppDatabase>().monthlyInformationDao() }
    single { get<AppDatabase>().entryDao() }
    single { get<AppDatabase>().studyDao() }
    single { DatabaseChangeNotifier() }
    single { EntryRepository(get(), get()) }
    single<EntryDetailsRepository> { get<EntryRepository>() }
    single<HomeEntryRepository> { get<EntryRepository>() }
    single { BibleStudyRepository(get(), get()) }
    single<HomeBibleStudyRepository> { get<BibleStudyRepository>() }
    single { MonthlyInformationRepository(get(), get()) }
    single<AppMonthlyInformationRepository> { get<MonthlyInformationRepository>() }
    single<HomeMonthlyInformationRepository> { get<MonthlyInformationRepository>() }
    single { SettingsService(androidContext()) }
    single<UserSettings> { get<SettingsService>() }
    single<EntryDetailsSettings> { get<SettingsService>() }
    single<HomeSettings> { get<SettingsService>() }
    single { ReminderManager(androidContext()) }
    single<ReminderScheduler> { get<ReminderManager>() }
    single { BackupService(androidContext(), get(), get(), get()) }
    single<HomeBackupService> { get<BackupService>() }
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
    viewModel { params ->
        BackupViewModel(
            androidContext() as Application,
            get(),
            get(),
            get(),
            params.getOrNull<BackupViewModelOptions>() ?: BackupViewModelOptions()
        )
    }
    viewModel { params -> SettingsViewModel(get(), get(), get(), params.getOrNull()) }
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
