package com.github.danieldaeschle.ministrynotes.lib

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

fun Context.shareBitmap(bitmap: Bitmap) {
    // Save bitmap to external cache directory
    // get cache directory
    val cachePath = File(externalCacheDir, "images/")
    cachePath.mkdirs()

    // create png file
    val file = File(cachePath, "Image_123.png")
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
    val myImageFileUri = FileProvider.getUriForFile(
        this,
        applicationContext.packageName + ".provider",
        file
    )

    // create a intent
    val intent = Intent(Intent.ACTION_SEND)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.putExtra(Intent.EXTRA_STREAM, myImageFileUri)
    intent.type = "image/png"
    startActivity(Intent.createChooser(intent, "Share with"))
}