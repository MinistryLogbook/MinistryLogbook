package app.ministrylogbook.shared.services

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import app.ministrylogbook.data.AppDatabase
import app.ministrylogbook.data.SettingsService
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent

class BackupService(
    private val context: Context,
    private val db: AppDatabase,
    private val settingsService: SettingsService
) : KoinComponent {

    companion object {
        const val VERSION = 1
        const val METADATA_FILE_NAME = "metadata.toml"
    }

    private val files by lazy {
        listOfNotNull(
            db.openHelper.readableDatabase.path?.let { File(it) },
            db.openHelper.readableDatabase.path?.let { File("$it-wal") },
            db.openHelper.readableDatabase.path?.let { File("$it-shm") }
        )
    }

    suspend fun createBackup(uri: Uri) {
        val outputStream = context.contentResolver.openOutputStream(uri) ?: return
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

    suspend fun importBackup(uri: Uri): Boolean {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return false
        val origin = BufferedInputStream(inputStream)
        val zip = ZipInputStream(origin)

        var entry = zip.nextEntry
        var metadata: Metadata? = null
        while (entry != null) {
            val file = files.find { it.name == entry.name }

            if (file != null) {
                val backupFile = File(file.path + ".bak")
                file.copyTo(backupFile, true)
                file.delete()
                val outputStream = file.outputStream()
                val out = BufferedOutputStream(outputStream)

                zip.copyTo(out)
                out.close()
            } else if (entry.name == METADATA_FILE_NAME) {
                metadata = Metadata.fromToml(zip.readBytes().decodeToString())
            }
            entry = zip.nextEntry
        }

        zip.close()

        metadata?.let {
            importSettings(it)
        }

        if (!verifyDatabase()) {
            recover()
            return false
        }

        return true
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

    private fun verifyDatabase(): Boolean {
        try {
            val db = SQLiteDatabase.openDatabase(
                db.openHelper.readableDatabase.path!!,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            db.rawQuery("SELECT * from entry LIMIT 1", arrayOf()).close()
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun recover() {
        files.forEach { file ->
            val backupFile = File(file.path + ".bak")
            backupFile.copyTo(file, true)
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
}
