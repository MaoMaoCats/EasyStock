package com.easystock.app.data.repository

import com.easystock.app.data.local.EasyStockDatabase
import com.easystock.app.data.model.BackupData
import com.easystock.app.data.model.DailyFruitSummary
import com.easystock.app.data.model.DailySummary
import com.easystock.app.data.model.FruitStockSummary
import com.easystock.app.data.model.FruitType
import com.easystock.app.data.model.PurchaseRecord
import com.easystock.app.data.model.SaleRecord
import com.easystock.app.data.model.StockCountRecord
import com.easystock.app.data.model.WasteRecord
import com.easystock.app.util.SampleData
import com.easystock.app.util.ThaiFormatUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.math.RoundingMode

class EasyStockRepository(private val database: EasyStockDatabase) {

    private val fruitTypeDao = database.fruitTypeDao()
    private val purchaseDao = database.purchaseDao()
    private val saleDao = database.saleDao()
    private val wasteDao = database.wasteDao()
    private val stockCountDao = database.stockCountDao()

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // -------------------------------------------------------------------------
    // Fruit Types
    // -------------------------------------------------------------------------

    fun getAllFruitTypes(): Flow<List<FruitType>> = fruitTypeDao.getAll()

    fun getActiveFruitTypes(): Flow<List<FruitType>> = fruitTypeDao.getActive()

    suspend fun getFruitTypeById(id: Long): FruitType? = fruitTypeDao.getById(id)

    suspend fun addFruitType(fruitType: FruitType): Long {
        return fruitTypeDao.insert(fruitType)
    }

    suspend fun updateFruitType(fruitType: FruitType) {
        fruitTypeDao.update(fruitType)
    }

    suspend fun deactivateFruitType(id: Long) {
        fruitTypeDao.deactivate(id)
    }

    suspend fun activateFruitType(id: Long) {
        fruitTypeDao.activate(id)
    }

    suspend fun deleteFruitType(fruitType: FruitType): Result<Unit> {
        // Enforce rule: Never delete historical transactions when a fruit type is deactivated.
        // If fruit type has transactions, we deactivate it instead of deleting.
        val hasPurchases = purchaseDao.getListByFruitTypeId(fruitType.id).isNotEmpty()
        val hasSales = saleDao.getListByFruitTypeId(fruitType.id).isNotEmpty()
        val hasWaste = wasteDao.getListByFruitTypeId(fruitType.id).isNotEmpty()

        return if (hasPurchases || hasSales || hasWaste) {
            fruitTypeDao.deactivate(fruitType.id)
            Result.success(Unit)
        } else {
            fruitTypeDao.delete(fruitType)
            Result.success(Unit)
        }
    }

    suspend fun countFruitTypes(): Int = fruitTypeDao.count()

    suspend fun seedSampleFruitTypes() {
        if (fruitTypeDao.count() == 0) {
            fruitTypeDao.insertAll(SampleData.defaultFruitTypes)
        }
    }

    // -------------------------------------------------------------------------
    // Purchases
    // -------------------------------------------------------------------------

    fun getAllPurchases(): Flow<List<PurchaseRecord>> = purchaseDao.getAll()

    fun getPurchasesByDate(dateString: String): Flow<List<PurchaseRecord>> =
        purchaseDao.getByDate(dateString)

    suspend fun addPurchase(
        fruitTypeId: Long,
        quantity: BigDecimal,
        unitCost: BigDecimal,
        purchaseDate: Long = System.currentTimeMillis(),
        notes: String = ""
    ): Result<Long> {
        if (quantity <= BigDecimal.ZERO) {
            return Result.failure(IllegalArgumentException("จำนวนที่ซื้อต้องมากกว่า 0"))
        }
        if (unitCost < BigDecimal.ZERO) {
            return Result.failure(IllegalArgumentException("ราคาต้นทุนต้องไม่ติดลบ"))
        }

        val totalCost = quantity.multiply(unitCost).setScale(2, RoundingMode.HALF_UP)
        val dateString = ThaiFormatUtils.toDateString(purchaseDate)
        val record = PurchaseRecord(
            fruitTypeId = fruitTypeId,
            quantity = quantity,
            unitCost = unitCost.setScale(2, RoundingMode.HALF_UP),
            totalCost = totalCost,
            purchaseDate = purchaseDate,
            dateString = dateString,
            notes = notes
        )
        val id = purchaseDao.insert(record)
        return Result.success(id)
    }

