package com.easystock.app.ui.screens.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.easystock.app.data.model.FruitStockSummary
import com.easystock.app.data.model.FruitType
import com.easystock.app.ui.components.QuickNumberRow
import com.easystock.app.util.ThaiFormatUtils
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleDialog(
    availableFruits: List<FruitStockSummary>,
    preselectedFruit: FruitType? = null,
    onDismiss: () -> Unit,
    onSave: (fruitTypeId: Long, quantity: BigDecimal, unitPrice: BigDecimal, saleDate: Long, paymentMethod: String, notes: String) -> Unit
) {
    // Only allow fruits that are active
    val activeFruitSummaries = remember(availableFruits) {
        availableFruits.filter { it.fruitType.isActive }
    }

    var selectedSummary by remember {
        mutableStateOf(
            activeFruitSummaries.find { it.fruitType.id == preselectedFruit?.id }
                ?: activeFruitSummaries.firstOrNull()
        )
    }

    var expanded by remember { mutableStateOf(false) }

    var quantityStr by remember { mutableStateOf("") }
    var unitPriceStr by remember {
        mutableStateOf(selectedSummary?.fruitType?.defaultSellingPrice?.toPlainString() ?: "")
    }
    var paymentMethod by remember { mutableStateOf("เงินสด") }
    var notes by remember { mutableStateOf("") }
    var saleDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Live calculation
    val currentStock = selectedSummary?.currentStock ?: BigDecimal.ZERO
    val unit = selectedSummary?.fruitType?.defaultUnit ?: "กก."

    val quantity = try { BigDecimal(quantityStr) } catch (_: Exception) { BigDecimal.ZERO }
    val unitPrice = try { BigDecimal(unitPriceStr) } catch (_: Exception) { BigDecimal.ZERO }
    val totalRevenue = if (quantity > BigDecimal.ZERO && unitPrice >= BigDecimal.ZERO) {
        quantity.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP)
    } else {
        BigDecimal.ZERO
    }

    val isStockInsufficient = quantity > currentStock

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "บันทึกการขายผลไม้", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (activeFruitSummaries.isEmpty()) {
                    Text(
                        text = "ยังไม่มีชนิดผลไม้ในระบบ กรุณาเพิ่มชนิดผลไม้และบันทึกซื้อเข้าก่อน",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    // Fruit selector
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedSummary?.let {
                                "${it.fruitType.name} (คงเหลือ: ${ThaiFormatUtils.formatQuantityWithUnit(it.currentStock, it.fruitType.defaultUnit)})"
                            } ?: "เลือกผลไม้",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("ผลไม้ที่ขาย *") },
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
                                        Column {
                                            Text(
                                                text = summary.fruitType.name,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "สต็อกคงเหลือ: ${ThaiFormatUtils.formatQuantityWithUnit(summary.currentStock, summary.fruitType.defaultUnit)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (summary.currentStock <= BigDecimal.ZERO) {
                                                    MaterialTheme.colorScheme.error
                                                } else {
                                                    MaterialTheme.colorScheme.primary
                                                }
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedSummary = summary
                                        expanded = false
                                        if (summary.fruitType.defaultSellingPrice != null) {
                                            unitPriceStr = summary.fruitType.defaultSellingPrice.toPlainString()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = {
                            quantityStr = it
                            errorMsg = null
                        },
                        label = { Text("จำนวนที่ขาย *") },
                        placeholder = { Text("เช่น 1, 2.5") },
                        suffix = { Text(unit) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = isStockInsufficient && quantity > BigDecimal.ZERO,
                        supportingText = {
                            if (isStockInsufficient && quantity > BigDecimal.ZERO) {
                                Text(
                                    text = "สต็อกไม่พอ (มีอยู่เพียง ${ThaiFormatUtils.formatQuantity(currentStock)} $unit)",
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text("สต็อกคงเหลือ: ${ThaiFormatUtils.formatQuantity(currentStock)} $unit")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick increment buttons
                    QuickNumberRow(
                        increments = listOf(0.5, 1.0, 2.0, 5.0, 10.0),
                        unit = unit,
                        onAdd = { inc ->
                            val current = try { BigDecimal(quantityStr) } catch (_: Exception) { BigDecimal.ZERO }
                            quantityStr = current.add(BigDecimal.valueOf(inc)).stripTrailingZeros().toPlainString()
                            errorMsg = null
                        },
                        onReset = { quantityStr = "" }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = unitPriceStr,
                        onValueChange = {
                            unitPriceStr = it
                            errorMsg = null
                        },
                        label = { Text("ราคาขายต่อหน่วย (฿/$unit) *") },
                        placeholder = { Text("0.00") },
                        suffix = { Text("฿") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Payment Method selector
                    Text(
                        text = "ช่องทางชำระเงิน:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = paymentMethod == "เงินสด",
                            onClick = { paymentMethod = "เงินสด" },
                            label = { Text("💵 เงินสด") }
                        )
                        FilterChip(
                            selected = paymentMethod == "พร้อมเพย์ / สแกน",
                            onClick = { paymentMethod = "พร้อมเพย์ / สแกน" },
                            label = { Text("📱 พร้อมเพย์ / สแกน") }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Total Revenue
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ยอดเงินรวมที่ได้รับ:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = ThaiFormatUtils.formatBaht(totalRevenue),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("หมายเหตุ (ถ้ามี)") },
                        placeholder = { Text("เช่น ลูกค้าประจำ, แถมถุง") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val summary = selectedSummary
                    if (summary == null) {
                        errorMsg = "กรุณาเลือกผลไม้"
                        return@Button
                    }
                    val qty = try { BigDecimal(quantityStr) } catch (_: Exception) { null }
                    if (qty == null || qty <= BigDecimal.ZERO) {
                        errorMsg = "กรุณากรอกจำนวนที่ขายมากกว่า 0"
                        return@Button
                    }
                    if (qty > currentStock) {
                        errorMsg = "สต็อกคงเหลือไม่เพียงพอ (คงเหลือ ${ThaiFormatUtils.formatQuantity(currentStock)} $unit)"
                        return@Button
                    }
                    val price = try { BigDecimal(unitPriceStr) } catch (_: Exception) { null }
                    if (price == null || price < BigDecimal.ZERO) {
                        errorMsg = "กรุณากรอกราคาขาย"
                        return@Button
                    }

                    onSave(summary.fruitType.id, qty, price, saleDate, paymentMethod, notes.trim())
                },
                enabled = activeFruitSummaries.isNotEmpty(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("บันทึกการขาย")
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
