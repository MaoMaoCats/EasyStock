package com.easystock.app.ui.screens.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.easystock.app.data.model.FruitStockSummary
import com.easystock.app.data.model.FruitType
import com.easystock.app.ui.components.QuickNumberRow
import com.easystock.app.util.SampleData
import com.easystock.app.util.ThaiFormatUtils
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WasteDialog(
    availableFruits: List<FruitStockSummary>,
    preselectedFruit: FruitType? = null,
    onDismiss: () -> Unit,
    onSave: (fruitTypeId: Long, quantity: BigDecimal, reason: String, wasteDate: Long, notes: String) -> Unit
) {
    var selectedSummary by remember {
        mutableStateOf(
            availableFruits.find { it.fruitType.id == preselectedFruit?.id }
                ?: availableFruits.firstOrNull()
        )
    }

    var expanded by remember { mutableStateOf(false) }
    var quantityStr by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf(SampleData.commonWasteReasons.first()) }
    var customReason by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var wasteDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    val currentStock = selectedSummary?.currentStock ?: BigDecimal.ZERO
    val unit = selectedSummary?.fruitType?.defaultUnit ?: "กก."

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "บันทึกผลไม้คัดทิ้ง / ของเสีย", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "รายการของเสียจะช่วยลดจำนวนสต็อกให้ถูกต้อง และบันทึกต้นทุนความสูญเสียในรายงานประจำวัน",
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
                        value = selectedSummary?.let {
                            "${it.fruitType.name} (สต็อก: ${ThaiFormatUtils.formatQuantityWithUnit(it.currentStock, it.fruitType.defaultUnit)})"
                        } ?: "เลือกผลไม้",
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
                        availableFruits.forEach { summary ->
                            DropdownMenuItem(
                                text = {
                                    Text("${summary.fruitType.name} (คงเหลือ ${ThaiFormatUtils.formatQuantityWithUnit(summary.currentStock, summary.fruitType.defaultUnit)})")
                                },
                                onClick = {
                                    selectedSummary = summary
                                    expanded = false
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
                    label = { Text("จำนวนที่คัดทิ้ง / เสียหาย *") },
                    placeholder = { Text("เช่น 1, 2.5") },
                    suffix = { Text(unit) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    supportingText = {
                        Text("สต็อกปัจจุบัน: ${ThaiFormatUtils.formatQuantity(currentStock)} $unit")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                QuickNumberRow(
                    increments = listOf(0.5, 1.0, 2.0, 5.0),
                    unit = unit,
                    onAdd = { inc ->
                        val current = try { BigDecimal(quantityStr) } catch (_: Exception) { BigDecimal.ZERO }
                        quantityStr = current.add(BigDecimal.valueOf(inc)).stripTrailingZeros().toPlainString()
                        errorMsg = null
                    },
                    onReset = { quantityStr = "" }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "สาเหตุที่คัดทิ้ง *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SampleData.commonWasteReasons.forEach { reason ->
                        FilterChip(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            label = { Text(reason) }
                        )
                    }
                }

                if (selectedReason == "อื่นๆ") {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        label = { Text("ระบุสาเหตุอื่นๆ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("หมายเหตุเพิ่มเติม (ถ้ามี)") },
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
                        errorMsg = "กรุณาเลือกผลไม้"
                        return@Button
                    }
                    val qty = try { BigDecimal(quantityStr) } catch (_: Exception) { null }
                    if (qty == null || qty <= BigDecimal.ZERO) {
                        errorMsg = "กรุณากรอกจำนวนมากกว่า 0"
                        return@Button
                    }
                    if (qty > currentStock) {
                        errorMsg = "จำนวนของเสียมากกว่าสต็อกที่มี (${ThaiFormatUtils.formatQuantity(currentStock)} $unit)"
                        return@Button
                    }

                    val finalReason = if (selectedReason == "อื่นๆ" && customReason.isNotBlank()) {
                        customReason.trim()
                    } else {
                        selectedReason
                    }

                    onSave(summary.fruitType.id, qty, finalReason, wasteDate, notes.trim())
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("บันทึกของเสีย")
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