    suspend fun updatePurchase(purchase: PurchaseRecord): Result<Unit> {
        if (purchase.quantity <= BigDecimal.ZERO) {
            return Result.failure(IllegalArgumentException("จำนวนที่ซื้อต้องมากกว่า 0"))
        }
        if (purchase.unitCost < BigDecimal.ZERO) {
            return Result.failure(IllegalArgumentException("ราคาต้นทุนต้องไม่ติดลบ"))
        }
        val totalCost = purchase.quantity.multiply(purchase.unitCost).setScale(2, RoundingMode.HALF_UP)
        purchaseDao.update(purchase.copy(totalCost = totalCost))
        return Result.success(Unit)
    }

    suspend fun deletePurchase(id: Long) {
        purchaseDao.deleteById(id)
    }

    // -------------------------------------------------------------------------
    // Sales
    // -------------------------------------------------------------------------

    fun getAllSales(): Flow<List<SaleRecord>> = saleDao.getAll()

    fun getSalesByDate(dateString: String): Flow<List<SaleRecord>> =
        saleDao.getByDate(dateString)

    suspend fun recordSale(
        fruitTypeId: Long,
        quantity: BigDecimal,
        unitPrice: BigDecimal,
        saleDate: Long = System.currentTimeMillis(),
        paymentMethod: String = "เงินสด",
        notes: String = "",
        allowNegativeStock: Boolean = false
    ): Result<Long> {
        if (quantity <= BigDecimal.ZERO) {
            return Result.failure(IllegalArgumentException("จำนวนที่ขายต้องมากกว่า 0"))
        }
        if (unitPrice < BigDecimal.ZERO) {
            return Result.failure(IllegalArgumentException("ราคาขายต้องไม่ติดลบ"))
        }

        val fruit = fruitTypeDao.getById(fruitTypeId)
            ?: return Result.failure(IllegalArgumentException("ไม่พบชนิดผลไม้ที่ระบุ"))

        // Check stock balance
        val stockSummary = calculateFruitStock(fruitTypeId)
        if (!allowNegativeStock && quantity > stockSummary.currentStock) {
            val unit = fruit.defaultUnit
            return Result.failure(
                IllegalStateException("สต็อกคงเหลือไม่เพียงพอ (มีอยู่ ${ThaiFormatUtils.formatQuantity(stockSummary.currentStock)} $unit)")
            )
        }

        // Calculate Cost of Goods Sold (COGS) based on Weighted Average Cost
        val avgCost = if (stockSummary.averageCost > BigDecimal.ZERO) {
            stockSummary.averageCost
        } else {
            fruit.defaultCostPrice ?: BigDecimal.ZERO
        }

        val totalRevenue = quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP)
        val cogs = quantity.multiply(avgCost).setScale(2, RoundingMode.HALF_UP)
        val dateString = ThaiFormatUtils.toDateString(saleDate)

