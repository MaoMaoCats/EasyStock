package com.easystock.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.easystock.app.util.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
@Entity(
    tableName = "stock_count_records",
    foreignKeys = [
        ForeignKey(
            entity = FruitType::class,
            parentColumns = ["id"],
            childColumns = ["fruitTypeId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("fruitTypeId"),
        Index("dateString")
    ]
)
data class StockCountRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // YYYY-MM-DD
    val fruitTypeId: Long,
    @Serializable(with = BigDecimalSerializer::class)
    val expectedQty: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val actualQty: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val discrepancyQty: BigDecimal, // actualQty - expectedQty
    val countDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)
