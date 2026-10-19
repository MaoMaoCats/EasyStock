package com.easystock.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easystock.app.data.model.FruitStockSummary
import com.easystock.app.data.model.FruitType
import com.easystock.app.ui.screens.FruitTypesScreen
import com.easystock.app.ui.screens.HomeScreen
import com.easystock.app.ui.screens.PurchasesScreen
import com.easystock.app.ui.screens.ReportsScreen
import com.easystock.app.ui.screens.SalesScreen
import com.easystock.app.ui.screens.dialogs.BackupRestoreDialog
import com.easystock.app.ui.screens.dialogs.FruitTypeDialog
import com.easystock.app.ui.screens.dialogs.PurchaseDialog
import com.easystock.app.ui.screens.dialogs.SaleDialog
import com.easystock.app.ui.screens.dialogs.StockCountDialog
import com.easystock.app.ui.screens.dialogs.WasteDialog
import com.easystock.app.ui.viewmodel.EasyStockViewModel
import com.easystock.app.ui.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest

enum class MainTab(val title: String, val icon: ImageVector) {
    HOME("หน้าหลัก", Icons.Default.Home),
    FRUIT_TYPES("ชนิดผลไม้", Icons.Default.Spa),
    PURCHASES("ซื้อเข้า", Icons.Default.AddShoppingCart),
    SALES("ขาย", Icons.Default.PointOfSale),
    REPORTS("รายงาน", Icons.Default.Assessment)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: EasyStockViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog states
    var showAddFruitTypeDialog by remember { mutableStateOf(false) }
    var editingFruitType by remember { mutableStateOf<FruitType?>(null) }

    var showPurchaseDialog by remember { mutableStateOf(false) }
    var preselectedFruitForPurchase by remember { mutableStateOf<FruitType?>(null) }

    var showSaleDialog by remember { mutableStateOf(false) }
    var preselectedFruitForSale by remember { mutableStateOf<FruitType?>(null) }

    var showStockCountDialog by remember { mutableStateOf(false) }
    var preselectedFruitForStockCount by remember { mutableStateOf<FruitStockSummary?>(null) }

    var showWasteDialog by remember { mutableStateOf(false) }
    var preselectedFruitForWaste by remember { mutableStateOf<FruitType?>(null) }

    var showBackupRestoreDialog by remember { mutableStateOf(false) }

    // State collections
    val allFruitTypes by viewModel.allFruitTypes.collectAsStateWithLifecycle()
    val activeFruitTypes by viewModel.activeFruitTypes.collectAsStateWithLifecycle()
    val stockSummaries by viewModel.stockSummaries.collectAsStateWithLifecycle()
    val allPurchases by viewModel.allPurchases.collectAsStateWithLifecycle()
    val filteredPurchases by viewModel.filteredPurchases.collectAsStateWithLifecycle()
    val allSales by viewModel.allSales.collectAsStateWithLifecycle()
    val filteredSales by viewModel.filteredSales.collectAsStateWithLifecycle()
    val allWaste by viewModel.allWaste.collectAsStateWithLifecycle()
    val filteredWaste by viewModel.filteredWaste.collectAsStateWithLifecycle()
    val filteredStockCounts by viewModel.filteredStockCounts.collectAsStateWithLifecycle()
    val todaySummary by viewModel.todaySummary.collectAsStateWithLifecycle()
    val dailySummary by viewModel.dailySummary.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()

    // Handle snackbar events
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    // Dialog Renderings
    if (showAddFruitTypeDialog || editingFruitType != null) {
        FruitTypeDialog(
            initialFruit = editingFruitType,
            onDismiss = {
                showAddFruitTypeDialog = false
                editingFruitType = null
            },
            onSave = { id, name, unit, cost, sell ->
                viewModel.saveFruitType(id, name, unit, cost, sell)
                showAddFruitTypeDialog = false
                editingFruitType = null
            }
        )
    }

    if (showPurchaseDialog) {
        PurchaseDialog(
            activeFruits = activeFruitTypes,
            preselectedFruit = preselectedFruitForPurchase,
            onDismiss = {
                showPurchaseDialog = false
                preselectedFruitForPurchase = null
            },
            onSave = { fruitId, qty, cost, date, notes ->
                viewModel.recordPurchase(fruitId, qty, cost, date, notes) {
                    showPurchaseDialog = false
                    preselectedFruitForPurchase = null
                }
            }
        )
    }

    if (showSaleDialog) {
        SaleDialog(
            availableFruits = stockSummaries,
            preselectedFruit = preselectedFruitForSale,
            onDismiss = {
                showSaleDialog = false
                preselectedFruitForSale = null
            },
            onSave = { fruitId, qty, price, date, payment, notes ->
                viewModel.recordSale(fruitId, qty, price, date, payment, notes) {
                    showSaleDialog = false
                    preselectedFruitForSale = null
                }
            }
        )
    }

