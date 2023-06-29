package app.ministrylogbook.shared.services

import android.content.Context
import android.net.Uri
import app.ministrylogbook.data.AppDatabase
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import org.koin.core.component.KoinComponent

class BackupService(private val context: Context, private val db: AppDatabase) : KoinComponent {

    companion object {
        const val Version = 1
    }

    private val files by lazy {
        listOfNotNull(
            db.openHelper.readableDatabase.path?.let { File(it) },
            db.openHelper.readableDatabase.path?.let { File("$it-wal") },
            db.openHelper.readableDatabase.path?.let { File("$it-shm") }
        )
    }

    fun createBackup(uri: Uri, settingsYaml: String) {
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

        val settingsFileName = "settings.yaml"
        val settingsEntry = ZipEntry(settingsFileName)
        out.putNextEntry(settingsEntry)
        out.write(settingsYaml.toByteArray())
        out.closeEntry()

        out.close()
    }

    /**
     * Import a backup file.
     *
     * @param uri The URI of the backup file.
     * @return The settings YAML.
     */
    fun importBackup(uri: Uri): String? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val origin = BufferedInputStream(inputStream)
        val zip = ZipInputStream(origin)

        var entry = zip.nextEntry
        var settings: String? = null
        while (entry != null) {
            val file = files.find { it.name == entry.name }

            if (file != null) {
                file.delete()
                val outputStream = file.outputStream()
                val out = BufferedOutputStream(outputStream)

                zip.copyTo(out)
                out.close()
            } else if (entry.name == "settings.yaml") {
                settings = zip.readBytes().decodeToString()
            }
            entry = zip.nextEntry
        }

        zip.close()
        return settings
    }
}
