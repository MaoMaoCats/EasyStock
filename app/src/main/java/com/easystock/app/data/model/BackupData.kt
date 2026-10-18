package com.easystock.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val exportTimestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val fruitTypes: List<FruitType> = emptyList(),
    val purchases: List<PurchaseRecord> = emptyList(),
    val sales: List<SaleRecord> = emptyList(),
    val wasteRecords: List<WasteRecord> = emptyList(),
    val stockCounts: List<StockCountRecord> = emptyList()
)
