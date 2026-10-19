package com.easystock.app.ui.screens

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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Delete
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
import com.easystock.app.data.model.PurchaseRecord
import com.easystock.app.ui.components.ConfirmDialog
import com.easystock.app.ui.components.EmptyStateView
import com.easystock.app.util.ThaiFormatUtils
import java.math.BigDecimal

@Composable
fun PurchasesScreen(
    purchases: List<PurchaseRecord>,
    allPurchases: List<PurchaseRecord>,
    fruits: List<FruitType>,
    selectedDate: String,
    onAddPurchase: () -> Unit,
    onDeletePurchase: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterTodayOnly by remember { mutableStateOf(false) }
    var purchaseToDelete by remember { mutableStateOf<PurchaseRecord?>(null) }

    val fruitsMap = remember(fruits) { fruits.associateBy { it.id } }

    val displayedPurchases = if (filterTodayOnly) purchases else allPurchases

    // Delete confirmation dialog
    if (purchaseToDelete != null) {
        val p = purchaseToDelete!!
        val fruit = fruitsMap[p.fruitTypeId]
        ConfirmDialog(
            title = "ลบรายการซื้อเข้า",
            message = "ต้องการลบรายการซื้อ ${fruit?.name ?: "ผลไม้"} จำนวน ${ThaiFormatUtils.formatQuantity(p.quantity)} ${fruit?.defaultUnit ?: "หน่วย"} (ยอด ${ThaiFormatUtils.formatBaht(p.totalCost)}) ใช่หรือไม่?\nสต็อกจะถูกปรับลดย้อนหลังตามจำนวนที่ลบ",
            confirmText = "ลบรายการ",
            cancelText = "ยกเลิก",
            isDestructive = true,
            onConfirm = {
                onDeletePurchase(p.id)
                purchaseToDelete = null
            },
            onDismiss = { purchaseToDelete = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Large Add Purchase Button
        Button(
            onClick = onAddPurchase,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "บันทึกซื้อผลไม้เข้าสต็อก (+)",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips & summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !filterTodayOnly,
                    onClick = { filterTodayOnly = false },
                    label = { Text("ทั้งหมด (${allPurchases.size})") }
                )
                FilterChip(
                    selected = filterTodayOnly,
                    onClick = { filterTodayOnly = true },
                    label = { Text("วันนี้ (${purchases.size})") }
                )
            }

            val totalCost = displayedPurchases.fold(BigDecimal.ZERO) { acc, item -> acc.add(item.totalCost) }
            Text(
                text = "รวม: ${ThaiFormatUtils.formatBaht(totalCost)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (displayedPurchases.isEmpty()) {
            EmptyStateView(
                title = "ยังไม่มีรายการซื้อเข้า",
                message = if (filterTodayOnly) "วันนี้ยังไม่มีรายการซื้อผลไม้เข้า" else "กดปุ่มด้านบนเพื่อบันทึกการซื้อผลไม้เข้าสต็อก",
                icon = Icons.Default.AddShoppingCart,
                actionButtonText = "+ บันทึกการซื้อเข้า",
                onActionClick = onAddPurchase,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayedPurchases, key = { it.id }) { purchase ->
                    val fruit = fruitsMap[purchase.fruitTypeId]
                    PurchaseCard(
                        purchase = purchase,
                        fruit = fruit,
                        onDelete = { purchaseToDelete = purchase }
                    )
                }
            }
        }
    }
}

@Composable
fun PurchaseCard(
    purchase: PurchaseRecord,
    fruit: FruitType?,
    onDelete: () -> Unit
) {
    val unit = fruit?.defaultUnit ?: "หน่วย"

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
                    Text(
                        text = fruit?.name ?: "ผลไม้ (รหัส #${purchase.fruitTypeId})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${ThaiFormatUtils.formatFriendlyDate(purchase.dateString)} • เวลา ${ThaiFormatUtils.formatTime(purchase.purchaseDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "ลบรายการ",
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
                        text = "จำนวนซื้อ: ${ThaiFormatUtils.formatQuantityWithUnit(purchase.quantity, unit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "ต้นทุน @ ${ThaiFormatUtils.formatBaht(purchase.unitCost)} / $unit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = ThaiFormatUtils.formatBaht(purchase.totalCost),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            }

            if (purchase.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "หมายเหตุ: ${purchase.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
