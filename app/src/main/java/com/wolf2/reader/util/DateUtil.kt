package com.wolf2.reader.util

import com.wolf2.reader.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtil {

    fun getDate(timeMillis: Long): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    fun compareDate(dateStr: String): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val date = sdf.parse(dateStr)
        val calendarToday = Calendar.getInstance()
        val calendarTarget = Calendar.getInstance().apply { time = date }

        calendarToday.set(Calendar.HOUR_OF_DAY, 0)
        calendarToday.set(Calendar.MINUTE, 0)
        calendarToday.set(Calendar.SECOND, 0)
        calendarToday.set(Calendar.MILLISECOND, 0)

        calendarTarget.set(Calendar.HOUR_OF_DAY, 0)
        calendarTarget.set(Calendar.MINUTE, 0)
        calendarTarget.set(Calendar.SECOND, 0)
        calendarTarget.set(Calendar.MILLISECOND, 0)

        val todayInMillis = calendarToday.timeInMillis
        val targetInMillis = calendarTarget.timeInMillis

        return when {
            targetInMillis == todayInMillis -> globalContext.getString(R.string.today)
            targetInMillis == todayInMillis - 86400000 -> globalContext.getString(R.string.yesterday)
            else -> {
                val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                sdf.format(date)
            }
        }
    }

    fun getTime(timeMillis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    fun getMinutes(durationMillis: Long): Long {
        return durationMillis / 1000 / 60
    }
}

