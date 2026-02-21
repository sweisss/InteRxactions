package com.example.interxactions.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "searched_drug")
data class SearchedDrug(
    @PrimaryKey val id: String,
    val drugName: String,
    val drugType: String = "BRAND_NAME",
    val timestamp: Long
) : Serializable