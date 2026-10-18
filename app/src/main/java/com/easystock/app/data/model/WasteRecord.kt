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
    tableName = "waste_records",
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
data class WasteRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fruitTypeId: Long,
    @Serializable(with = BigDecimalSerializer::class)
    val quantity: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val estimatedUnitCost: BigDecimal = BigDecimal.ZERO,
    @Serializable(with = BigDecimalSerializer::class)
    val totalWasteCost: BigDecimal = BigDecimal.ZERO,
    val wasteDate: Long,
    val dateString: String, // YYYY-MM-DD
    val reason: String = "เน่าเสีย", // "เน่าเสีย", "ช้ำ/เสียหาย", "สุกเกินไป", "อื่นๆ"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
