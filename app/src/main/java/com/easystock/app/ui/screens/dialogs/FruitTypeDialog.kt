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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.easystock.app.data.model.FruitType
import com.easystock.app.util.SampleData
import java.math.BigDecimal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FruitTypeDialog(
    initialFruit: FruitType? = null,
    onDismiss: () -> Unit,
    onSave: (id: Long, name: String, defaultUnit: String, defaultCost: BigDecimal?, defaultSell: BigDecimal?) -> Unit
) {
    var name by remember { mutableStateOf(initialFruit?.name ?: "") }
    var unit by remember { mutableStateOf(initialFruit?.defaultUnit ?: "กก.") }
    var costPriceStr by remember {
        mutableStateOf(initialFruit?.defaultCostPrice?.toPlainString() ?: "")
    }
    var sellPriceStr by remember {
        mutableStateOf(initialFruit?.defaultSellingPrice?.toPlainString() ?: "")
    }
    var nameError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialFruit == null) "เพิ่มชนิดผลไม้" else "แก้ไขชนิดผลไม้",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "ชนิดผลไม้เป็นแม่แบบข้อมูล จะยังไม่สร้างสต็อกสินค้าจนกว่าจะมีการบันทึกซื้อเข้า",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    label = { Text("ชื่อผลไม้ *") },
                    placeholder = { Text("เช่น ทุเรียนหมอนทอง, มะม่วงน้ำดอกไม้") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "หน่วยนับหลัก *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SampleData.commonUnits.forEach { u ->
                        FilterChip(
                            selected = unit == u,
                            onClick = { unit = u },
                            label = { Text(u) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("ระบุหน่วยนับเอง") },
                    placeholder = { Text("เช่น กก., หวี, ผล") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = costPriceStr,
                        onValueChange = { costPriceStr = it },
                        label = { Text("ราคาต้นทุนแนะนำ") },
                        placeholder = { Text("0.00") },
                        suffix = { Text("฿") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = sellPriceStr,
                        onValueChange = { sellPriceStr = it },
                        label = { Text("ราคาขายแนะนำ") },
                        placeholder = { Text("0.00") },
                        suffix = { Text("฿") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = "กรุณากรอกชื่อชนิดผลไม้"
                        return@Button
                    }
                    val cost = costPriceStr.trim().takeIf { it.isNotEmpty() }?.let {
                        try { BigDecimal(it) } catch (_: Exception) { null }
                    }
                    val sell = sellPriceStr.trim().takeIf { it.isNotEmpty() }?.let {
                        try { BigDecimal(it) } catch (_: Exception) { null }
                    }
                    onSave(initialFruit?.id ?: 0L, name.trim(), unit.trim(), cost, sell)
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("บันทึก")
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
