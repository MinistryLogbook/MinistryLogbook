package app.ministrylogbook.data

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.atTime

class LocalDateTimeConverters {
    @TypeConverter
    fun toDateTime(dateString: String?): LocalDateTime? {
        return if (dateString == null) {
            null
        } else {
            LocalDateTime.runCatching {
                parse(dateString)
            }.recover {
                // beta.1 had date instead of datetime
                LocalDate.parse(dateString).atTime(0, 0)
            }.getOrThrow()
        }
    }

    @TypeConverter
    fun toDateTimeString(date: LocalDateTime?): String? {
        return date?.toString()
    }
}
