package com.github.danieldaeschle.ministrynotes.lib


data class Time(val hours: Int = 0, val minutes: Int = 0) : Comparable<Time> {
    companion object {
        val Empty = Time()
    }

    operator fun plus(other: Time): Time {
        var accumulatedHours = this.hours + other.hours
        var accumulatedMinutes = this.minutes + other.minutes
        accumulatedHours += accumulatedMinutes / 60
        accumulatedMinutes %= 60
        return Time(accumulatedHours, accumulatedMinutes)
    }

    operator fun minus(other: Time): Time {
        var accumulatedHours = this.hours - other.hours
        var accumulatedMinutes = this.minutes - other.minutes
        if (accumulatedMinutes < 0) {
            accumulatedHours -= 1
            accumulatedMinutes += 60
        }
        return Time(accumulatedHours, accumulatedMinutes)
    }

    override fun compareTo(other: Time): Int {
        val res = this.hours.compareTo(other.hours)
        if (res == 0) {
            return this.minutes.compareTo(other.minutes)
        }
        return res
    }

    fun isEmpty() = this == Empty

    fun isNotEmpty() = this != Empty
}