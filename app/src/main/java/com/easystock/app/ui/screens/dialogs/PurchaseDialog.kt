package com.easystock.app.ui.screens.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.easystock.app.data.model.FruitType
import com.easystock.app.ui.components.QuickNumberRow
import com.easystock.app.util.ThaiFormatUtils
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseDialog(
    activeFruits: List<FruitType>,
    preselectedFruit: FruitType? = null,
    onDismiss: () -> Unit,
    onSave: (fruitTypeId: Long, quantity: BigDecimal, unitCost: BigDecimal, purchaseDate: Long, notes: String) -> Unit
) {
    var selectedFruit by remember {
        mutableStateOf(preselectedFruit ?: activeFruits.firstOrNull())
    }
    var expanded by remember { mutableStateOf(false) }

    var quantityStr by remember { mutableStateOf("") }
    var unitCostStr by remember {
        mutableStateOf(selectedFruit?.defaultCostPrice?.toPlainString() ?: "")
    }
    var purchaseDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var notes by remember { mutableStateOf("") }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Live total cost
    val totalCost: BigDecimal = remember(quantityStr, unitCostStr) {
        try {
            val q = BigDecimal(quantityStr)
            val c = BigDecimal(unitCostStr)
            if (q > BigDecimal.ZERO && c >= BigDecimal.ZERO) {
                q.multiply(c).setScale(2, RoundingMode.HALF_UP)
            } else BigDecimal.ZERO
        } catch (_: Exception) {
            BigDecimal.ZERO
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "บันทึกการซื้อผลไม้เข้าสต็อก", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (activeFruits.isEmpty()) {
                    Text(
                        text = "ไม่มีชนิดผลไม้ที่เปิดใช้งาน กรุณาเพิ่มหรือเปิดใช้งานชนิดผลไม้ก่อน",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    // Fruit Dropdown Selector
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedFruit?.name ?: "เลือกผลไม้",
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
                            activeFruits.forEach { fruit ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${fruit.name} (หน่วย: ${fruit.defaultUnit})")
                                    },
                                    onClick = {
                                        selectedFruit = fruit
                                        expanded = false
                                        if (unitCostStr.isBlank() && fruit.defaultCostPrice != null) {
                                            unitCostStr = fruit.defaultCostPrice.toPlainString()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val unit = selectedFruit?.defaultUnit ?: "กก."
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = {
                            quantityStr = it
                            errorMsg = null
                        },
                        label = { Text("จำนวนที่ซื้อเข้า *") },
                        placeholder = { Text("เช่น 10, 25.5") },
                        suffix = { Text(unit) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quick buttons for quantity
                    QuickNumberRow(
                        increments = listOf(1.0, 5.0, 10.0, 20.0, 50.0),
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
                        value = unitCostStr,
                        onValueChange = {
                            unitCostStr = it
                            errorMsg = null
                        },
                        label = { Text("ราคาต้นทุนต่อหน่วยจริง (฿/$unit) *") },
                        placeholder = { Text("0.00") },
                        suffix = { Text("฿") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Total display card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ยอดเงินรวมที่ต้องจ่าย:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = ThaiFormatUtils.formatBaht(totalCost),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("หมายเหตุ / แหล่งที่ซื้อ (ถ้ามี)") },
                        placeholder = { Text("เช่น ตลาดไท, สวนลุงสม, เกรด A") },
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
                    val fruit = selectedFruit
                    if (fruit == null) {
                        errorMsg = "กรุณาเลือกชนิดผลไม้"
                        return@Button
                    }
                    val qty = try { BigDecimal(quantityStr) } catch (_: Exception) { null }
                    if (qty == null || qty <= BigDecimal.ZERO) {
                        errorMsg = "กรุณากรอกจำนวนที่ซื้อมากกว่า 0"
                        return@Button
                    }
                    val cost = try { BigDecimal(unitCostStr) } catch (_: Exception) { null }
                    if (cost == null || cost < BigDecimal.ZERO) {
                        errorMsg = "กรุณากรอกราคาต้นทุนต่อหน่วย"
                        return@Button
                    }

                    onSave(fruit.id, qty, cost, purchaseDate, notes.trim())
                },
                enabled = activeFruits.isNotEmpty(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("บันทึกซื้อเข้า")
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
