package app.ministrylogbook.shared.services

import android.net.Uri
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.ministrylogbook.data.AppDatabase
import app.ministrylogbook.data.DatabaseChangeNotifier
import app.ministrylogbook.data.Design
import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.EntryRepository
import app.ministrylogbook.data.Role
import app.ministrylogbook.data.SettingsService
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupServiceInstrumentedTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val databaseName = "backup-test.db"
    private val legacyDatabaseName = "backup-legacy-v1.db"
    private val backupFile = File(context.cacheDir, "backup-service-test.mlbak")
    private var db: AppDatabase? = null

    @After
    fun tearDown() {
        db?.close()
        context.deleteDatabase(databaseName)
        context.deleteDatabase(legacyDatabaseName)
        backupFile.delete()
    }

    @Test
    fun createBackup_writesDatabaseAndMetadataEntries() = runBlocking {
        val database = Room.databaseBuilder(context, AppDatabase::class.java, databaseName).build()
        db = database
        database.entryDao().upsert(
            Entry(
                datetime = LocalDateTime(2026, 4, 27, 9, 30),
                hours = 1,
                minutes = 15
            )
        )

        val settings = SettingsService(context)
        settings.setRole(Role.RegularPioneer)
        settings.setPioneerSince(LocalDate(2024, 9, 1))
        settings.setName("Ada")
        settings.setDesign(Design.Dark)
        settings.setPrecisionMode(true)
        settings.setSendReportReminders(false)

        val backupService = BackupService(context, database, settings, DatabaseChangeNotifier())
        val backupUri = Uri.fromFile(backupFile)

        backupService.createBackup(backupUri)

        assertTrue(backupFile.exists())
        assertTrue(backupService.validateBackup(backupUri))
        val metadata = backupService.getBackupMetadata(backupUri)
        assertNotNull(metadata)
        assertEquals(Role.RegularPioneer, metadata!!.role)
        assertEquals(LocalDate(2024, 9, 1), metadata.startOfPioneering)
        assertEquals("Ada", metadata.name)
        assertEquals(Design.Dark, metadata.design)
        assertEquals(true, metadata.precisionMode)
        assertEquals(false, metadata.sendReportReminder)
    }

    @Test
    fun importBackup_updatesActiveEntryObservers() = runBlocking {
        val database = Room.databaseBuilder(context, AppDatabase::class.java, databaseName).build()
        db = database
        val settings = SettingsService(context)
        val databaseChangeNotifier = DatabaseChangeNotifier()
        val backupService = BackupService(context, database, settings, databaseChangeNotifier)
        val entryRepository = EntryRepository(database.entryDao(), databaseChangeNotifier)
        val backupUri = Uri.fromFile(backupFile)
        val month = LocalDate(2026, 4, 1)
        val observedEntries = Channel<List<Entry>>(Channel.UNLIMITED)
        val observeJob = launch {
            entryRepository
                .getAllOfMonth(month)
                .collect { observedEntries.send(it) }
        }

        database.entryDao().upsert(
            Entry(
                datetime = LocalDateTime(2026, 4, 27, 9, 30),
                hours = 3
            )
        )
        waitUntilObserved(observedEntries, "initial backup data") { it.singleOrNull()?.hours == 3 }

        backupService.createBackup(backupUri)
        val currentEntries = database.entryDao()
            .getAllOfMonth(month.year, month.month.ordinal + 1)
            .first()
        database.entryDao().upsert(
            currentEntries.single().copy(hours = 9)
        )
        waitUntilObserved(observedEntries, "changed local data") { it.singleOrNull()?.hours == 9 }

        val imported = backupService.importBackup(backupUri)

        assertTrue(imported)
        assertEquals(
            3,
            database.entryDao()
                .getAllOfMonth(month.year, month.month.ordinal + 1)
                .first()
                .single()
                .hours
        )
        waitUntilObserved(observedEntries, "restored backup data") { it.singleOrNull()?.hours == 3 }
        observeJob.cancelAndJoin()
    }

    @Test
    fun importBackup_migratesOlderDatabaseBeforeCopyingRows() = runBlocking {
        val database = Room.databaseBuilder(context, AppDatabase::class.java, databaseName).build()
        db = database
        val backupService = BackupService(
            context,
            database,
            SettingsService(context),
            DatabaseChangeNotifier()
        )

        helper.createDatabase(legacyDatabaseName, 1).apply {
            insertVersion1BackupData()
            close()
        }
        writeDatabaseBackup(
            sourceDatabase = context.getDatabasePath(legacyDatabaseName),
            entryName = databaseName
        )

        val imported = backupService.importBackup(Uri.fromFile(backupFile))

        assertTrue(imported)
        database.openHelper.writableDatabase.query(
            "SELECT hours, minutes FROM Entry WHERE id = 1"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
            assertEquals(30, cursor.getInt(1))
        }
        database.openHelper.writableDatabase.query(
            """
            SELECT goal, report_comment, dismissed_bible_studies_hint,
                bible_studies_transferred, report_sent
            FROM MonthlyInformation
            WHERE id = 1
            """.trimIndent()
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(15, cursor.getInt(0))
            assertEquals("", cursor.getString(1))
            assertEquals(0, cursor.getInt(2))
            assertEquals(0, cursor.getInt(3))
            assertEquals(0, cursor.getInt(4))
        }
        database.openHelper.writableDatabase.query(
            "SELECT COUNT(*) FROM BibleStudy"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }

    private suspend fun waitUntilObserved(
        observedEntries: Channel<List<Entry>>,
        label: String,
        predicate: (List<Entry>) -> Boolean
    ) {
        try {
            withTimeout(5_000) {
                while (true) {
                    if (predicate(observedEntries.receive())) {
                        return@withTimeout
                    }
                }
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            throw AssertionError("Timed out waiting for $label", e)
        }
    }

    private fun SupportSQLiteDatabase.insertVersion1BackupData() {
        execSQL(
            """
            INSERT INTO Entry (
                id, datetime, placements, video_showings, hours, minutes,
                return_visits, type, transferred_from
            ) VALUES (
                1, '2026-04-27T08:15:00', 2, 1, 1, 30, 3, 'Ministry', NULL
            )
            """.trimIndent()
        )
        execSQL(
            """
            INSERT INTO MonthlyInformation (
                id, month, bible_studies, goal
            ) VALUES (
                1, '2026-04-01', 1, 15
            )
            """.trimIndent()
        )
    }

    private fun writeDatabaseBackup(sourceDatabase: File, entryName: String) {
        ZipOutputStream(backupFile.outputStream().buffered()).use { zip ->
            zip.putNextEntry(ZipEntry(entryName))
            sourceDatabase.inputStream().buffered().use { input ->
                input.copyTo(zip)
            }
            zip.closeEntry()
        }
    }
}
