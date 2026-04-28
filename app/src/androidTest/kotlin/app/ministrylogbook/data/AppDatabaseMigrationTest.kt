package app.ministrylogbook.data

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.IOException
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        context.deleteDatabase(TEST_DB)
    }

    @Test
    @Throws(IOException::class)
    fun migratesFromVersion1ToLatest() {
        helper.createDatabase(TEST_DB, 1).apply {
            insertVersion1Data()
            close()
        }

        withMigratedDatabase { db ->
            assertEquals(4, db.openHelper.readableDatabase.version)
            assertEntryCount(db.openHelper.readableDatabase)
            assertMonthlyInformationDefaults(db.openHelper.readableDatabase)
        }

        helper.runMigrationsAndValidate(TEST_DB, 4, true)
    }

    @Test
    @Throws(IOException::class)
    fun migratesFromVersion2ToLatest() {
        helper.createDatabase(TEST_DB, 2).close()

        withMigratedDatabase { db ->
            assertEquals(4, db.openHelper.readableDatabase.version)
            assertTableExists(db.openHelper.readableDatabase, "BibleStudy")
        }

        helper.runMigrationsAndValidate(TEST_DB, 4, true)
    }

    @Test
    @Throws(IOException::class)
    fun migratesFromVersion3ToLatest() {
        helper.createDatabase(TEST_DB, 3).close()

        withMigratedDatabase { db ->
            assertEquals(4, db.openHelper.readableDatabase.version)
            assertTableExists(db.openHelper.readableDatabase, "MonthlyInformation")
        }

        helper.runMigrationsAndValidate(TEST_DB, 4, true)
    }

    private fun openWithRoomAndRunAutoMigrations() = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        TEST_DB
    ).build().also {
        it.openHelper.writableDatabase.query("SELECT 1").close()
    }

    private fun withMigratedDatabase(block: (AppDatabase) -> Unit) {
        val db = openWithRoomAndRunAutoMigrations()
        try {
            block(db)
        } finally {
            db.close()
        }
    }

    private fun SupportSQLiteDatabase.insertVersion1Data() {
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

    private fun assertEntryCount(db: SupportSQLiteDatabase) {
        db.query("SELECT COUNT(*) FROM Entry").use { cursor ->
            assertNotNull(cursor)
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }
    }

    private fun assertMonthlyInformationDefaults(db: SupportSQLiteDatabase) {
        db.query(
            """
            SELECT report_comment, dismissed_bible_studies_hint,
                bible_studies_transferred, report_sent
            FROM MonthlyInformation
            WHERE id = 1
            """.trimIndent()
        ).use { cursor ->
            assertNotNull(cursor)
            cursor.moveToFirst()
            assertEquals("", cursor.getString(0))
            assertEquals(0, cursor.getInt(1))
            assertEquals(0, cursor.getInt(2))
            assertEquals(0, cursor.getInt(3))
        }
    }

    private fun assertTableExists(db: SupportSQLiteDatabase, tableName: String) {
        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
            arrayOf(tableName)
        ).use { cursor ->
            assertEquals(true, cursor.moveToFirst())
        }
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
