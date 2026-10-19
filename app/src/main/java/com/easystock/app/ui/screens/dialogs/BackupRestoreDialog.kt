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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.easystock.app.ui.components.ConfirmDialog

@Composable
fun BackupRestoreDialog(
    onDismiss: () -> Unit,
    onExport: suspend () -> String,
    onRestore: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var exportJson by remember { mutableStateOf("") }
    var restoreJson by remember { mutableStateOf("") }
    var isCopied by remember { mutableStateOf(false) }
    var showRestoreConfirm by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            exportJson = onExport()
        }
    }

    if (showRestoreConfirm) {
        ConfirmDialog(
            title = "ยืนยันการกู้คืนข้อมูล",
            message = "คำเตือน: การกู้คืนข้อมูลจะเขียนทับข้อมูลผลไม้ การซื้อ การขาย และสต็อกปัจจุบันทั้งหมดด้วยข้อมูลจากไฟล์สำรองนี้ คุณแน่ใจหรือไม่?",
            confirmText = "เขียนทับและกู้คืน",
            cancelText = "ยกเลิก",
            isDestructive = true,
            onConfirm = {
                showRestoreConfirm = false
                onRestore(restoreJson.trim())
                onDismiss()
            },
            onDismiss = { showRestoreConfirm = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "สำรองและกู้คืนข้อมูล (Backup & Restore)", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("สำรองข้อมูล (Export)") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("กู้คืนข้อมูล (Import)") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    Text(
                        text = "ข้อมูลทั้งหมด (ชนิดผลไม้, ยอดซื้อ, ยอดขาย, ของเสีย, สต็อก) ถูกรวบรวมเป็นรหัส JSON ด้านล่าง ท่านสามารถกดคัดลอกเพื่อเก็บไว้ได้อย่างปลอดภัย:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = exportJson,
                        onValueChange = {},
                        readOnly = true,
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(exportJson))
                            isCopied = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(if (isCopied) "คัดลอกแล้วเรียบร้อย! ✓" else "คัดลอกข้อมูลสำรอง (Copy JSON)")
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "วางรหัสข้อมูลสำรอง JSON ที่คัดลอกไว้ เพื่อกู้คืนข้อมูลทั้งหมดกลับมา:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = restoreJson,
                        onValueChange = { restoreJson = it },
                        placeholder = { Text("วางรหัส JSON ที่นี่...") },
                        maxLines = 8,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (restoreJson.isNotBlank()) {
                                showRestoreConfirm = true
                            }
                        },
                        enabled = restoreJson.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("เริ่มการกู้คืนข้อมูล")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("ปิด")
            }
        }
    )
}
