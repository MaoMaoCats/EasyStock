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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.easystock.app.data.model.FruitStockSummary
import com.easystock.app.data.model.FruitType
import com.easystock.app.ui.components.ConfirmDialog
import com.easystock.app.ui.components.EmptyStateView
import com.easystock.app.util.ThaiFormatUtils

@Composable
fun FruitTypesScreen(
    allFruits: List<FruitType>,
    stockSummaries: List<FruitStockSummary>,
    onAddFruitType: () -> Unit,
    onEditFruitType: (FruitType) -> Unit,
    onToggleActive: (FruitType) -> Unit,
    onDeleteFruitType: (FruitType) -> Unit,
    onSeedFruits: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Active, 1: Inactive
    var fruitToToggle by remember { mutableStateOf<FruitType?>(null) }
    var fruitToDelete by remember { mutableStateOf<FruitType?>(null) }

    val stockMap = remember(stockSummaries) {
        stockSummaries.associateBy { it.fruitType.id }
    }

    val activeFruits = remember(allFruits) { allFruits.filter { it.isActive } }
    val inactiveFruits = remember(allFruits) { allFruits.filter { !it.isActive } }

    val currentList = if (selectedTab == 0) activeFruits else inactiveFruits

    // Toggle active dialog
    if (fruitToToggle != null) {
        val fruit = fruitToToggle!!
        ConfirmDialog(
            title = if (fruit.isActive) "ปิดการใช้งาน ${fruit.name}" else "เปิดใช้งาน ${fruit.name}",
            message = if (fruit.isActive) {
                "เมื่อปิดการใช้งาน ผลไม้นี้จะไม่แสดงในรายการเลือกซื้อเข้าและขายใหม่ แต่ประวัติการซื้อ-ขายในอดีตจะยังคงอยู่ครบถ้วน"
            } else {
                "ต้องการเปิดใช้งานผลไม้นี้ให้กลับมาแสดงในรายการซื้อเข้าและขายใช่หรือไม่?"
            },
            confirmText = if (fruit.isActive) "ปิดการใช้งาน" else "เปิดใช้งาน",
            cancelText = "ยกเลิก",
            isDestructive = fruit.isActive,
            onConfirm = {
                onToggleActive(fruit)
                fruitToToggle = null
            },
            onDismiss = { fruitToToggle = null }
        )
    }

    // Delete dialog
    if (fruitToDelete != null) {
        val fruit = fruitToDelete!!
        ConfirmDialog(
            title = "ลบ ${fruit.name}",
            message = "หากมีประวัติการซื้อหรือขายในอดีต ระบบจะทำการปิดการใช้งานแทนการลบ เพื่อรักษาข้อมูลประวัติบัญชีไว้",
            confirmText = "ลบ / ปิดใช้งาน",
            cancelText = "ยกเลิก",
            isDestructive = true,
            onConfirm = {
                onDeleteFruitType(fruit)
                fruitToDelete = null
            },
            onDismiss = { fruitToDelete = null }
        )
    }

    if (allFruits.isEmpty()) {
        EmptyStateView(
            title = "ยังไม่มีชนิดผลไม้ในร้าน",
            message = "กรุณาเพิ่มชนิดผลไม้ก่อน เพื่อเริ่มบันทึกการซื้อเข้าและขาย",
            actionButtonText = "+ เพิ่มชนิดผลไม้แรก",
            onActionClick = onAddFruitType,
            secondaryButtonText = "โหลดผลไม้ยอดนิยมเริ่มต้น",
            onSecondaryClick = onSeedFruits,
            modifier = modifier.fillMaxSize()
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Add Button
        Button(
            onClick = onAddFruitType,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "เพิ่มชนิดผลไม้ใหม่",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("กำลังขาย (${activeFruits.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("ปิดใช้งาน (${inactiveFruits.size})") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (currentList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedTab == 0) "ไม่มีผลไม้ที่กำลังขายอยู่" else "ไม่มีผลไม้ที่ปิดใช้งาน",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(currentList, key = { it.id }) { fruit ->
                    val stock = stockMap[fruit.id]?.currentStock
                    FruitTypeCard(
                        fruit = fruit,
                        currentStock = stock,
                        onEdit = { onEditFruitType(fruit) },
                        onToggleActive = { fruitToToggle = fruit },
                        onDelete = { fruitToDelete = fruit }
                    )
                }
            }
        }
    }
}

@Composable
fun FruitTypeCard(
    fruit: FruitType,
    currentStock: java.math.BigDecimal?,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (fruit.isActive) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        ),
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
                            text = fruit.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "หน่วย: ${fruit.defaultUnit}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "ต้นทุนแนะนำ: ${fruit.defaultCostPrice?.let { ThaiFormatUtils.formatBaht(it) } ?: "ไม่ระบุ"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "ราคาขายแนะนำ: ${fruit.defaultSellingPrice?.let { ThaiFormatUtils.formatBaht(it) } ?: "ไม่ระบุ"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (currentStock != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "สต็อกคงเหลือปัจจุบัน: ${ThaiFormatUtils.formatQuantityWithUnit(currentStock, fruit.defaultUnit)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Action buttons
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "แก้ไข", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onToggleActive) {
                        if (fruit.isActive) {
                            Icon(Icons.Default.VisibilityOff, contentDescription = "ปิดใช้งาน", tint = Color(0xFFE65100))
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "เปิดใช้งาน", tint = Color(0xFF2E7D32))
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "ลบ", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
