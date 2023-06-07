package app.ministrylogbook.lib

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import app.ministrylogbook.R
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

fun Context.shareBitmap(bitmap: Bitmap, fileName: String, subject: String? = null) {
    val fileNameWithExtension = if (fileName.endsWith(".png")) {
        fileName
    } else {
        "$fileName.png"
    }

    // Save bitmap to external cache directory
    // get cache directory
    val cachePath = File(externalCacheDir, "images/")
    cachePath.mkdirs()

    // cleanup directory
    cachePath.listFiles()?.forEach { file ->
        file.delete()
    }

    // create png file
    val file = File(cachePath, fileNameWithExtension)
    val fileOutputStream: FileOutputStream
    try {
        fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    // get file uri
    val fileUri = FileProvider.getUriForFile(
        this,
        applicationContext.packageName + ".provider",
        file
    )

    // create a intent
    val intent = Intent(Intent.ACTION_SEND).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        putExtra(Intent.EXTRA_STREAM, fileUri)
        subject?.let {
            putExtra(Intent.EXTRA_SUBJECT, it)
        }
        type = "image/png"
    }

    startActivity(Intent.createChooser(intent, getString(R.string.share_with)))
}
