package com.easystock.app.ui.screens.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.easystock.app.data.model.FruitStockSummary
import com.easystock.app.ui.components.QuickNumberRow
import com.easystock.app.util.ThaiFormatUtils
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockCountDialog(
    availableFruits: List<FruitStockSummary>,
    initialFruitSummary: FruitStockSummary? = null,
    onDismiss: () -> Unit,
    onSave: (fruitTypeId: Long, actualQty: BigDecimal, recordWaste: Boolean, wasteReason: String, notes: String) -> Unit
) {
    val activeFruitSummaries = remember(availableFruits) {
        availableFruits.filter { it.fruitType.isActive }
    }

    var selectedSummary by remember {
        mutableStateOf(initialFruitSummary ?: activeFruitSummaries.firstOrNull())
    }
    var expanded by remember { mutableStateOf(false) }

    var actualQtyStr by remember {
        mutableStateOf(selectedSummary?.currentStock?.stripTrailingZeros()?.toPlainString() ?: "")
    }
    var recordWasteForShortage by remember { mutableStateOf(true) }
    var wasteReason by remember { mutableStateOf("คัดทิ้งจากการตรวจนับสิ้นวัน") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val expectedQty = selectedSummary?.currentStock ?: BigDecimal.ZERO
    val unit = selectedSummary?.fruitType?.defaultUnit ?: "กก."

    val actualQty = try { BigDecimal(actualQtyStr) } catch (_: Exception) { null }
    val discrepancy = if (actualQty != null) actualQty.subtract(expectedQty) else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "ตรวจนับสต็อกปิดร้าน (นับสต็อกจริง)", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "กรอกจำนวนผลไม้ที่เหลืออยู่จริงหน้าร้าน ระบบจะคำนวณส่วนต่างอัตโนมัติ โดยไม่สร้างยอดขายแอบแฝง",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Fruit Selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedSummary?.fruitType?.name ?: "เลือกผลไม้",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("ชนิดผลไม้ *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        activeFruitSummaries.forEach { summary ->
                            DropdownMenuItem(
                                text = {
                                    Text("${summary.fruitType.name} (สต็อกคาดหวัง: ${ThaiFormatUtils.formatQuantityWithUnit(summary.currentStock, summary.fruitType.defaultUnit)})")
                                },
                                onClick = {
                                    selectedSummary = summary
                                    actualQtyStr = summary.currentStock.stripTrailingZeros().toPlainString()
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Expected Stock Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "สต็อกที่คาดหวังตามระบบ:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = ThaiFormatUtils.formatQuantityWithUnit(expectedQty, unit),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actual Count Input
                OutlinedTextField(
                    value = actualQtyStr,
                    onValueChange = {
                        actualQtyStr = it
                        errorMsg = null
                    },
                    label = { Text("สต็อกที่นับได้จริง *") },
                    placeholder = { Text("เช่น 10, 12.5") },
                    suffix = { Text(unit) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                QuickNumberRow(
                    increments = listOf(1.0, 5.0, 10.0),
                    unit = unit,
                    onAdd = { inc ->
                        val current = try { BigDecimal(actualQtyStr) } catch (_: Exception) { BigDecimal.ZERO }
                        actualQtyStr = current.add(BigDecimal.valueOf(inc)).stripTrailingZeros().toPlainString()
                        errorMsg = null
                    },
                    onReset = { actualQtyStr = "" }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Discrepancy Box
                if (discrepancy != null) {
                    val (boxColor, textColor, textContent, icon) = when {
                        discrepancy == BigDecimal.ZERO -> {
                            Quadruple(
                                Color(0xFFE8F5E9),
                                Color(0xFF2E7D32),
                                "จำนวนตรงกันพอดี ไม่พบส่วนต่าง",
                                Icons.Default.CheckCircle
                            )
                        }
                        discrepancy < BigDecimal.ZERO -> {
                            val shortage = discrepancy.abs()
                            Quadruple(
                                Color(0xFFFFEBEE),
                                Color(0xFFC62828),
                                "ของขาดไป ${ThaiFormatUtils.formatQuantityWithUnit(shortage, unit)} (สูญหายหรือเน่าเสีย)",
                                Icons.Default.Warning
                            )
                        }
                        else -> {
                            Quadruple(
                                Color(0xFFFFF8E1),
                                Color(0xFFF57F17),
                                "ของเกิน ${ThaiFormatUtils.formatQuantityWithUnit(discrepancy, unit)} (อาจมีซื้อเข้าตกหล่น)",
                                Icons.Default.Warning
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(boxColor, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "ผลการตรวจนับ:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor
                                )
                                Text(
                                    text = textContent,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }
                    }

                    // If shortage, option to record waste automatically
                    if (discrepancy < BigDecimal.ZERO) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = recordWasteForShortage,
                                onCheckedChange = { recordWasteForShortage = it }
                            )
                            Text(
                                text = "บันทึกส่วนที่ขาดเป็นของเสียอัตโนมัติ เพื่อปรับสต็อกให้ตรง",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("หมายเหตุการตรวจนับ (ถ้ามี)") },
                    placeholder = { Text("เช่น ตรวจนับสิ้นวันรอบ 20:00 น.") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val summary = selectedSummary
                    if (summary == null) {
                        errorMsg = "กรุณาเลือกชนิดผลไม้"
                        return@Button
                    }
                    val qty = try { BigDecimal(actualQtyStr) } catch (_: Exception) { null }
                    if (qty == null || qty < BigDecimal.ZERO) {
                        errorMsg = "กรุณากรอกจำนวนสต็อกจริงที่ถูกต้อง"
                        return@Button
                    }

                    onSave(
                        summary.fruitType.id,
                        qty,
                        recordWasteForShortage,
                        wasteReason,
                        notes.trim()
                    )
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("บันทึกการตรวจนับ")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ยกเลิก")
            }
        }
    )
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
