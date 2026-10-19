package com.easystock.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easystock.app.data.model.DailySummary
import com.easystock.app.data.model.FruitStockSummary
import com.easystock.app.data.model.FruitType
import com.easystock.app.ui.components.EmptyStateView
import com.easystock.app.ui.components.StatCard
import com.easystock.app.util.ThaiFormatUtils
import java.math.BigDecimal

@Composable
fun HomeScreen(
    todaySummary: DailySummary,
    stockSummaries: List<FruitStockSummary>,
    allFruits: List<FruitType>,
    onQuickSale: (FruitType) -> Unit,
    onQuickPurchase: (FruitType) -> Unit,
    onOpenSales: () -> Unit,
    onOpenPurchases: () -> Unit,
    onOpenStockCount: () -> Unit,
    onOpenFruitTypes: () -> Unit,
    onSeedFruits: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (allFruits.isEmpty()) {
        EmptyStateView(
            title = "ยินดีต้อนรับสู่ EasyStock",
            message = "ระบบจัดการสต็อกและบันทึกยอดขายผลไม้\nเริ่มต้นด้วยการเพิ่มชนิดผลไม้ของร้านคุณ",
            actionButtonText = "+ เพิ่มชนิดผลไม้แรก",
            onActionClick = onOpenFruitTypes,
            secondaryButtonText = "โหลดผลไม้ยอดนิยม (ทุเรียน, มะม่วง, กล้วย ฯลฯ)",
            onSecondaryClick = onSeedFruits,
            modifier = modifier.fillMaxSize()
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today Date Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "สรุปภาพรวมร้านวันนี้",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = ThaiFormatUtils.formatFullThaiDate(System.currentTimeMillis()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Key Today Stats Grid (Sales & Profit)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "ยอดขายวันนี้",
                    value = ThaiFormatUtils.formatBaht(todaySummary.totalSalesRevenue),
                    subtitle = "ขายได้ ${todaySummary.fruitSummaries.sumOf { it.quantitySold.toDouble() }.toInt()} รายการ",
                    icon = Icons.Default.PointOfSale,
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF1B5E20),
                    valueColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "กำไรขั้นต้น",
                    value = ThaiFormatUtils.formatBaht(todaySummary.grossProfit),
                    subtitle = "มาร์จิ้น ${String.format("%.1f", todaySummary.profitMarginPercent)}%",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    containerColor = Color(0xFFFFF3E0),
                    contentColor = Color(0xFFE65100),
                    valueColor = Color(0xFFEF6C00),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Secondary Stats (Stock Value & Waste)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "มูลค่าสต็อกคงเหลือ",
                    value = ThaiFormatUtils.formatBaht(todaySummary.remainingStockTotalValue),
                    subtitle = "คงเหลือรวม ${ThaiFormatUtils.formatQuantity(todaySummary.remainingStockTotalQty)} หน่วย",
                    icon = Icons.Default.Inventory,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "ของเสียวันนี้",
                    value = ThaiFormatUtils.formatBaht(todaySummary.totalRecordedWasteCost),
                    subtitle = "${ThaiFormatUtils.formatQuantity(todaySummary.totalRecordedWasteQty)} หน่วยคัดทิ้ง",
                    icon = Icons.Default.Delete,
                    containerColor = if (todaySummary.totalRecordedWasteCost > BigDecimal.ZERO) {
                        Color(0xFFFFEBEE)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (todaySummary.totalRecordedWasteCost > BigDecimal.ZERO) {
                        Color(0xFFC62828)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Big Touch Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "เมนูลัด (ทำรายการด่วน)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenSales,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(28.dp))
                            Text(
                                text = "บันทึกขาย\nผลไม้",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Button(
                        onClick = onOpenPurchases,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(28.dp))
                            Text(
                                text = "ซื้อผลไม้\nเข้าสต็อก",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = onOpenStockCount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "ตรวจนับสต็อกปิดร้าน (สิ้นวัน)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Stock Overview List Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "สต็อกผลไม้ปัจจุบัน",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${stockSummaries.count { it.fruitType.isActive }} ชนิด",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Active Fruit Stock Cards
        val activeSummaries = stockSummaries.filter { it.fruitType.isActive }
        if (activeSummaries.isEmpty()) {
            item {
                Text(
                    text = "ไม่มีผลไม้ที่เปิดใช้งานอยู่",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(activeSummaries, key = { it.fruitType.id }) { summary ->
                HomeFruitStockCard(
                    summary = summary,
                    onQuickSale = { onQuickSale(summary.fruitType) },
                    onQuickPurchase = { onQuickPurchase(summary.fruitType) }
                )
            }
        }
    }
}

@Composable
fun HomeFruitStockCard(
    summary: FruitStockSummary,
    onQuickSale: () -> Unit,
    onQuickPurchase: () -> Unit
) {
    val fruit = summary.fruitType
    val currentStock = summary.currentStock
    val unit = fruit.defaultUnit

    val (badgeBg, badgeText, statusText) = when {
        currentStock > BigDecimal.valueOf(5) -> {
            Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "มีของพร้อมขาย")
        }
        currentStock > BigDecimal.ZERO -> {
            Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "สต็อกเหลือน้อย")
        }
        else -> {
            Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "สต็อกหมด")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = fruit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ราคาขาย: ${fruit.defaultSellingPrice?.let { ThaiFormatUtils.formatBaht(it) } ?: "-"} / $unit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Current Stock Badge
                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = ThaiFormatUtils.formatQuantityWithUnit(currentStock, unit),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeText
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onQuickPurchase,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("ซื้อเพิ่ม")
                }

                Button(
                    onClick = onQuickSale,
                    modifier = Modifier.weight(1f),
                    enabled = currentStock > BigDecimal.ZERO,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("ขาย")
                }
            }
        }
    }
}
