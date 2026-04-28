package app.ministrylogbook

import app.ministrylogbook.data.Design
import app.ministrylogbook.data.Role
import app.ministrylogbook.shared.services.Metadata
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MetadataTest {
    @Test
    fun metadata_roundTripsThroughToml() {
        val metadata = Metadata(
            version = 1,
            datetime = LocalDateTime(2026, 4, 27, 11, 45),
            role = Role.RegularPioneer,
            startOfPioneering = LocalDate(2024, 9, 1),
            name = "Ada",
            design = Design.Dark,
            precisionMode = true,
            sendReportReminder = false
        )

        val restored = Metadata.fromToml(metadata.toToml())

        assertEquals(metadata, restored)
    }

    @Test
    fun metadata_preservesNullStartOfPioneering() {
        val metadata = Metadata(
            version = 1,
            datetime = LocalDateTime(2026, 4, 27, 11, 45),
            role = Role.Publisher,
            startOfPioneering = null,
            name = "",
            design = Design.System,
            precisionMode = false,
            sendReportReminder = true
        )

        val restored = Metadata.fromToml(metadata.toToml())

        assertEquals(metadata, restored)
    }

    @Test
    fun metadata_returnsNullForInvalidToml() {
        assertNull(Metadata.fromToml("not valid toml"))
    }
}
