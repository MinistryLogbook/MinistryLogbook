package app.ministrylogbook

import app.ministrylogbook.lib.Time
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeTest {
    @Test
    fun negative_plus_positive_isZero() {
        val negativeTime = Time(hours = 1, minutes = 50, isNegative = true)
        val positiveTime = Time(hours = 1, minutes = 50)

        val result = negativeTime + positiveTime

        assertEquals(Time.Empty, result)
    }

    @Test
    fun negative_plus_negative_isNegative() {
        val negativeTime = Time(hours = 1, minutes = 50, isNegative = true)
        val positiveTime = Time(hours = 1, minutes = 50, isNegative = true)

        val result = negativeTime + positiveTime

        assertEquals(Time(hours = 3, minutes = 40, isNegative = true), result)
    }

    @Test
    fun negative_isSmallerThanPositive() {
        val negativeTime = Time(hours = 1, minutes = 50, isNegative = true)
        val positiveTime = Time(hours = 1, minutes = 50)

        assertTrue(negativeTime < positiveTime)
    }

    @Test
    fun positive_isGreaterThan_negative() {
        val negativeTime = Time(hours = 1, minutes = 50, isNegative = true)
        val positiveTime = Time(hours = 1, minutes = 50)

        assertTrue(positiveTime > negativeTime)
    }

    @Test
    fun positive_isEqualTo_positive() {
        val negativeTime1 = Time(hours = 1, minutes = 50)
        val negativeTime2 = Time(hours = 1, minutes = 50)

        assertTrue(negativeTime1 == negativeTime2)
    }

    @Test
    fun negative_isEqualTo_negative() {
        val negativeTime1 = Time(hours = 1, minutes = 50, isNegative = true)
        val negativeTime2 = Time(hours = 1, minutes = 50, isNegative = true)

        assertTrue(negativeTime1 == negativeTime2)
    }
}