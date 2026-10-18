package com.easystock.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.easystock.app.data.model.SaleRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sale_records ORDER BY saleDate DESC, id DESC")
    fun getAll(): Flow<List<SaleRecord>>

    @Query("SELECT * FROM sale_records WHERE dateString = :dateString ORDER BY saleDate DESC, id DESC")
    fun getByDate(dateString: String): Flow<List<SaleRecord>>

    @Query("SELECT * FROM sale_records WHERE fruitTypeId = :fruitTypeId ORDER BY saleDate ASC")
    fun getByFruitTypeId(fruitTypeId: Long): Flow<List<SaleRecord>>

    @Query("SELECT * FROM sale_records WHERE fruitTypeId = :fruitTypeId ORDER BY saleDate ASC")
    suspend fun getListByFruitTypeId(fruitTypeId: Long): List<SaleRecord>

    @Query("SELECT * FROM sale_records")
    suspend fun getAllList(): List<SaleRecord>

    @Query("SELECT * FROM sale_records WHERE dateString = :dateString")
    suspend fun getListByDate(dateString: String): List<SaleRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sale: SaleRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sales: List<SaleRecord>)

    @Update
    suspend fun update(sale: SaleRecord)

    @Delete
    suspend fun delete(sale: SaleRecord)

    @Query("DELETE FROM sale_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sale_records")
    suspend fun deleteAll()
}
