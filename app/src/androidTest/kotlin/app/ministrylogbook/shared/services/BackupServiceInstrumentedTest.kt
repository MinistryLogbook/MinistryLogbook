package app.ministrylogbook.shared.services

import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.ministrylogbook.data.AppDatabase
import app.ministrylogbook.data.Design
import app.ministrylogbook.data.Entry
import app.ministrylogbook.data.Role
import app.ministrylogbook.data.SettingsService
import java.io.File
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackupServiceInstrumentedTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val databaseName = "backup-test.db"
    private val backupFile = File(context.cacheDir, "backup-service-test.mlbak")
    private var db: AppDatabase? = null

    @After
    fun tearDown() {
        db?.close()
        context.deleteDatabase(databaseName)
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

        val backupService = BackupService(context, database, settings)
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
}
