package com.budgettracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.budgettracker.data.entity.Badge

@Dao
interface BadgeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(badge: Badge): Long

    @Query("SELECT * FROM badges WHERE userId = :userId ORDER BY earnedAt DESC")
    fun getBadgesForUser(userId: Int): LiveData<List<Badge>>

    @Query("SELECT * FROM badges WHERE userId = :userId ORDER BY earnedAt DESC")
    suspend fun getBadgesForUserSync(userId: Int): List<Badge>

    @Query("SELECT COUNT(*) FROM badges WHERE userId = :userId AND badgeType = :type")
    suspend fun hasBadge(userId: Int, type: String): Int

    @Query("SELECT * FROM badges ORDER BY earnedAt DESC")
    fun getAllBadges(): LiveData<List<Badge>>
}