        val sale = SaleRecord(
            fruitTypeId = fruitTypeId,
            quantity = quantity,
            unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP),
            totalRevenue = totalRevenue,
            unitCost = avgCost.setScale(2, RoundingMode.HALF_UP),
            costOfGoodsSold = cogs,
            saleDate = saleDate,
            dateString = dateString,
            paymentMethod = paymentMethod,
            notes = notes
        )

        val id = saleDao.insert(sale)
        return Result.success(id)
    }

    suspend fun deleteSale(id: Long) {
        saleDao.deleteById(id)
    }

    // -------------------------------------------------------------------------
    // Waste
    // -------------------------------------------------------------------------

    fun getAllWaste(): Flow<List<WasteRecord>> = wasteDao.getAll()

    fun getWasteByDate(dateString: String): Flow<List<WasteRecord>> =
        wasteDao.getByDate(dateString)

    suspend fun recordWaste(
        fruitTypeId: Long,
        quantity: BigDecimal,
        reason: String,
        wasteDate: Long = System.currentTimeMillis(),
        notes: String = "",
        allowNegativeStock: Boolean = false
    ): Result<Long> {
        if (quantity <= BigDecimal.ZERO) {
            return Result.failure(IllegalArgumentException("จำนวนของเสียต้องมากกว่า 0"))
        }

        val fruit = fruitTypeDao.getById(fruitTypeId)
            ?: return Result.failure(IllegalArgumentException("ไม่พบชนิดผลไม้ที่ระบุ"))

        val stockSummary = calculateFruitStock(fruitTypeId)
        if (!allowNegativeStock && quantity > stockSummary.currentStock) {
            val unit = fruit.defaultUnit
            return Result.failure(
                IllegalStateException("สต็อกคงเหลือไม่เพียงพอ (มีอยู่ ${ThaiFormatUtils.formatQuantity(stockSummary.currentStock)} $unit)")
            )
        }

        val avgCost = if (stockSummary.averageCost > BigDecimal.ZERO) {
            stockSummary.averageCost
        } else {
            fruit.defaultCostPrice ?: BigDecimal.ZERO
        }

        val totalCost = quantity.multiply(avgCost).setScale(2, RoundingMode.HALF_UP)
        val dateString = ThaiFormatUtils.toDateString(wasteDate)

        val record = WasteRecord(
            fruitTypeId = fruitTypeId,
            quantity = quantity,
            estimatedUnitCost = avgCost.setScale(2, RoundingMode.HALF_UP),
            totalWasteCost = totalCost,
            wasteDate = wasteDate,
            dateString = dateString,
            reason = reason,
            notes = notes
        )

        val id = wasteDao.insert(record)
        return Result.success(id)
    }

    suspend fun deleteWaste(id: Long) {
        wasteDao.deleteById(id)
    }

    // -------------------------------------------------------------------------
    // Stock Counts
    // -------------------------------------------------------------------------

    fun getStockCountsByDate(dateString: String): Flow<List<StockCountRecord>> =
        stockCountDao.getByDate(dateString)

    suspend fun recordStockCount(
        fruitTypeId: Long,
        actualQty: BigDecimal,
        dateString: String = ThaiFormatUtils.todayDateString(),
        recordWasteForShortage: Boolean = false,
        wasteReason: String = "คัดทิ้งจากการนับสต็อก / สูญหาย",
        notes: String = ""
    ): Result<Long> {
        if (actualQty < BigDecimal.ZERO) {
            return Result.failure(IllegalArgumentException("จำนวนสต็อกจริงต้องไม่ติดลบ"))
        }

        val stockSummary = calculateFruitStock(fruitTypeId)
        val expectedQty = stockSummary.currentStock
        val discrepancy = actualQty.subtract(expectedQty)

        val stockCount = StockCountRecord(
            dateString = dateString,
            fruitTypeId = fruitTypeId,
            expectedQty = expectedQty,
            actualQty = actualQty,
            discrepancyQty = discrepancy,
            countDate = System.currentTimeMillis(),
            notes = notes
        )

        val countId = stockCountDao.insert(stockCount)

        // If there is a shortage (actual < expected) and user requested recording waste
        if (recordWasteForShortage && discrepancy < BigDecimal.ZERO) {
            val shortageQty = discrepancy.abs()
            recordWaste(
                fruitTypeId = fruitTypeId,
                quantity = shortageQty,
                reason = wasteReason,
                wasteDate = System.currentTimeMillis(),
                notes = "บันทึกอัตโนมัติจากการนับสต็อก (คาดหวัง $expectedQty, นับได้ $actualQty)",
                allowNegativeStock = true
            )
        }

        return Result.success(countId)
    }

    // -------------------------------------------------------------------------
    // Stock & Inventory Calculations
    // -------------------------------------------------------------------------

    suspend fun calculateFruitStock(fruitTypeId: Long): FruitStockSummary {
        val fruit = fruitTypeDao.getById(fruitTypeId)
            ?: throw IllegalArgumentException("FruitType not found")

        val purchases = purchaseDao.getListByFruitTypeId(fruitTypeId)
        val sales = saleDao.getListByFruitTypeId(fruitTypeId)
        val wastes = wasteDao.getListByFruitTypeId(fruitTypeId)

        var totalPurchasedQty = BigDecimal.ZERO
        var totalPurchasedCost = BigDecimal.ZERO
        for (p in purchases) {
            totalPurchasedQty = totalPurchasedQty.add(p.quantity)
            totalPurchasedCost = totalPurchasedCost.add(p.totalCost)
        }

        var totalSoldQty = BigDecimal.ZERO
        for (s in sales) {
            totalSoldQty = totalSoldQty.add(s.quantity)
        }

        var totalWasteQty = BigDecimal.ZERO
        for (w in wastes) {
            totalWasteQty = totalWasteQty.add(w.quantity)
        }

        val currentStock = totalPurchasedQty.subtract(totalSoldQty).subtract(totalWasteQty)

        // Weighted average cost calculation
        val averageCost = if (totalPurchasedQty > BigDecimal.ZERO) {
            totalPurchasedCost.divide(totalPurchasedQty, 2, RoundingMode.HALF_UP)
        } else {
            fruit.defaultCostPrice ?: BigDecimal.ZERO
        }

        val estimatedStockValue = if (currentStock > BigDecimal.ZERO) {
            currentStock.multiply(averageCost).setScale(2, RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }

        return FruitStockSummary(
            fruitType = fruit,
            totalPurchased = totalPurchasedQty,
            totalSold = totalSoldQty,
            totalWaste = totalWasteQty,
            currentStock = currentStock,
            averageCost = averageCost,
            estimatedStockValue = estimatedStockValue
        )
    }

    /**
     * Reactive stream of current stock for all fruits
     */
    val fruitStockSummaries: Flow<List<FruitStockSummary>> = combine(
        fruitTypeDao.getAll(),
        purchaseDao.getAll(),
        saleDao.getAll(),
        wasteDao.getAll()
    ) { fruits, purchases, sales, wastes ->
        fruits.map { fruit ->
            val pList = purchases.filter { it.fruitTypeId == fruit.id }
            val sList = sales.filter { it.fruitTypeId == fruit.id }
            val wList = wastes.filter { it.fruitTypeId == fruit.id }

            var totalPurchasedQty = BigDecimal.ZERO
            var totalPurchasedCost = BigDecimal.ZERO
            for (p in pList) {
                totalPurchasedQty = totalPurchasedQty.add(p.quantity)
                totalPurchasedCost = totalPurchasedCost.add(p.totalCost)
            }

            var totalSoldQty = BigDecimal.ZERO
            for (s in sList) {
                totalSoldQty = totalSoldQty.add(s.quantity)
            }

            var totalWasteQty = BigDecimal.ZERO
            for (w in wList) {
                totalWasteQty = totalWasteQty.add(w.quantity)
            }

            val currentStock = totalPurchasedQty.subtract(totalSoldQty).subtract(totalWasteQty)

            val averageCost = if (totalPurchasedQty > BigDecimal.ZERO) {
                totalPurchasedCost.divide(totalPurchasedQty, 2, RoundingMode.HALF_UP)
            } else {
                fruit.defaultCostPrice ?: BigDecimal.ZERO
            }

            val estimatedStockValue = if (currentStock > BigDecimal.ZERO) {
                currentStock.multiply(averageCost).setScale(2, RoundingMode.HALF_UP)
            } else {
                BigDecimal.ZERO
            }

            FruitStockSummary(
                fruitType = fruit,
                totalPurchased = totalPurchasedQty,
                totalSold = totalSoldQty,
                totalWaste = totalWasteQty,
                currentStock = currentStock,
                averageCost = averageCost,
                estimatedStockValue = estimatedStockValue
            )
        }
    }

    // -------------------------------------------------------------------------
    // Daily Summary Calculation
    // -------------------------------------------------------------------------

    fun getDailySummary(dateString: String): Flow<DailySummary> = combine(
        fruitTypeDao.getAll(),
        saleDao.getByDate(dateString),
        wasteDao.getByDate(dateString),
        purchaseDao.getByDate(dateString),
        fruitStockSummaries
    ) { fruits, daySales, dayWastes, dayPurchases, stockSummaries ->
        val stockMap = stockSummaries.associateBy { it.fruitType.id }

        var totalRevenue = BigDecimal.ZERO
        var totalCogs = BigDecimal.ZERO
        var totalWasteQty = BigDecimal.ZERO
        var totalWasteCost = BigDecimal.ZERO
        var totalPurchasesCost = BigDecimal.ZERO
        var totalPurchasesQty = BigDecimal.ZERO

        val fruitSummaries = fruits.map { fruit ->
            val fSales = daySales.filter { it.fruitTypeId == fruit.id }
            val fWastes = dayWastes.filter { it.fruitTypeId == fruit.id }
            val fPurchases = dayPurchases.filter { it.fruitTypeId == fruit.id }

            var fruitQtySold = BigDecimal.ZERO
            var fruitRevenue = BigDecimal.ZERO
            var fruitCogs = BigDecimal.ZERO
            for (s in fSales) {
                fruitQtySold = fruitQtySold.add(s.quantity)
                fruitRevenue = fruitRevenue.add(s.totalRevenue)
                fruitCogs = fruitCogs.add(s.costOfGoodsSold)
            }

            var fruitWasteQty = BigDecimal.ZERO
            var fruitWasteCost = BigDecimal.ZERO
            for (w in fWastes) {
                fruitWasteQty = fruitWasteQty.add(w.quantity)
                fruitWasteCost = fruitWasteCost.add(w.totalWasteCost)
            }

            var fruitPurchasedQty = BigDecimal.ZERO
            var fruitPurchasedCost = BigDecimal.ZERO
            for (p in fPurchases) {
                fruitPurchasedQty = fruitPurchasedQty.add(p.quantity)
                fruitPurchasedCost = fruitPurchasedCost.add(p.totalCost)
            }

            val fruitGrossProfit = fruitRevenue.subtract(fruitCogs)

            totalRevenue = totalRevenue.add(fruitRevenue)
            totalCogs = totalCogs.add(fruitCogs)
            totalWasteQty = totalWasteQty.add(fruitWasteQty)
            totalWasteCost = totalWasteCost.add(fruitWasteCost)
            totalPurchasesCost = totalPurchasesCost.add(fruitPurchasedCost)
            totalPurchasesQty = totalPurchasesQty.add(fruitPurchasedQty)

            DailyFruitSummary(
                fruitType = fruit,
                quantitySold = fruitQtySold,
                revenue = fruitRevenue,
                costOfGoodsSold = fruitCogs,
                grossProfit = fruitGrossProfit,
                wasteQty = fruitWasteQty,
                wasteCost = fruitWasteCost,
                purchasedQty = fruitPurchasedQty,
                purchasedCost = fruitPurchasedCost,
                currentStock = stockMap[fruit.id]?.currentStock ?: BigDecimal.ZERO
            )
        }.filter {
            // Keep fruits that had activity today or have current stock
            it.quantitySold > BigDecimal.ZERO ||
                it.wasteQty > BigDecimal.ZERO ||
                it.purchasedQty > BigDecimal.ZERO ||
                it.currentStock > BigDecimal.ZERO
        }

        val grossProfit = totalRevenue.subtract(totalCogs)
        val profitMarginPercent = if (totalRevenue > BigDecimal.ZERO) {
            grossProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).toDouble() * 100.0
        } else {
            0.0
        }

        var remainingTotalQty = BigDecimal.ZERO
        var remainingTotalVal = BigDecimal.ZERO
        for (item in stockSummaries) {
            if (item.currentStock > BigDecimal.ZERO) {
                remainingTotalQty = remainingTotalQty.add(item.currentStock)
                remainingTotalVal = remainingTotalVal.add(item.estimatedStockValue)
            }
        }

        DailySummary(
            dateString = dateString,
            totalSalesRevenue = totalRevenue,
            totalCostOfGoodsSold = totalCogs,
            grossProfit = grossProfit,
            profitMarginPercent = profitMarginPercent,
            totalRecordedWasteQty = totalWasteQty,
            totalRecordedWasteCost = totalWasteCost,
            totalPurchasesCost = totalPurchasesCost,
            totalPurchasesQty = totalPurchasesQty,
            remainingStockTotalQty = remainingTotalQty,
            remainingStockTotalValue = remainingTotalVal,
            fruitSummaries = fruitSummaries
        )
    }

    // -------------------------------------------------------------------------
    // Backup & Restore
    // -------------------------------------------------------------------------

    suspend fun exportBackupJson(): String {
        val backup = BackupData(
            exportTimestamp = System.currentTimeMillis(),
            appVersion = "1.0",
            fruitTypes = fruitTypeDao.getAllList(),
            purchases = purchaseDao.getAllList(),
            sales = saleDao.getAllList(),
            wasteRecords = wasteDao.getAllList(),
            stockCounts = stockCountDao.getAllList()
        )
        return json.encodeToString(BackupData.serializer(), backup)
    }

    suspend fun restoreBackupJson(jsonString: String): Result<Unit> {
        return try {
            val backup = json.decodeFromString(BackupData.serializer(), jsonString)

            // Clear current data and insert restored data
            stockCountDao.deleteAll()
            wasteDao.deleteAll()
            saleDao.deleteAll()
            purchaseDao.deleteAll()
            fruitTypeDao.deleteAll()

            fruitTypeDao.insertAll(backup.fruitTypes)
            purchaseDao.insertAll(backup.purchases)
            saleDao.insertAll(backup.sales)
            wasteDao.insertAll(backup.wasteRecords)
            stockCountDao.insertAll(backup.stockCounts)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
