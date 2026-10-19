package com.easystock.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ThaiFormatUtilsTest {

    @Test
    fun testFormatBaht() {
        assertEquals("฿120.00", ThaiFormatUtils.formatBaht(BigDecimal("120")))
        assertEquals("฿120.50", ThaiFormatUtils.formatBaht(BigDecimal("120.5")))
        assertEquals("฿1,250.75", ThaiFormatUtils.formatBaht(BigDecimal("1250.75")))
        assertEquals("฿0.00", ThaiFormatUtils.formatBaht(BigDecimal.ZERO))
        assertEquals("฿0.00", ThaiFormatUtils.formatBaht(null))
    }

    @Test
    fun testFormatQuantity() {
        assertEquals("12", ThaiFormatUtils.formatQuantity(BigDecimal("12")))
        assertEquals("12.5", ThaiFormatUtils.formatQuantity(BigDecimal("12.50")))
        assertEquals("0.75", ThaiFormatUtils.formatQuantity(BigDecimal("0.75")))
        assertEquals("0", ThaiFormatUtils.formatQuantity(BigDecimal.ZERO))
    }

    @Test
    fun testFormatQuantityWithUnit() {
        assertEquals("12.5 กก.", ThaiFormatUtils.formatQuantityWithUnit(BigDecimal("12.5"), "กก."))
        assertEquals("3 หวี", ThaiFormatUtils.formatQuantityWithUnit(BigDecimal("3"), "หวี"))
    }

    @Test
    fun testThaiDateFormatting() {
        // Test date string format
        val todayStr = ThaiFormatUtils.todayDateString()
        assertTrue(todayStr.matches(Regex("""\d{4}-\d{2}-\d{2}""")))

        // Test friendly date
        assertEquals("วันนี้", ThaiFormatUtils.formatFriendlyDate(todayStr))
    }
}
