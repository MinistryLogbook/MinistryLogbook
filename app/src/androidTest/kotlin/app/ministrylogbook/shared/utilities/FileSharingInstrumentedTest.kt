package app.ministrylogbook.shared.utilities

import android.content.Context
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileSharingInstrumentedTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val imageDirectory = File(context.externalCacheDir, "images")

    @After
    fun tearDown() {
        imageDirectory.deleteRecursively()
    }

    @Test
    fun fileProviderSharesReportImageFromExternalCacheImagesDirectory() {
        imageDirectory.mkdirs()
        val imageBytes = byteArrayOf(1, 2, 3, 4)
        val imageFile = File(imageDirectory, "report.png")
        imageFile.writeBytes(imageBytes)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )

        assertEquals("content", uri.scheme)
        assertTrue(uri.authority!!.endsWith(".provider"))
        val restoredBytes = context.contentResolver.openInputStream(uri)!!.use { input ->
            input.readBytes()
        }
        assertArrayEquals(imageBytes, restoredBytes)
    }
}
