package android.boot.common.extensions

import android.util.Log
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.Month
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

val LocalDateTime.startOfDayTimeMills: Long
    get() {
        val startOfDay = LocalDateTime.of(this.toLocalDate(), LocalTime.MIN)
        return startOfDay.toTimestamp
    }
val LocalDateTime.endOfDayTimeMills: Long
    get() {
        val endOfDay = LocalDateTime.of(this.toLocalDate(), LocalTime.MAX)
        return endOfDay.toTimestamp
    }

val LocalDateTime.toTimestamp: Long
    get() {
        val zoneDateTime = ZonedDateTime.of(this, ZoneId.systemDefault())
        return zoneDateTime.toInstant().toEpochMilli().also {
            Log.i("_LDT", "${this}->$it")
        }
    }

val yearLifespanTimestamp: Pair<Long, Long>
    get() {
        val year = LocalDateTime.now().year
        val startOfYear: LocalDateTime =
            LocalDateTime.of(LocalDateTime.now().year, Month.JANUARY, 1, 0, 0)

        // 获取今年的结束时间
        val endOfYear: LocalDateTime =
            LocalDateTime.of(LocalDateTime.now().year, Month.DECEMBER, 31, 23, 59, 59)

        // 获取系统默认时区
        val systemTimeZone = ZoneId.systemDefault()

        // 转换为ZonedDateTime
        val startOfThisYear = startOfYear.atZone(systemTimeZone)
        val endOfThisYear = endOfYear.atZone(systemTimeZone)

        val startTimestamp = startOfThisYear.toEpochSecond() * 1000
        val endTimestamp = endOfThisYear.toEpochSecond() * 1000
        return Pair(startTimestamp, endTimestamp)
    }