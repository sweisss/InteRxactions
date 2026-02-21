package com.example.interxactions.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [SearchedDrug::class], version = 3)
abstract class SearchedDrugDatabase : RoomDatabase() {
    abstract fun searchedDrugDao(): SearchedDrugDAO

    companion object {
        const val DATABASE_NAME = "searched-drugs-db"

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Check if table exists before altering
                val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='searched_drug'")
                if (cursor.moveToFirst()) {
                    db.execSQL("ALTER TABLE searched_drug ADD COLUMN drugType TEXT NOT NULL DEFAULT 'BRAND_NAME'")
                } else {
                    // If table doesn't exist, create it (if this is a fresh installation)
                    db.execSQL("""
                CREATE TABLE IF NOT EXISTS searched_drug (
                    id TEXT PRIMARY KEY NOT NULL,
                    drugName TEXT NOT NULL,
                    drugType TEXT NOT NULL DEFAULT 'BRAND_NAME',
                    timestamp INTEGER NOT NULL
                )
            """.trimIndent())
                }
                cursor.close()
            }
        }

        @Volatile private var instance: SearchedDrugDatabase? = null

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context,
                SearchedDrugDatabase::class.java,
                DATABASE_NAME
            ).addMigrations(MIGRATION_2_3).build()

        fun getInstance(context: Context): SearchedDrugDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }
        }
    }
}