package com.easystock.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.easystock.app.data.model.StockCountRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface StockCountDao {
    @Query("SELECT * FROM stock_count_records ORDER BY countDate DESC, id DESC")
    fun getAll(): Flow<List<StockCountRecord>>

    @Query("SELECT * FROM stock_count_records WHERE dateString = :dateString ORDER BY countDate DESC, id DESC")
    fun getByDate(dateString: String): Flow<List<StockCountRecord>>

    @Query("SELECT * FROM stock_count_records WHERE dateString = :dateString AND fruitTypeId = :fruitTypeId LIMIT 1")
    suspend fun getByDateAndFruit(dateString: String, fruitTypeId: Long): StockCountRecord?

    @Query("SELECT * FROM stock_count_records")
    suspend fun getAllList(): List<StockCountRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stockCount: StockCountRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stockCounts: List<StockCountRecord>)

    @Update
    suspend fun update(stockCount: StockCountRecord)

    @Delete
    suspend fun delete(stockCount: StockCountRecord)

    @Query("DELETE FROM stock_count_records")
    suspend fun deleteAll()
}
