package com.easystock.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.easystock.app.data.model.FruitType
import com.easystock.app.data.model.PurchaseRecord
import com.easystock.app.data.model.SaleRecord
import com.easystock.app.data.model.StockCountRecord
import com.easystock.app.data.model.WasteRecord

@Database(
    entities = [
        FruitType::class,
        PurchaseRecord::class,
        SaleRecord::class,
        WasteRecord::class,
        StockCountRecord::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EasyStockDatabase : RoomDatabase() {
    abstract fun fruitTypeDao(): FruitTypeDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun saleDao(): SaleDao
    abstract fun wasteDao(): WasteDao
    abstract fun stockCountDao(): StockCountDao

    companion object {
        @Volatile
        private var INSTANCE: EasyStockDatabase? = null

        fun getDatabase(context: Context): EasyStockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EasyStockDatabase::class.java,
                    "easystock.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
