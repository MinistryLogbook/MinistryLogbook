package app.ministrylogbook.shared.services

import app.ministrylogbook.data.Design
import app.ministrylogbook.data.Role
import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlOutputConfig
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

@Serializable
data class Metadata(
    val version: Int,
    val datetime: LocalDateTime,
    val role: Role,
    val startOfPioneering: LocalDate?,
    val name: String,
    val design: Design,
    val precisionMode: Boolean,
    val sendReportReminder: Boolean
) {
    companion object {
        private val toml = Toml(outputConfig = TomlOutputConfig(ignoreNullValues = false))

        fun fromToml(raw: String): Metadata? {
            toml.runCatching {
                return decodeFromString<Metadata>(raw)
            }

            return null
        }
    }

    fun toToml() = toml.encodeToString(this)
}
