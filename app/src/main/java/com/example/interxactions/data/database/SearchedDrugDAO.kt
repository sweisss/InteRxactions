package com.example.interxactions.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchedDrugDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(drug: SearchedDrug)

    @Delete
    suspend fun delete(drug: SearchedDrug)

    @Query("DELETE FROM searched_drug WHERE drugName = :name")
    suspend fun deleteDrugByName(name: String)

    @Query("DELETE FROM searched_drug WHERE id = :id")
    suspend fun deleteDrugById(id: String)

    @Query("SELECT * FROM searched_drug ORDER BY timestamp DESC")
    fun getAllSearchedDrugs(): Flow<List<SearchedDrug>>

    @Query("SELECT * FROM searched_drug ORDER BY timestamp DESC LIMIT 1")
    fun getMostRecentSearchedDrug(): Flow<List<SearchedDrug>>
}