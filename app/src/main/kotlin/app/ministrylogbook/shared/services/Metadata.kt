package app.ministrylogbook.shared.services

import app.ministrylogbook.data.Design
import app.ministrylogbook.data.Role
import com.akuleshov7.ktoml.Toml
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

@Serializable
data class Metadata(
    val version: Int,
    val dateTime: LocalDateTime,
    val role: Role,
    val startOfPioneering: LocalDate?,
    val name: String,
    val design: Design,
    val precisionMode: Boolean,
    val sendReportReminder: Boolean
) {
    companion object {
        fun fromToml(raw: String): Metadata? {
            Toml.decodeFromString<Metadata>(raw).runCatching {
                return this
            }

            return null
        }
    }

    fun toToml() = Toml.encodeToString(this)
}
