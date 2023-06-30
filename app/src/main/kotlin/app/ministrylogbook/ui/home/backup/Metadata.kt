package app.ministrylogbook.ui.home.backup

import app.ministrylogbook.data.Design
import app.ministrylogbook.data.Role
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Metadata(
    val version: Int,
    val role: Role,
    val startOfPioneering: LocalDate?,
    val name: String,
    val design: Design,
    val precisionMode: Boolean,
    val sendReportReminder: Boolean
)
