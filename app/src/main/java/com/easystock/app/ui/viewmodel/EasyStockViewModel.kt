package com.easystock.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.easystock.app.data.model.DailySummary
import com.easystock.app.data.model.FruitStockSummary
import com.easystock.app.data.model.FruitType
import com.easystock.app.data.model.PurchaseRecord
import com.easystock.app.data.model.SaleRecord
import com.easystock.app.data.model.StockCountRecord
import com.easystock.app.data.model.WasteRecord
import com.easystock.app.data.repository.EasyStockRepository
import com.easystock.app.util.ThaiFormatUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class EasyStockViewModel(
    private val repository: EasyStockRepository
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    // -------------------------------------------------------------------------
    // Selected Date & Filters
    // -------------------------------------------------------------------------
    private val _selectedDate = MutableStateFlow(ThaiFormatUtils.todayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun setSelectedDate(dateString: String) {
        _selectedDate.value = dateString
    }

    fun setToday() {
        _selectedDate.value = ThaiFormatUtils.todayDateString()
    }

    // -------------------------------------------------------------------------
    // Persistent Data Flows
    // -------------------------------------------------------------------------
    val allFruitTypes: StateFlow<List<FruitType>> = repository.getAllFruitTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeFruitTypes: StateFlow<List<FruitType>> = repository.getActiveFruitTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockSummaries: StateFlow<List<FruitStockSummary>> = repository.fruitStockSummaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPurchases: StateFlow<List<PurchaseRecord>> = repository.getAllPurchases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPurchases: StateFlow<List<PurchaseRecord>> = _selectedDate
        .flatMapLatest { date -> repository.getPurchasesByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSales: StateFlow<List<SaleRecord>> = repository.getAllSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSales: StateFlow<List<SaleRecord>> = _selectedDate
        .flatMapLatest { date -> repository.getSalesByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWaste: StateFlow<List<WasteRecord>> = repository.getAllWaste()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredWaste: StateFlow<List<WasteRecord>> = _selectedDate
        .flatMapLatest { date -> repository.getWasteByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredStockCounts: StateFlow<List<StockCountRecord>> = _selectedDate
        .flatMapLatest { date -> repository.getStockCountsByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailySummary: StateFlow<DailySummary> = _selectedDate
        .flatMapLatest { date -> repository.getDailySummary(date) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DailySummary(dateString = ThaiFormatUtils.todayDateString())
        )

    val todaySummary: StateFlow<DailySummary> = repository.getDailySummary(ThaiFormatUtils.todayDateString())
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DailySummary(dateString = ThaiFormatUtils.todayDateString())
        )

    // -------------------------------------------------------------------------
    // Fruit Types Actions
    // -------------------------------------------------------------------------

    fun saveFruitType(
        id: Long,
        name: String,
        defaultUnit: String,
        defaultCostPrice: BigDecimal?,
        defaultSellingPrice: BigDecimal?
    ) {
        viewModelScope.launch {
            if (name.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar("กรุณากรอกชื่อชนิดผลไม้"))
                return@launch
            }
            if (defaultUnit.isBlank()) {
                _eventFlow.emit(UiEvent.ShowSnackbar("กรุณาระบุหน่วยนับ"))
                return@launch
            }

            if (id == 0L) {
                val fruit = FruitType(
                    name = name.trim(),
                    defaultUnit = defaultUnit.trim(),
                    defaultCostPrice = defaultCostPrice,
                    defaultSellingPrice = defaultSellingPrice,
                    isActive = true
                )
                repository.addFruitType(fruit)
                _eventFlow.emit(UiEvent.ShowSnackbar("เพิ่มชนิดผลไม้ \"${fruit.name}\" เรียบร้อยแล้ว"))
            } else {
                val existing = repository.getFruitTypeById(id)
                if (existing != null) {
                    val updated = existing.copy(
                        name = name.trim(),
                        defaultUnit = defaultUnit.trim(),
                        defaultCostPrice = defaultCostPrice,
                        defaultSellingPrice = defaultSellingPrice
                    )
                    repository.updateFruitType(updated)
                    _eventFlow.emit(UiEvent.ShowSnackbar("แก้ไขชนิดผลไม้เรียบร้อยแล้ว"))
                }
            }
        }
    }

    fun toggleFruitTypeActive(fruitType: FruitType) {
        viewModelScope.launch {
            if (fruitType.isActive) {
                repository.deactivateFruitType(fruitType.id)
                _eventFlow.emit(UiEvent.ShowSnackbar("ปิดการใช้งาน \"${fruitType.name}\" แล้ว (ประวัติยังคงอยู่)"))
            } else {
                repository.activateFruitType(fruitType.id)
                _eventFlow.emit(UiEvent.ShowSnackbar("เปิดใช้งาน \"${fruitType.name}\" แล้ว"))
            }
        }
    }

    fun deleteOrDeactivateFruitType(fruitType: FruitType) {
        viewModelScope.launch {
            repository.deleteFruitType(fruitType)
            _eventFlow.emit(UiEvent.ShowSnackbar("จัดการชนิดผลไม้เรียบร้อยแล้ว"))
        }
    }

    fun seedDefaultFruits() {
        viewModelScope.launch {
            repository.seedSampleFruitTypes()
            _eventFlow.emit(UiEvent.ShowSnackbar("เพิ่มผลไม้ยอดนิยมเริ่มต้นเรียบร้อยแล้ว"))
        }
    }

    // -------------------------------------------------------------------------
    // Purchases Actions
    // -------------------------------------------------------------------------

    fun recordPurchase(
        fruitTypeId: Long,
        quantity: BigDecimal,
        unitCost: BigDecimal,
        purchaseDate: Long,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.addPurchase(
                fruitTypeId = fruitTypeId,
                quantity = quantity,
                unitCost = unitCost,
                purchaseDate = purchaseDate,
                notes = notes
            )
            result.onSuccess {
                _eventFlow.emit(UiEvent.ShowSnackbar("บันทึกการซื้อเข้าสำเร็จ (+${ThaiFormatUtils.formatQuantity(quantity)} หน่วย)"))
                onSuccess()
            }.onFailure { e ->
                _eventFlow.emit(UiEvent.ShowSnackbar(e.message ?: "เกิดข้อผิดพลาดในการบันทึก"))
            }
        }
    }

    fun deletePurchase(id: Long) {
        viewModelScope.launch {
            repository.deletePurchase(id)
            _eventFlow.emit(UiEvent.ShowSnackbar("ลบรายการซื้อเข้าแล้ว"))
        }
    }

    // -------------------------------------------------------------------------
    // Sales Actions
    // -------------------------------------------------------------------------

    fun recordSale(
        fruitTypeId: Long,
        quantity: BigDecimal,
        unitPrice: BigDecimal,
        saleDate: Long,
        paymentMethod: String,
        notes: String,
        allowNegativeStock: Boolean = false,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.recordSale(
                fruitTypeId = fruitTypeId,
                quantity = quantity,
                unitPrice = unitPrice,
                saleDate = saleDate,
                paymentMethod = paymentMethod,
                notes = notes,
                allowNegativeStock = allowNegativeStock
            )
            result.onSuccess {
                _eventFlow.emit(UiEvent.ShowSnackbar("บันทึกการขายสำเร็จ ยอดขาย ${ThaiFormatUtils.formatBaht(quantity.multiply(unitPrice))}"))
                onSuccess()
            }.onFailure { e ->
                _eventFlow.emit(UiEvent.ShowSnackbar(e.message ?: "เกิดข้อผิดพลาดในการขาย"))
            }
        }
    }

    fun deleteSale(id: Long) {
        viewModelScope.launch {
            repository.deleteSale(id)
            _eventFlow.emit(UiEvent.ShowSnackbar("ยกเลิกรายการขายเรียบร้อยแล้ว"))
        }
    }

    // -------------------------------------------------------------------------
    // Waste Actions
    // -------------------------------------------------------------------------

    fun recordWaste(
        fruitTypeId: Long,
        quantity: BigDecimal,
        reason: String,
        wasteDate: Long,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.recordWaste(
                fruitTypeId = fruitTypeId,
                quantity = quantity,
                reason = reason,
                wasteDate = wasteDate,
                notes = notes,
                allowNegativeStock = false
            )
            result.onSuccess {
                _eventFlow.emit(UiEvent.ShowSnackbar("บันทึกของเสียเรียบร้อยแล้ว"))
                onSuccess()
            }.onFailure { e ->
                _eventFlow.emit(UiEvent.ShowSnackbar(e.message ?: "เกิดข้อผิดพลาด"))
            }
        }
    }

    fun deleteWaste(id: Long) {
        viewModelScope.launch {
            repository.deleteWaste(id)
            _eventFlow.emit(UiEvent.ShowSnackbar("ลบรายการของเสียแล้ว"))
        }
    }

    // -------------------------------------------------------------------------
    // Daily Stock Count Actions
    // -------------------------------------------------------------------------

    fun recordStockCount(
        fruitTypeId: Long,
        actualQty: BigDecimal,
        recordWasteForShortage: Boolean,
        wasteReason: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.recordStockCount(
                fruitTypeId = fruitTypeId,
                actualQty = actualQty,
                dateString = _selectedDate.value,
                recordWasteForShortage = recordWasteForShortage,
                wasteReason = wasteReason,
                notes = notes
            )
            result.onSuccess {
                _eventFlow.emit(UiEvent.ShowSnackbar("บันทึกผลการนับสต็อกสำเร็จ"))
                onSuccess()
            }.onFailure { e ->
                _eventFlow.emit(UiEvent.ShowSnackbar(e.message ?: "เกิดข้อผิดพลาดในการนับสต็อก"))
            }
        }
    }

    // -------------------------------------------------------------------------
    // Backup & Restore
    // -------------------------------------------------------------------------

    suspend fun exportBackupJson(): String {
        return repository.exportBackupJson()
    }

    fun restoreBackup(jsonString: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.restoreBackupJson(jsonString)
            result.onSuccess {
                _eventFlow.emit(UiEvent.ShowSnackbar("กู้คืนข้อมูลสำเร็จ"))
                onSuccess()
            }.onFailure { e ->
                _eventFlow.emit(UiEvent.ShowSnackbar("การกู้คืนล้มเหลว: ${e.message}"))
            }
        }
    }

    class Factory(private val repository: EasyStockRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EasyStockViewModel::class.java)) {
                return EasyStockViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
