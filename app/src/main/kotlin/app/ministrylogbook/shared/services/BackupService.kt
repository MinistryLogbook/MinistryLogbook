package app.ministrylogbook.shared.services

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import app.ministrylogbook.data.AppDatabase
import app.ministrylogbook.data.DatabaseChangeNotifier
import app.ministrylogbook.data.SettingsService
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent

class BackupService(
    private val context: Context,
    private val db: AppDatabase,
    private val settingsService: SettingsService,
    private val databaseChangeNotifier: DatabaseChangeNotifier
) : KoinComponent {

    companion object {
        const val VERSION = 1
        const val METADATA_FILE_NAME = "metadata.toml"
    }

    private val databaseTables = listOf("Entry", "BibleStudy", "MonthlyInformation")

    private val files by lazy {
        listOfNotNull(
            db.openHelper.readableDatabase.path?.let { File(it) },
            db.openHelper.readableDatabase.path?.let { File("$it-wal") },
            db.openHelper.readableDatabase.path?.let { File("$it-shm") }
        )
    }

    suspend fun createBackup(uri: Uri) = withContext(Dispatchers.IO) {
        val outputStream = context.contentResolver.openOutputStream(uri) ?: return@withContext
        val out = ZipOutputStream(BufferedOutputStream(outputStream))

        files.filter { it.exists() }.forEach { file ->
            val inputStream = file.inputStream()
            val origin = BufferedInputStream(inputStream)
            val entry = ZipEntry(file.name)

            out.putNextEntry(entry)
            origin.copyTo(out)
            out.closeEntry()
            origin.close()
        }

        val metadataEntry = ZipEntry(METADATA_FILE_NAME)
        out.putNextEntry(metadataEntry)
        val metadata = Metadata(
            version = VERSION,
            datetime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            role = settingsService.role.first(),
            startOfPioneering = settingsService.pioneerSince.first(),
            name = settingsService.name.first(),
            design = settingsService.design.first(),
            precisionMode = settingsService.precisionMode.first(),
            sendReportReminder = settingsService.sendReportReminder.first()
        )
        val content = metadata.toToml().toByteArray().decodeToString()
        out.write(content.toByteArray())
        out.closeEntry()
        out.close()
    }

    suspend fun importBackup(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext false
        val origin = BufferedInputStream(inputStream)
        val zip = ZipInputStream(origin)
        val restoreDirectory = File(context.cacheDir, "restore-${UUID.randomUUID()}")
        restoreDirectory.mkdirs()

        var entry = zip.nextEntry
        var metadata: Metadata? = null
        while (entry != null) {
            val databaseFile = files.find { it.name == entry.name }

            if (databaseFile != null) {
                val restoreFile = File(restoreDirectory, databaseFile.name)
                val outputStream = restoreFile.outputStream()
                val out = BufferedOutputStream(outputStream)

                zip.copyTo(out)
                out.close()
            } else if (entry.name == METADATA_FILE_NAME) {
                metadata = Metadata.fromToml(zip.readBytes().decodeToString())
            }
            entry = zip.nextEntry
        }

        zip.close()

        val restoredDatabaseFile = File(restoreDirectory, files.first().name)
        val isDatabaseValid = restoredDatabaseFile.exists() && verifyDatabase(restoredDatabaseFile)

        if (!isDatabaseValid) {
            restoreDirectory.deleteRecursively()
            return@withContext false
        }

        val migratedDatabase = migrateRestoredDatabase(restoredDatabaseFile)
        try {
            replaceDatabaseContents(migratedDatabase.file)
            databaseChangeNotifier.notifyDatabaseChanged()
        } finally {
            context.deleteDatabase(migratedDatabase.name)
        }

        metadata?.let {
            importSettings(it)
        }

        restoreDirectory.deleteRecursively()
        true
    }

    fun getBackupMetadata(uri: Uri): Metadata? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val origin = BufferedInputStream(inputStream)
        val zip = ZipInputStream(origin)

        var entry = zip.nextEntry
        var metadata: Metadata? = null
        while (entry != null) {
            if (entry.name == METADATA_FILE_NAME) {
                val content = zip.readBytes().decodeToString()
                metadata = Metadata.fromToml(content)
                break
            }
            entry = zip.nextEntry
        }

        zip.close()

        return metadata
    }

    fun validateBackup(uri: Uri): Boolean {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return false
        val origin = BufferedInputStream(inputStream)
        val zip = ZipInputStream(origin)
        val entries = arrayListOf<String>()

        var entry = zip.nextEntry
        while (entry != null) {
            entries.add(entry.name)
            entry = zip.nextEntry
        }

        zip.close()

        return files.all { file ->
            entries.any { file.name == it }
        }
    }

    private fun verifyDatabase(file: File): Boolean {
        try {
            val db = SQLiteDatabase.openDatabase(
                file.path,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            db.rawQuery("SELECT * from entry LIMIT 1", arrayOf()).close()
            db.close()
        } catch (_: Exception) {
            return false
        }
        return true
    }

    private fun migrateRestoredDatabase(restoredDatabaseFile: File): RestoredDatabase {
        val tempDatabaseName = "restore-${UUID.randomUUID()}.db"
        val tempDatabaseFile = context.getDatabasePath(tempDatabaseName)
        tempDatabaseFile.parentFile?.mkdirs()
        restoredDatabaseFile.copyTo(tempDatabaseFile, overwrite = true)
        File("${restoredDatabaseFile.path}-wal")
            .copyToIfExists(File("${tempDatabaseFile.path}-wal"))
        File("${restoredDatabaseFile.path}-shm")
            .copyToIfExists(File("${tempDatabaseFile.path}-shm"))

        val restoredDatabase = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            tempDatabaseName
        ).build()
        try {
            restoredDatabase.openHelper.writableDatabase.query("SELECT 1").close()
        } finally {
            restoredDatabase.close()
        }

        return RestoredDatabase(tempDatabaseFile, tempDatabaseName)
    }

    private fun File.copyToIfExists(target: File) {
        if (exists()) {
            copyTo(target, overwrite = true)
        }
    }

    private suspend fun replaceDatabaseContents(restoredDatabaseFile: File) {
        val writableDatabase = db.openHelper.writableDatabase
        val escapedPath = restoredDatabaseFile.path.replace("'", "''")

        writableDatabase.execSQL("ATTACH DATABASE '$escapedPath' AS restored")
        try {
            writableDatabase.beginTransaction()
            try {
                databaseTables.forEach { table ->
                    val columns = columnNames(writableDatabase, table)
                        .joinToString(", ") { "`$it`" }
                    writableDatabase.execSQL("DELETE FROM `$table`")
                    writableDatabase.execSQL(
                        "INSERT INTO `$table` ($columns) SELECT $columns FROM restored.`$table`"
                    )
                }
                writableDatabase.setTransactionSuccessful()
            } finally {
                writableDatabase.endTransaction()
            }
        } finally {
            writableDatabase.execSQL("DETACH DATABASE restored")
        }
    }

    private fun columnNames(database: SupportSQLiteDatabase, table: String) =
        database.query("PRAGMA table_info(`$table`)").use { cursor ->
            val nameColumnIndex = cursor.getColumnIndexOrThrow("name")
            buildList {
                while (cursor.moveToNext()) {
                    add(cursor.getString(nameColumnIndex))
                }
            }
        }

    private suspend fun importSettings(metadata: Metadata) {
        settingsService.setRole(metadata.role)
        settingsService.setPioneerSince(metadata.startOfPioneering)
        settingsService.setName(metadata.name)
        settingsService.setDesign(metadata.design)
        settingsService.setPrecisionMode(metadata.precisionMode)
        settingsService.setSendReportReminders(metadata.sendReportReminder)
    }

    private data class RestoredDatabase(val file: File, val name: String)
}
