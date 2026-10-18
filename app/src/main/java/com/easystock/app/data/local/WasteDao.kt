package com.easystock.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.easystock.app.data.model.WasteRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WasteDao {
    @Query("SELECT * FROM waste_records ORDER BY wasteDate DESC, id DESC")
    fun getAll(): Flow<List<WasteRecord>>

    @Query("SELECT * FROM waste_records WHERE dateString = :dateString ORDER BY wasteDate DESC, id DESC")
    fun getByDate(dateString: String): Flow<List<WasteRecord>>

    @Query("SELECT * FROM waste_records WHERE fruitTypeId = :fruitTypeId ORDER BY wasteDate ASC")
    fun getByFruitTypeId(fruitTypeId: Long): Flow<List<WasteRecord>>

    @Query("SELECT * FROM waste_records WHERE fruitTypeId = :fruitTypeId ORDER BY wasteDate ASC")
    suspend fun getListByFruitTypeId(fruitTypeId: Long): List<WasteRecord>

    @Query("SELECT * FROM waste_records")
    suspend fun getAllList(): List<WasteRecord>

    @Query("SELECT * FROM waste_records WHERE dateString = :dateString")
    suspend fun getListByDate(dateString: String): List<WasteRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(waste: WasteRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(wastes: List<WasteRecord>)

    @Update
    suspend fun update(waste: WasteRecord)

    @Delete
    suspend fun delete(waste: WasteRecord)

    @Query("DELETE FROM waste_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM waste_records")
    suspend fun deleteAll()
}
