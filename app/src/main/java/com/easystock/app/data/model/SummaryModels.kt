package com.easystock.app.data.model

import java.math.BigDecimal

data class FruitStockSummary(
    val fruitType: FruitType,
    val totalPurchased: BigDecimal = BigDecimal.ZERO,
    val totalSold: BigDecimal = BigDecimal.ZERO,
    val totalWaste: BigDecimal = BigDecimal.ZERO,
    val currentStock: BigDecimal = BigDecimal.ZERO,
    val averageCost: BigDecimal = BigDecimal.ZERO,
    val estimatedStockValue: BigDecimal = BigDecimal.ZERO
)

data class DailyFruitSummary(
    val fruitType: FruitType,
    val quantitySold: BigDecimal = BigDecimal.ZERO,
    val revenue: BigDecimal = BigDecimal.ZERO,
    val costOfGoodsSold: BigDecimal = BigDecimal.ZERO,
    val grossProfit: BigDecimal = BigDecimal.ZERO,
    val wasteQty: BigDecimal = BigDecimal.ZERO,
    val wasteCost: BigDecimal = BigDecimal.ZERO,
    val purchasedQty: BigDecimal = BigDecimal.ZERO,
    val purchasedCost: BigDecimal = BigDecimal.ZERO,
    val currentStock: BigDecimal = BigDecimal.ZERO
)

data class DailySummary(
    val dateString: String,
    val totalSalesRevenue: BigDecimal = BigDecimal.ZERO,
    val totalCostOfGoodsSold: BigDecimal = BigDecimal.ZERO,
    val grossProfit: BigDecimal = BigDecimal.ZERO,
    val profitMarginPercent: Double = 0.0,
    val totalRecordedWasteQty: BigDecimal = BigDecimal.ZERO,
    val totalRecordedWasteCost: BigDecimal = BigDecimal.ZERO,
    val totalPurchasesCost: BigDecimal = BigDecimal.ZERO,
    val totalPurchasesQty: BigDecimal = BigDecimal.ZERO,
    val remainingStockTotalQty: BigDecimal = BigDecimal.ZERO,
    val remainingStockTotalValue: BigDecimal = BigDecimal.ZERO,
    val fruitSummaries: List<DailyFruitSummary> = emptyList()
)
