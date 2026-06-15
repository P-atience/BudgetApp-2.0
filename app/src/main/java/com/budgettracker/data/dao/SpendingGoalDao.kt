package com.budgettracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.budgettracker.data.entity.SpendingGoal

@Dao
interface SpendingGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SpendingGoal): Long

    @Update
    suspend fun update(goal: SpendingGoal)

    @Query("SELECT * FROM spending_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    suspend fun getGoalForMonth(userId: Int, month: Int, year: Int): SpendingGoal?

    @Query("SELECT * FROM spending_goals WHERE userId = :userId ORDER BY year DESC, month DESC LIMIT 1")
    fun getLatestGoal(userId: Int): LiveData<SpendingGoal?>

    @Query("SELECT * FROM spending_goals WHERE userId = :userId ORDER BY year DESC, month DESC LIMIT 1")
    suspend fun getLatestGoalSync(userId: Int): SpendingGoal?

    @Query("SELECT * FROM spending_goals WHERE userId = :userId ORDER BY year DESC, month DESC")
    fun getAllGoals(userId: Int): LiveData<List<SpendingGoal>>
}
