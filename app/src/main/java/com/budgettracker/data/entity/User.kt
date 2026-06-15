package com.budgettracker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String,
    val isAdmin: Boolean = false,
    val totalPoints: Int = 0,
    val currentStreak: Int = 0,
    val lastLoginDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
