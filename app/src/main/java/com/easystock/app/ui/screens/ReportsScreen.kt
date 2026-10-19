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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.easystock.app.data.model.DailyFruitSummary
import com.easystock.app.data.model.DailySummary
import com.easystock.app.data.model.FruitStockSummary
import com.easystock.app.data.model.FruitType
import com.easystock.app.data.model.StockCountRecord
import com.easystock.app.data.model.WasteRecord
import com.easystock.app.ui.components.ConfirmDialog
import com.easystock.app.ui.components.StatCard
import com.easystock.app.util.ThaiFormatUtils
import java.math.BigDecimal

@Composable
fun ReportsScreen(
    dailySummary: DailySummary,
    selectedDate: String,
    onSelectDate: (String) -> Unit,
    stockSummaries: List<FruitStockSummary>,
    stockCounts: List<StockCountRecord>,
    wasteRecords: List<WasteRecord>,
    fruits: List<FruitType>,
    onOpenStockCountForFruit: (FruitStockSummary) -> Unit,
    onOpenGeneralStockCount: () -> Unit,
    onOpenRecordWaste: () -> Unit,
    onDeleteWaste: (Long) -> Unit,
    onOpenBackupRestore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: สรุปยอด, 1: ตรวจนับสต็อก, 2: ของเสียและสำรองข้อมูล
    var wasteToDelete by remember { mutableStateOf<WasteRecord?>(null) }

    val fruitsMap = remember(fruits) { fruits.associateBy { it.id } }

    if (wasteToDelete != null) {
        val w = wasteToDelete!!
        val fruit = fruitsMap[w.fruitTypeId]
        ConfirmDialog(
            title = "ลบรายการของเสีย",
            message = "ต้องการลบรายการของเสีย ${fruit?.name ?: "ผลไม้"} จำนวน ${ThaiFormatUtils.formatQuantity(w.quantity)} ${fruit?.defaultUnit ?: "หน่วย"} ใช่หรือไม่?",
            confirmText = "ลบรายการ",
            cancelText = "ยกเลิก",
            isDestructive = true,
            onConfirm = {
                onDeleteWaste(w.id)
                wasteToDelete = null
            },
            onDismiss = { wasteToDelete = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("สรุปประจำวัน") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("ตรวจนับสต็อก") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("ของเสีย & สำรอง") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> DailySummaryTabView(
                summary = dailySummary,
                selectedDate = selectedDate,
                onSelectDate = onSelectDate
            )
            1 -> DailyStockCountTabView(
                stockSummaries = stockSummaries,
                stockCounts = stockCounts,
                onCountFruit = onOpenStockCountForFruit,
                onGeneralCount = onOpenGeneralStockCount
            )
            2 -> WasteAndBackupTabView(
                wasteRecords = wasteRecords,
                fruitsMap = fruitsMap,
                onAddWaste = onOpenRecordWaste,
                onDeleteWaste = { wasteToDelete = it },
                onOpenBackupRestore = onOpenBackupRestore
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Tab 1: Daily Summary View
// -----------------------------------------------------------------------------
@Composable
fun DailySummaryTabView(
    summary: DailySummary,
    selectedDate: String,
    onSelectDate: (String) -> Unit
) {
    val today = ThaiFormatUtils.todayDateString()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Date switcher chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedDate == today,
                    onClick = { onSelectDate(today) },
                    label = { Text("วันนี้") }
                )
                Text(
                    text = "รายงานวันที่: ${ThaiFormatUtils.formatFriendlyDate(selectedDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Financial Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "ยอดขายรวม",
                        value = ThaiFormatUtils.formatBaht(summary.totalSalesRevenue),
                        icon = Icons.Default.PointOfSale,
                        containerColor = Color(0xFFE8F5E9),
                        contentColor = Color(0xFF1B5E20),
                        valueColor = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "กำไรขั้นต้น",
                        value = ThaiFormatUtils.formatBaht(summary.grossProfit),
                        subtitle = "กำไร ${String.format("%.1f", summary.profitMarginPercent)}%",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        containerColor = Color(0xFFFFF3E0),
                        contentColor = Color(0xFFE65100),
                        valueColor = Color(0xFFEF6C00),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "ต้นทุนสินค้าที่ขาย (COGS)",
                        value = ThaiFormatUtils.formatBaht(summary.totalCostOfGoodsSold),
                        icon = Icons.Default.Assessment,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "ของเสีย/คัดทิ้ง",
                        value = ThaiFormatUtils.formatBaht(summary.totalRecordedWasteCost),
                        subtitle = "${ThaiFormatUtils.formatQuantity(summary.totalRecordedWasteQty)} หน่วย",
                        icon = Icons.Default.Delete,
                        containerColor = if (summary.totalRecordedWasteCost > BigDecimal.ZERO) {
                            Color(0xFFFFEBEE)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (summary.totalRecordedWasteCost > BigDecimal.ZERO) {
                            Color(0xFFC62828)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "ยอดซื้อผลไม้เข้าร้าน",
                        value = ThaiFormatUtils.formatBaht(summary.totalPurchasesCost),
                        subtitle = "${ThaiFormatUtils.formatQuantity(summary.totalPurchasesQty)} หน่วย",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "สต็อกคงเหลือปัจจุบัน",
                        value = ThaiFormatUtils.formatBaht(summary.remainingStockTotalValue),
                        subtitle = "${ThaiFormatUtils.formatQuantity(summary.remainingStockTotalQty)} หน่วย",
                        icon = Icons.Default.Inventory,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Fruit Breakdown Header
        item {
            Text(
                text = "รายละเอียดแยกตามชนิดผลไม้",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (summary.fruitSummaries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ยังไม่มีข้อมูลการขายหรือความเคลื่อนไหวในวันที่เลือก",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(summary.fruitSummaries, key = { it.fruitType.id }) { item ->
                FruitSummaryRowCard(item = item)
            }
        }
    }
}

@Composable
fun FruitSummaryRowCard(item: DailyFruitSummary) {
    val fruit = item.fruitType
    val unit = fruit.defaultUnit

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
                Text(
                    text = fruit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "สต็อกคงเหลือ: ${ThaiFormatUtils.formatQuantityWithUnit(item.currentStock, unit)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ขายได้: ${ThaiFormatUtils.formatQuantityWithUnit(item.quantitySold, unit)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "ยอดขาย: ${ThaiFormatUtils.formatBaht(item.revenue)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "กำไร: ${ThaiFormatUtils.formatBaht(item.grossProfit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "ต้นทุนขาย: ${ThaiFormatUtils.formatBaht(item.costOfGoodsSold)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (item.wasteQty > BigDecimal.ZERO || item.purchasedQty > BigDecimal.ZERO) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (item.purchasedQty > BigDecimal.ZERO) {
                        Text(
                            text = "ซื้อเข้า: ${ThaiFormatUtils.formatQuantityWithUnit(item.purchasedQty, unit)} (${ThaiFormatUtils.formatBaht(item.purchasedCost)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100)
                        )
                    }
                    if (item.wasteQty > BigDecimal.ZERO) {
                        Text(
                            text = "ของเสีย: ${ThaiFormatUtils.formatQuantityWithUnit(item.wasteQty, unit)} (${ThaiFormatUtils.formatBaht(item.wasteCost)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Tab 2: Daily Stock Count Tab View
// -----------------------------------------------------------------------------
@Composable
fun DailyStockCountTabView(
    stockSummaries: List<FruitStockSummary>,
    stockCounts: List<StockCountRecord>,
    onCountFruit: (FruitStockSummary) -> Unit,
    onGeneralCount: () -> Unit
) {
    val activeSummaries = remember(stockSummaries) {
        stockSummaries.filter { it.fruitType.isActive }
    }
    val countedFruitMap = remember(stockCounts) {
        stockCounts.associateBy { it.fruitTypeId }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "การตรวจนับสต็อกปิดร้าน (Daily Closing Count)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "นับจำนวนผลไม้ที่เหลือจริงตอนปิดร้าน ระบบจะคำนวณส่วนต่าง หากของขาดสามารถบันทึกเป็นของเสียได้โดยไม่สร้างยอดขายแอบแฝง",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onGeneralCount,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("เริ่มตรวจนับสต็อกวันนี้")
                    }
                }
            }
        }

        item {
            Text(
                text = "สถานะการตรวจนับสต็อกวันนี้",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (activeSummaries.isEmpty()) {
            item {
                Text(
                    text = "ยังไม่มีผลไม้ที่เปิดใช้งาน",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(activeSummaries, key = { it.fruitType.id }) { summary ->
                val fruit = summary.fruitType
                val countRecord = countedFruitMap[fruit.id]
                val unit = fruit.defaultUnit

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
                            Text(
                                text = fruit.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (countRecord != null) {
                                val discrepancy = countRecord.discrepancyQty
                                val (bg, fg, statusText) = when {
                                    discrepancy == BigDecimal.ZERO -> {
                                        Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "นับแล้ว ตรงกัน ✓")
                                    }
                                    discrepancy < BigDecimal.ZERO -> {
                                        Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "ของขาด ${ThaiFormatUtils.formatQuantity(discrepancy.abs())} $unit")
                                    }
                                    else -> {
                                        Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "ของเกิน ${ThaiFormatUtils.formatQuantity(discrepancy)} $unit")
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .background(bg, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = fg
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFEEEEEE), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ยังไม่ได้นับวันนี้",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "สต็อกคาดหวัง: ${ThaiFormatUtils.formatQuantityWithUnit(summary.currentStock, unit)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (countRecord != null) {
                                    Text(
                                        text = "นับได้จริง: ${ThaiFormatUtils.formatQuantityWithUnit(countRecord.actualQty, unit)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Button(
                                onClick = { onCountFruit(summary) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (countRecord != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(if (countRecord != null) "นับใหม่" else "ตรวจนับ")
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Tab 3: Waste and Backup View
// -----------------------------------------------------------------------------
@Composable
fun WasteAndBackupTabView(
    wasteRecords: List<WasteRecord>,
    fruitsMap: Map<Long, FruitType>,
    onAddWaste: () -> Unit,
    onDeleteWaste: (WasteRecord) -> Unit,
    onOpenBackupRestore: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Backup card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Backup,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "สำรองและกู้คืนข้อมูล (Local Backup)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "แอปทำงานแบบออฟไลน์ 100% ท่านสามารถส่งออกข้อมูลเป็นไฟล์ JSON เพื่อสำรองเก็บไว้ หรือนำไปเปิดในเครื่องอื่นได้",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onOpenBackupRestore,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("จัดการสำรอง / กู้คืนข้อมูล")
                    }
                }
            }
        }

        // Waste Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ประวัติของเสีย / ผลไม้คัดทิ้ง",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onAddWaste,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("บันทึกของเสีย")
                }
            }
        }

        if (wasteRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ยังไม่มีประวัติของเสียในวันที่เลือก",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(wasteRecords, key = { it.id }) { record ->
                val fruit = fruitsMap[record.fruitTypeId]
                val unit = fruit?.defaultUnit ?: "หน่วย"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fruit?.name ?: "ผลไม้",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "สาเหตุ: ${record.reason} • ${ThaiFormatUtils.formatFriendlyDate(record.dateString)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "จำนวนคัดทิ้ง: ${ThaiFormatUtils.formatQuantityWithUnit(record.quantity, unit)} (มูลค่าต้นทุน: ${ThaiFormatUtils.formatBaht(record.totalWasteCost)})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error
                            )
                            if (record.notes.isNotBlank()) {
                                Text(
                                    text = "หมายเหตุ: ${record.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { onDeleteWaste(record) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "ลบ",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
