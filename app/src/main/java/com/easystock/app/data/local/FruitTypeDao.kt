package com.easystock.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.easystock.app.data.model.FruitType
import kotlinx.coroutines.flow.Flow

@Dao
interface FruitTypeDao {
    @Query("SELECT * FROM fruit_types ORDER BY isActive DESC, name ASC")
    fun getAll(): Flow<List<FruitType>>

    @Query("SELECT * FROM fruit_types WHERE isActive = 1 ORDER BY name ASC")
    fun getActive(): Flow<List<FruitType>>

    @Query("SELECT * FROM fruit_types WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FruitType?

    @Query("SELECT * FROM fruit_types")
    suspend fun getAllList(): List<FruitType>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fruitType: FruitType): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fruitTypes: List<FruitType>)

    @Update
    suspend fun update(fruitType: FruitType)

    @Delete
    suspend fun delete(fruitType: FruitType)

    @Query("UPDATE fruit_types SET isActive = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("UPDATE fruit_types SET isActive = 1 WHERE id = :id")
    suspend fun activate(id: Long)

    @Query("SELECT COUNT(*) FROM fruit_types")
    suspend fun count(): Int

    @Query("DELETE FROM fruit_types")
    suspend fun deleteAll()
}
