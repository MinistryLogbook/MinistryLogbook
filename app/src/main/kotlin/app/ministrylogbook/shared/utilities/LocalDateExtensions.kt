package app.ministrylogbook.shared.utilities

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

val LocalDate.lastDayOfMonth: LocalDate
    get() = LocalDate(this.year, this.monthNumber, 1) + DatePeriod(months = 1) - DatePeriod(days = 1)

val LocalDate.weekNumber: Int
    get() {
        val firstDayOfYear = LocalDate(year, 1, 1)
        val daysFromFirstDay = dayOfYear - firstDayOfYear.dayOfYear
        val firstDayOfYearDayOfWeek = firstDayOfYear.dayOfWeek.value
        val adjustment = when {
            firstDayOfYearDayOfWeek <= 4 -> firstDayOfYearDayOfWeek - 1
            else -> 8 - firstDayOfYearDayOfWeek
        }
        return (daysFromFirstDay + adjustment) / 7 + 1
    }
