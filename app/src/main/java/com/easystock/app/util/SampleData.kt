package com.easystock.app.util

import com.easystock.app.data.model.FruitType
import java.math.BigDecimal

object SampleData {
    val defaultFruitTypes = listOf(
        FruitType(
            name = "ทุเรียนหมอนทอง",
            defaultUnit = "กก.",
            defaultCostPrice = BigDecimal("120.00"),
            defaultSellingPrice = BigDecimal("160.00")
        ),
        FruitType(
            name = "มะม่วงน้ำดอกไม้",
            defaultUnit = "กก.",
            defaultCostPrice = BigDecimal("40.00"),
            defaultSellingPrice = BigDecimal("60.00")
        ),
        FruitType(
            name = "กล้วยหอมทอง",
            defaultUnit = "หวี",
            defaultCostPrice = BigDecimal("25.00"),
            defaultSellingPrice = BigDecimal("40.00")
        ),
        FruitType(
            name = "แตงโม",
            defaultUnit = "ผล",
            defaultCostPrice = BigDecimal("35.00"),
            defaultSellingPrice = BigDecimal("50.00")
        ),
        FruitType(
            name = "ส้มสายน้ำผึ้ง",
            defaultUnit = "กก.",
            defaultCostPrice = BigDecimal("45.00"),
            defaultSellingPrice = BigDecimal("65.00")
        ),
        FruitType(
            name = "มังคุด",
            defaultUnit = "กก.",
            defaultCostPrice = BigDecimal("35.00"),
            defaultSellingPrice = BigDecimal("55.00")
        ),
        FruitType(
            name = "ฝรั่งกิมจู",
            defaultUnit = "กก.",
            defaultCostPrice = BigDecimal("25.00"),
            defaultSellingPrice = BigDecimal("40.00")
        )
    )

    val commonUnits = listOf(
        "กก.",
        "ผล",
        "ลูก",
        "หวี",
        "กล่อง",
        "แพ็ค",
        "ถุง",
        "เข่ง"
    )

    val commonWasteReasons = listOf(
        "เน่าเสีย",
        "ช้ำ / เสียหาย",
        "สุกเกินไป (ขายไม่ได้)",
        "แห้งเหี่ยว / น้ำหนักลด",
        "อื่นๆ"
    )
}
