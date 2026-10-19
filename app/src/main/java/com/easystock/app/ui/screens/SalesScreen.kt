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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.easystock.app.data.model.FruitType
import com.easystock.app.data.model.SaleRecord
import com.easystock.app.ui.components.ConfirmDialog
import com.easystock.app.ui.components.EmptyStateView
import com.easystock.app.util.ThaiFormatUtils
import java.math.BigDecimal

@Composable
fun SalesScreen(
    sales: List<SaleRecord>,
    allSales: List<SaleRecord>,
    fruits: List<FruitType>,
    selectedDate: String,
    onAddSale: () -> Unit,
    onDeleteSale: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterTodayOnly by remember { mutableStateOf(false) }
    var saleToDelete by remember { mutableStateOf<SaleRecord?>(null) }

    val fruitsMap = remember(fruits) { fruits.associateBy { it.id } }
    val displayedSales = if (filterTodayOnly) sales else allSales

    if (saleToDelete != null) {
        val s = saleToDelete!!
        val fruit = fruitsMap[s.fruitTypeId]
        ConfirmDialog(
            title = "ยกเลิกรายการขาย",
            message = "ต้องการยกเลิกการขาย ${fruit?.name ?: "ผลไม้"} จำนวน ${ThaiFormatUtils.formatQuantity(s.quantity)} ${fruit?.defaultUnit ?: "หน่วย"} (ยอด ${ThaiFormatUtils.formatBaht(s.totalRevenue)}) ใช่หรือไม่?\nสต็อกจะถูกคืนกลับเข้าคลังอัตโนมัติ",
            confirmText = "ยกเลิกรายการขาย",
            cancelText = "ปิด",
            isDestructive = true,
            onConfirm = {
                onDeleteSale(s.id)
                saleToDelete = null
            },
            onDismiss = { saleToDelete = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Large Add Sale Button
        Button(
            onClick = onAddSale,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "บันทึกการขายผลไม้ (+)",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter and revenue summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !filterTodayOnly,
                    onClick = { filterTodayOnly = false },
                    label = { Text("ทั้งหมด (${allSales.size})") }
                )
                FilterChip(
                    selected = filterTodayOnly,
                    onClick = { filterTodayOnly = true },
                    label = { Text("วันนี้ (${sales.size})") }
                )
            }

            val totalRev = displayedSales.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.totalRevenue) }
            Text(
                text = "ยอดขายรวม: ${ThaiFormatUtils.formatBaht(totalRev)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (displayedSales.isEmpty()) {
            EmptyStateView(
                title = "ยังไม่มีรายการขาย",
                message = if (filterTodayOnly) "วันนี้ยังไม่มีรายการขายผลไม้" else "กดปุ่มด้านบนเพื่อบันทึกการขาย",
                icon = Icons.Default.PointOfSale,
                actionButtonText = "+ บันทึกการขาย",
                onActionClick = onAddSale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedSales, key = { it.id }) { sale ->
                    val fruit = fruitsMap[sale.fruitTypeId]
                    SaleCard(
                        sale = sale,
                        fruit = fruit,
                        onDelete = { saleToDelete = sale }
                    )
                }
            }
        }
    }
}

@Composable
fun SaleCard(
    sale: SaleRecord,
    fruit: FruitType?,
    onDelete: () -> Unit
) {
    val unit = fruit?.defaultUnit ?: "หน่วย"
    val grossProfit = sale.totalRevenue.subtract(sale.costOfGoodsSold)

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
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = fruit?.name ?: "ผลไม้ (รหัส #${sale.fruitTypeId})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFFE8F5E9),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = sale.paymentMethod,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${ThaiFormatUtils.formatFriendlyDate(sale.dateString)} • เวลา ${ThaiFormatUtils.formatTime(sale.saleDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "ยกเลิกรายการขาย",
                        tint = MaterialTheme.colorScheme.error
                    )
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
                        text = "จำนวนขาย: ${ThaiFormatUtils.formatQuantityWithUnit(sale.quantity, unit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "ราคาขาย @ ${ThaiFormatUtils.formatBaht(sale.unitPrice)} / $unit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = ThaiFormatUtils.formatBaht(sale.totalRevenue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "กำไร: ${ThaiFormatUtils.formatBaht(grossProfit)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            if (sale.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "หมายเหตุ: ${sale.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
