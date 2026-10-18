package com.easystock.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.easystock.app.util.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
@Entity(tableName = "fruit_types")
data class FruitType(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val defaultUnit: String, // e.g. "กก.", "ผล", "หวี", "กล่อง", "แพ็ค", "ถุง"
    @Serializable(with = BigDecimalSerializer::class)
    val defaultCostPrice: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val defaultSellingPrice: BigDecimal? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
