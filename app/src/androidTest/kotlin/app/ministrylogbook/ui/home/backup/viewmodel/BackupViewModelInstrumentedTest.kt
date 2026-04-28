package app.ministrylogbook.ui.home.backup.viewmodel

import android.app.Application
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ministrylogbook.data.AppDatabase
import app.ministrylogbook.data.DatabaseChangeNotifier
import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryRepository
import app.ministrylogbook.data.SettingsService
import app.ministrylogbook.shared.services.BackupService
import java.io.File
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupViewModelInstrumentedTest {
    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val databaseName = "backup-viewmodel.db"
    private val backupFile = File(application.cacheDir, "backup-viewmodel-test.mlbak")
    private var sourceDb: AppDatabase? = null
    private var targetDb: AppDatabase? = null

    @After
    fun tearDown() {
        sourceDb?.close()
        targetDb?.close()
        application.deleteDatabase(databaseName)
        backupFile.delete()
    }

    @Test
    fun importBackupFromIntroMarksIntroShownWithoutLatestEntryObserver() = runBlocking {
        val sourceDatabase = Room.databaseBuilder(application, AppDatabase::class.java, databaseName).build()
        sourceDb = sourceDatabase
        sourceDatabase.entryDao().upsert(
            Entry(
                datetime = LocalDateTime(2026, 4, 28, 14, 30),
                hours = 2
            )
        )
        val settingsService = SettingsService(application)
        settingsService.setName("Restored user")
        BackupService(
            application,
            sourceDatabase,
            settingsService,
            DatabaseChangeNotifier()
        ).createBackup(Uri.fromFile(backupFile))
        sourceDatabase.close()
        sourceDb = null
        application.deleteDatabase(databaseName)

        val targetDatabase = Room.databaseBuilder(application, AppDatabase::class.java, databaseName).build()
        targetDb = targetDatabase
        val viewModel = BackupViewModel(
            application,
            BackupService(application, targetDatabase, settingsService, DatabaseChangeNotifier()),
            settingsService,
            EntryRepository(targetDatabase.entryDao(), DatabaseChangeNotifier()),
            BackupViewModelOptions(
                markIntroShownAfterImport = true,
                observeLatestEntry = false
            )
        )
        val collectJob = launch {
            viewModel.state.collect {}
        }

        viewModel.dispatch(BackupIntent.SelectBackupFile(Uri.fromFile(backupFile)))
        waitUntil { viewModel.state.value.selectedBackupFile != null }
        viewModel.dispatch(BackupIntent.ImportBackup)
        waitUntil { viewModel.state.value.importFinished }

        assertTrue(settingsService.introShown.first())
        assertEquals(
            2,
            targetDatabase.entryDao()
                .getLatest()
                .first()
                ?.hours
        )
        collectJob.cancelAndJoin()
    }

    private suspend fun waitUntil(predicate: () -> Boolean) {
        withTimeout(5_000) {
            while (!predicate()) {
                delay(50)
            }
        }
    }
}
