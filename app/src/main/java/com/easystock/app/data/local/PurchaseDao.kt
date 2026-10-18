package com.easystock.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.easystock.app.data.model.PurchaseRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchase_records ORDER BY purchaseDate DESC, id DESC")
    fun getAll(): Flow<List<PurchaseRecord>>

    @Query("SELECT * FROM purchase_records WHERE dateString = :dateString ORDER BY purchaseDate DESC, id DESC")
    fun getByDate(dateString: String): Flow<List<PurchaseRecord>>

    @Query("SELECT * FROM purchase_records WHERE fruitTypeId = :fruitTypeId ORDER BY purchaseDate ASC")
    fun getByFruitTypeId(fruitTypeId: Long): Flow<List<PurchaseRecord>>

    @Query("SELECT * FROM purchase_records WHERE fruitTypeId = :fruitTypeId ORDER BY purchaseDate ASC")
    suspend fun getListByFruitTypeId(fruitTypeId: Long): List<PurchaseRecord>

    @Query("SELECT * FROM purchase_records")
    suspend fun getAllList(): List<PurchaseRecord>

    @Query("SELECT * FROM purchase_records WHERE dateString = :dateString")
    suspend fun getListByDate(dateString: String): List<PurchaseRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(purchase: PurchaseRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(purchases: List<PurchaseRecord>)

    @Update
    suspend fun update(purchase: PurchaseRecord)

    @Delete
    suspend fun delete(purchase: PurchaseRecord)

    @Query("DELETE FROM purchase_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM purchase_records")
    suspend fun deleteAll()
}