    if (showStockCountDialog) {
        StockCountDialog(
            availableFruits = stockSummaries,
            initialFruitSummary = preselectedFruitForStockCount,
            onDismiss = {
                showStockCountDialog = false
                preselectedFruitForStockCount = null
            },
            onSave = { fruitId, actualQty, recordWaste, wasteReason, notes ->
                viewModel.recordStockCount(fruitId, actualQty, recordWaste, wasteReason, notes) {
                    showStockCountDialog = false
                    preselectedFruitForStockCount = null
                }
            }
        )
    }

    if (showWasteDialog) {
        WasteDialog(
            availableFruits = stockSummaries,
            preselectedFruit = preselectedFruitForWaste,
            onDismiss = {
                showWasteDialog = false
                preselectedFruitForWaste = null
            },
            onSave = { fruitId, qty, reason, date, notes ->
                viewModel.recordWaste(fruitId, qty, reason, date, notes) {
                    showWasteDialog = false
                    preselectedFruitForWaste = null
                }
            }
        )
    }

    if (showBackupRestoreDialog) {
        BackupRestoreDialog(
            onDismiss = { showBackupRestoreDialog = false },
            onExport = { viewModel.exportBackupJson() },
            onRestore = { json ->
                viewModel.restoreBackup(json) {
                    showBackupRestoreDialog = false
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            MainTab.HOME -> "EasyStock • สรุปยอดร้านวันนี้"
                            MainTab.FRUIT_TYPES -> "จัดการชนิดผลไม้"
                            MainTab.PURCHASES -> "บันทึกซื้อผลไม้เข้า"
                            MainTab.SALES -> "บันทึกการขายผลไม้"
                            MainTab.REPORTS -> "รายงานและตรวจนับสต็อก"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.HOME -> HomeScreen(
                    todaySummary = todaySummary,
                    stockSummaries = stockSummaries,
                    allFruits = allFruitTypes,
                    onQuickSale = { fruit ->
                        preselectedFruitForSale = fruit
                        showSaleDialog = true
                    },
                    onQuickPurchase = { fruit ->
                        preselectedFruitForPurchase = fruit
                        showPurchaseDialog = true
                    },
                    onOpenSales = {
                        preselectedFruitForSale = null
                        showSaleDialog = true
                    },
                    onOpenPurchases = {
                        preselectedFruitForPurchase = null
                        showPurchaseDialog = true
                    },
                    onOpenStockCount = {
                        preselectedFruitForStockCount = null
                        showStockCountDialog = true
                    },
                    onOpenFruitTypes = {
                        currentTab = MainTab.FRUIT_TYPES
                        showAddFruitTypeDialog = true
                    },
                    onSeedFruits = {
                        viewModel.seedDefaultFruits()
                    }
                )

                MainTab.FRUIT_TYPES -> FruitTypesScreen(
                    allFruits = allFruitTypes,
                    stockSummaries = stockSummaries,
                    onAddFruitType = {
                        editingFruitType = null
                        showAddFruitTypeDialog = true
                    },
                    onEditFruitType = { fruit ->
                        editingFruitType = fruit
                    },
                    onToggleActive = { fruit ->
                        viewModel.toggleFruitTypeActive(fruit)
                    },
                    onDeleteFruitType = { fruit ->
                        viewModel.deleteOrDeactivateFruitType(fruit)
                    },
                    onSeedFruits = {
                        viewModel.seedDefaultFruits()
                    }
                )

                MainTab.PURCHASES -> PurchasesScreen(
                    purchases = filteredPurchases,
                    allPurchases = allPurchases,
                    fruits = allFruitTypes,
                    selectedDate = selectedDate,
                    onAddPurchase = {
                        preselectedFruitForPurchase = null
                        showPurchaseDialog = true
                    },
                    onDeletePurchase = { id ->
                        viewModel.deletePurchase(id)
                    }
                )

                MainTab.SALES -> SalesScreen(
                    sales = filteredSales,
                    allSales = allSales,
                    fruits = allFruitTypes,
                    selectedDate = selectedDate,
                    onAddSale = {
                        preselectedFruitForSale = null
                        showSaleDialog = true
                    },
                    onDeleteSale = { id ->
                        viewModel.deleteSale(id)
                    }
                )

                MainTab.REPORTS -> ReportsScreen(
                    dailySummary = dailySummary,
                    selectedDate = selectedDate,
                    onSelectDate = { viewModel.setSelectedDate(it) },
                    stockSummaries = stockSummaries,
                    stockCounts = filteredStockCounts,
                    wasteRecords = filteredWaste,
                    fruits = allFruitTypes,
                    onOpenStockCountForFruit = { summary ->
                        preselectedFruitForStockCount = summary
                        showStockCountDialog = true
                    },
                    onOpenGeneralStockCount = {
                        preselectedFruitForStockCount = null
                        showStockCountDialog = true
                    },
                    onOpenRecordWaste = {
                        preselectedFruitForWaste = null
                        showWasteDialog = true
                    },
                    onDeleteWaste = { id ->
                        viewModel.deleteWaste(id)
                    },
                    onOpenBackupRestore = {
                        showBackupRestoreDialog = true
                    }
                )
            }
        }
    }
}
