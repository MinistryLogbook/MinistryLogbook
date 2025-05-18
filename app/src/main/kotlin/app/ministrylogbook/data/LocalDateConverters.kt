package app.ministrylogbook.data

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate

class LocalDateConverters {
    @TypeConverter
    fun toDate(dateString: String?): LocalDate? = if (dateString == null) {
        null
    } else {
        LocalDate.parse(dateString)
    }

    @TypeConverter
    fun toDateString(date: LocalDate?): String? = date?.toString()
}
