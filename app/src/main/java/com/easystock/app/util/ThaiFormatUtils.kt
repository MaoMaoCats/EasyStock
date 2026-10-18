package com.easystock.app.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object ThaiFormatUtils {

    private val thaiLocale = Locale.forLanguageTag("th-TH")
    private val currencyFormat = DecimalFormat("#,##0.00")
    private val quantityFormat = DecimalFormat("#,##0.##")

    /**
     * Formats BigDecimal to Thai Baht string (e.g. "฿120.00" or "฿0.00")
     */
    fun formatBaht(amount: BigDecimal?): String {
        val value = amount ?: BigDecimal.ZERO
        return "฿${currencyFormat.format(value.setScale(2, RoundingMode.HALF_UP))}"
    }

    /**
     * Formats BigDecimal to clean quantity string (e.g. "12", "12.5", "0.75")
     */
    fun formatQuantity(qty: BigDecimal?): String {
        val value = qty ?: BigDecimal.ZERO
        return quantityFormat.format(value)
    }

    /**
     * Formats quantity with unit (e.g. "12.5 กก.", "5 หวี")
     */
    fun formatQuantityWithUnit(qty: BigDecimal?, unit: String): String {
        return "${formatQuantity(qty)} $unit"
    }

    /**
     * Formats epoch millis to Thai date string (e.g. "20 ต.ค. 2569")
     */
    fun formatThaiDate(timestamp: Long): String {
        val cal = Calendar.getInstance(thaiLocale)
        cal.timeInMillis = timestamp
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val monthStr = getThaiMonthShort(cal.get(Calendar.MONTH))
        val buddhistYear = cal.get(Calendar.YEAR) + (if (cal.get(Calendar.YEAR) < 2400) 543 else 0)
        return "$day $monthStr $buddhistYear"
    }

    /**
     * Formats epoch millis to full Thai date (e.g. "วันอังคารที่ 20 ตุลาคม 2569")
     */
    fun formatFullThaiDate(timestamp: Long): String {
        val cal = Calendar.getInstance(thaiLocale)
        cal.timeInMillis = timestamp
        val dayName = getThaiDayOfWeek(cal.get(Calendar.DAY_OF_WEEK))
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val monthStr = getThaiMonthFull(cal.get(Calendar.MONTH))
        val buddhistYear = cal.get(Calendar.YEAR) + (if (cal.get(Calendar.YEAR) < 2400) 543 else 0)
        return "วัน${dayName}ที่ $day $monthStr $buddhistYear"
    }

    /**
     * Formats time (e.g. "14:30 น.")
     */
    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return "${sdf.format(Date(timestamp))} น."
    }

    /**
     * Formats date to YYYY-MM-DD
     */
    fun toDateString(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(timestamp))
    }

    /**
     * Today's date string in YYYY-MM-DD
     */
    fun todayDateString(): String {
        return toDateString(System.currentTimeMillis())
    }

    /**
     * Converts YYYY-MM-DD to friendly Thai display (e.g. "วันนี้", "เมื่อวาน", or "20 ต.ค. 69")
     */
    fun formatFriendlyDate(dateString: String): String {
        val today = todayDateString()
        if (dateString == today) return "วันนี้"

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
        if (dateString == yesterday) return "เมื่อวาน"

        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateString)
            if (date != null) formatThaiDate(date.time) else dateString
        } catch (_: Exception) {
            dateString
        }
    }

    private fun getThaiDayOfWeek(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "อาทิตย์"
            Calendar.MONDAY -> "จันทร์"
            Calendar.TUESDAY -> "อังคาร"
            Calendar.WEDNESDAY -> "พุธ"
            Calendar.THURSDAY -> "พฤหัสบดี"
            Calendar.FRIDAY -> "ศุกร์"
            Calendar.SATURDAY -> "เสาร์"
            else -> ""
        }
    }

    private fun getThaiMonthShort(month: Int): String {
        return when (month) {
            0 -> "ม.ค."
            1 -> "ก.พ."
            2 -> "มี.ค."
            3 -> "เม.ย."
            4 -> "พ.ค."
            5 -> "มิ.ย."
            6 -> "ก.ค."
            7 -> "ส.ค."
            8 -> "ก.ย."
            9 -> "ต.ค."
            10 -> "พ.ย."
            11 -> "ธ.ค."
            else -> ""
        }
    }

    private fun getThaiMonthFull(month: Int): String {
        return when (month) {
            0 -> "มกราคม"
            1 -> "กุมภาพันธ์"
            2 -> "มีนาคม"
            3 -> "เมษายน"
            4 -> "พฤษภาคม"
            5 -> "มิถุนายน"
            6 -> "กรกฎาคม"
            7 -> "สิงหาคม"
            8 -> "กันยายน"
            9 -> "ตุลาคม"
            10 -> "พฤศจิกายน"
            11 -> "ธันวาคม"
            else -> ""
        }
    }
}
