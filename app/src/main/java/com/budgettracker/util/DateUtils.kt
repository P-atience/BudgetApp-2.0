package com.budgettracker.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val DISPLAY_DATE_FORMAT = "dd MMM yyyy"
    const val TIME_FORMAT = "HH:mm"
    const val MONTH_FORMAT = "yyyy-MM"

    fun today(): String = formatDate(Date())
    fun currentMonth(): String = SimpleDateFormat(MONTH_FORMAT, Locale.getDefault()).format(Date())
    fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
    fun currentMonthInt(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1

    fun formatDate(date: Date): String =
        SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date)

    fun parseDate(dateStr: String): Date? = try {
        SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).parse(dateStr)
    } catch (e: Exception) { null }

    fun displayDate(dateStr: String): String {
        val date = parseDate(dateStr) ?: return dateStr
        return SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault()).format(date)
    }

    fun getMonthStartEnd(year: Int, month: Int): Pair<String, String> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        val start = formatDate(cal.time)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = formatDate(cal.time)
        return Pair(start, end)
    }

    fun getDateRange(startStr: String, endStr: String): Pair<String, String> =
        Pair(startStr, endStr)

    fun monthName(month: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, month - 1)
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
    }
}
