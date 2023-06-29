package app.ministrylogbook.shared.utilities

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

fun LocalDate.lastDayOfMonth() =
    LocalDate(this.year, this.monthNumber, 1) + DatePeriod(months = 1) - DatePeriod(days = 1)
