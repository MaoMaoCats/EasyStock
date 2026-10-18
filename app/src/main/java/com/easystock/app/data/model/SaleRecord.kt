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
    tableName = "sale_records",
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
data class SaleRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fruitTypeId: Long,
    @Serializable(with = BigDecimalSerializer::class)
    val quantity: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val unitPrice: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val totalRevenue: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val unitCost: BigDecimal = BigDecimal.ZERO,
    @Serializable(with = BigDecimalSerializer::class)
    val costOfGoodsSold: BigDecimal = BigDecimal.ZERO,
    val saleDate: Long,
    val dateString: String, // YYYY-MM-DD
    val paymentMethod: String = "เงินสด", // "เงินสด", "พร้อมเพย์ / สแกน"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
