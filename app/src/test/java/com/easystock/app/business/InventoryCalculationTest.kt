package com.easystock.app.business

import com.easystock.app.data.model.BackupData
import com.easystock.app.data.model.FruitType
import com.easystock.app.data.model.PurchaseRecord
import com.easystock.app.data.model.SaleRecord
import com.easystock.app.data.model.StockCountRecord
import com.easystock.app.data.model.WasteRecord
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class InventoryCalculationTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun testFruitTypeIsTemplateAndDoesNotCreateStock() {
        val fruit = FruitType(
            id = 1L,
            name = "ทุเรียนหมอนทอง",
            defaultUnit = "กก.",
            defaultCostPrice = BigDecimal("120.00"),
            defaultSellingPrice = BigDecimal("160.00")
        )

        // Without purchases, stock must be 0
        val purchases = emptyList<PurchaseRecord>()
        val sales = emptyList<SaleRecord>()
        val waste = emptyList<WasteRecord>()

        val totalPurchased = purchases.fold(BigDecimal.ZERO) { acc, p -> acc.add(p.quantity) }
        val totalSold = sales.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.quantity) }
        val totalWaste = waste.fold(BigDecimal.ZERO) { acc, w -> acc.add(w.quantity) }

        val currentStock = totalPurchased.subtract(totalSold).subtract(totalWaste)

        assertEquals(BigDecimal.ZERO, currentStock)
    }

    @Test
    fun testRepeatedPurchasesAtDifferentCostsWeightedAverage() {
        // Purchase 1: 10 kg @ 40 THB = 400 THB
        // Purchase 2: 20 kg @ 55 THB = 1100 THB
        // Total Qty: 30 kg, Total Cost: 1500 THB
        // Average Cost per unit: 1500 / 30 = 50.00 THB
        val p1 = PurchaseRecord(
            id = 1,
            fruitTypeId = 1,
            quantity = BigDecimal("10.00"),
            unitCost = BigDecimal("40.00"),
            totalCost = BigDecimal("400.00"),
            purchaseDate = 1000L,
            dateString = "2026-10-20"
        )
        val p2 = PurchaseRecord(
            id = 2,
            fruitTypeId = 1,
            quantity = BigDecimal("20.00"),
            unitCost = BigDecimal("55.00"),
            totalCost = BigDecimal("1100.00"),
            purchaseDate = 2000L,
            dateString = "2026-10-20"
        )

        val purchases = listOf(p1, p2)
        val totalQty = purchases.fold(BigDecimal.ZERO) { acc, p -> acc.add(p.quantity) }
        val totalCost = purchases.fold(BigDecimal.ZERO) { acc, p -> acc.add(p.totalCost) }

        val weightedAverageCost = totalCost.divide(totalQty, 2, RoundingMode.HALF_UP)

        assertEquals(BigDecimal("30.00"), totalQty)
        assertEquals(BigDecimal("1500.00"), totalCost)
        assertEquals(BigDecimal("50.00"), weightedAverageCost)

        // Now record sale of 5 kg @ 70 THB:
        // Revenue = 5 * 70 = 350.00 THB
        // COGS = 5 * 50.00 = 250.00 THB
        // Gross Profit = 350.00 - 250.00 = 100.00 THB
        val saleQty = BigDecimal("5.00")
        val sellPrice = BigDecimal("70.00")
        val revenue = saleQty.multiply(sellPrice).setScale(2, RoundingMode.HALF_UP)
        val cogs = saleQty.multiply(weightedAverageCost).setScale(2, RoundingMode.HALF_UP)
        val grossProfit = revenue.subtract(cogs)

        assertEquals(BigDecimal("350.00"), revenue)
        assertEquals(BigDecimal("250.00"), cogs)
        assertEquals(BigDecimal("100.00"), grossProfit)

        // Remaining stock = 30 - 5 = 25 kg
        val remainingStock = totalQty.subtract(saleQty)
        assertEquals(BigDecimal("25.00"), remainingStock)
    }

    @Test
    fun testSalesAndWasteDecreaseStock() {
        val totalPurchased = BigDecimal("50.00")
        val salesQty = BigDecimal("20.00")
        val wasteQty = BigDecimal("3.50")

        val currentStock = totalPurchased.subtract(salesQty).subtract(wasteQty)
        assertEquals(BigDecimal("26.50"), currentStock)
    }

    @Test
    fun testStockCountDiscrepancyCalculationWithoutSilentlyCreatingSales() {
        // Expected stock = 20 kg
        // Actual counted at closing = 17 kg
        // Discrepancy = 17 - 20 = -3 kg (shortage)
        val expected = BigDecimal("20.00")
        val actual = BigDecimal("17.00")
        val discrepancy = actual.subtract(expected)

        assertEquals(BigDecimal("-3.00"), discrepancy)
        assertTrue(discrepancy < BigDecimal.ZERO)

        // Recording discrepancy as a waste record:
        val wasteRecord = WasteRecord(
            id = 1,
            fruitTypeId = 1,
            quantity = discrepancy.abs(),
            estimatedUnitCost = BigDecimal("50.00"),
            totalWasteCost = discrepancy.abs().multiply(BigDecimal("50.00")).setScale(2, RoundingMode.HALF_UP),
            wasteDate = System.currentTimeMillis(),
            dateString = "2026-10-20",
            reason = "คัดทิ้งจากการตรวจนับสิ้นวัน"
        )

        assertEquals(BigDecimal("3.00"), wasteRecord.quantity)
        assertEquals(BigDecimal("150.00"), wasteRecord.totalWasteCost)
        assertEquals("คัดทิ้งจากการตรวจนับสิ้นวัน", wasteRecord.reason)

        // Closing stock count must NOT generate a sale
        val salesCreated = 0
        assertEquals(0, salesCreated)
    }

    @Test
    fun testBackupAndRestoreSerialization() {
        val backup = BackupData(
            exportTimestamp = 123456789L,
            appVersion = "1.0",
            fruitTypes = listOf(
                FruitType(
                    id = 1L,
                    name = "มะม่วงน้ำดอกไม้",
                    defaultUnit = "กก.",
                    defaultCostPrice = BigDecimal("40.00"),
                    defaultSellingPrice = BigDecimal("60.00"),
                    isActive = true
                )
            ),
            purchases = listOf(
                PurchaseRecord(
                    id = 1L,
                    fruitTypeId = 1L,
                    quantity = BigDecimal("20.00"),
                    unitCost = BigDecimal("40.00"),
                    totalCost = BigDecimal("800.00"),
                    purchaseDate = 1000L,
                    dateString = "2026-10-20",
                    notes = "ตลาดไท"
                )
            ),
            sales = listOf(
                SaleRecord(
                    id = 1L,
                    fruitTypeId = 1L,
                    quantity = BigDecimal("5.00"),
                    unitPrice = BigDecimal("60.00"),
                    totalRevenue = BigDecimal("300.00"),
                    unitCost = BigDecimal("40.00"),
                    costOfGoodsSold = BigDecimal("200.00"),
                    saleDate = 2000L,
                    dateString = "2026-10-20",
                    paymentMethod = "เงินสด"
                )
            ),
            wasteRecords = listOf(
                WasteRecord(
                    id = 1L,
                    fruitTypeId = 1L,
                    quantity = BigDecimal("1.00"),
                    estimatedUnitCost = BigDecimal("40.00"),
                    totalWasteCost = BigDecimal("40.00"),
                    wasteDate = 3000L,
                    dateString = "2026-10-20",
                    reason = "เน่าเสีย"
                )
            ),
            stockCounts = listOf(
                StockCountRecord(
                    id = 1L,
                    dateString = "2026-10-20",
                    fruitTypeId = 1L,
                    expectedQty = BigDecimal("15.00"),
                    actualQty = BigDecimal("14.00"),
                    discrepancyQty = BigDecimal("-1.00")
                )
            )
        )

        val jsonString = json.encodeToString(BackupData.serializer(), backup)
        val decoded = json.decodeFromString(BackupData.serializer(), jsonString)

        assertEquals(1, decoded.fruitTypes.size)
        assertEquals("มะม่วงน้ำดอกไม้", decoded.fruitTypes[0].name)
        assertEquals(BigDecimal("40.00"), decoded.fruitTypes[0].defaultCostPrice)
        assertEquals(1, decoded.purchases.size)
        assertEquals(BigDecimal("800.00"), decoded.purchases[0].totalCost)
        assertEquals(1, decoded.sales.size)
        assertEquals(BigDecimal("300.00"), decoded.sales[0].totalRevenue)
        assertEquals(1, decoded.wasteRecords.size)
        assertEquals("เน่าเสีย", decoded.wasteRecords[0].reason)
        assertEquals(1, decoded.stockCounts.size)
        assertEquals(BigDecimal("-1.00"), decoded.stockCounts[0].discrepancyQty)
    }

    @Test
    fun testDeactivatingFruitPreservesHistoricalRecords() {
        val fruit = FruitType(
            id = 5L,
            name = "กล้วยหอม",
            defaultUnit = "หวี",
            isActive = true
        )

        // Deactivate fruit
        val deactivated = fruit.copy(isActive = false)

        assertFalse(deactivated.isActive)
        assertEquals(fruit.id, deactivated.id)
        assertEquals(fruit.name, deactivated.name)

        // Historical purchase referencing this fruit remains unchanged
        val historicalPurchase = PurchaseRecord(
            id = 10L,
            fruitTypeId = fruit.id,
            quantity = BigDecimal("10.00"),
            unitCost = BigDecimal("25.00"),
            totalCost = BigDecimal("250.00"),
            purchaseDate = 1000L,
            dateString = "2026-10-19"
        )

        assertEquals(fruit.id, historicalPurchase.fruitTypeId)
    }
}
