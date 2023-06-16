package app.ministrylogbook.shared

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.preferencesDataStoreFile
import app.ministrylogbook.data.AppDatabase
import app.ministrylogbook.data.SettingsDataStore
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.koin.core.component.KoinComponent

class BackupService(private val context: Context, private val db: AppDatabase) : KoinComponent {

    companion object {
        const val Version = 1
    }

    private val files by lazy {
        listOfNotNull(
            db.openHelper.writableDatabase.path?.let { File(it) },
            context.preferencesDataStoreFile(SettingsDataStore.Name)
        )
    }

    fun createBackup(uri: Uri, buffer: Int = 1024) {
        val outputStream = context.contentResolver.openOutputStream(uri) ?: return
        val out = ZipOutputStream(BufferedOutputStream(outputStream))

        files.filter { it.exists() }.forEach { file ->
            val inputStream = file.inputStream()
            val origin = BufferedInputStream(inputStream, buffer)
            val entry = ZipEntry(file.name)

            out.putNextEntry(entry)
            origin.copyTo(out, buffer)
            out.closeEntry()
            origin.close()
        }

        out.close()
    }

    fun importBackup() {
    }
}
