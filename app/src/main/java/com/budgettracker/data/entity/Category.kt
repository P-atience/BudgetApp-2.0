package com.budgettracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val iconName: String = "ic_category",
    val colorHex: String = "#6200EE",
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
