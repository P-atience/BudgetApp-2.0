package com.budgettracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.budgettracker.data.entity.Expense

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC, startTime DESC")
    fun getAllExpensesForUser(userId: Int): LiveData<List<Expense>>

    @Query("""
        SELECT * FROM expenses 
        WHERE userId = :userId AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, startTime DESC
    """)
    fun getExpensesByDateRange(userId: Int, startDate: String, endDate: String): LiveData<List<Expense>>

    @Query("""
        SELECT * FROM expenses 
        WHERE userId = :userId AND date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, startTime DESC
    """)
    suspend fun getExpensesByDateRangeSync(userId: Int, startDate: String, endDate: String): List<Expense>

    @Query("""
        SELECT categoryName, SUM(amount) as total 
        FROM expenses 
        WHERE userId = :userId AND date BETWEEN :startDate AND :endDate
        GROUP BY categoryName
        ORDER BY total DESC
    """)
    fun getCategoryTotals(userId: Int, startDate: String, endDate: String): LiveData<List<CategoryTotal>>

    @Query("""
        SELECT categoryName, SUM(amount) as total 
        FROM expenses 
        WHERE userId = :userId AND date BETWEEN :startDate AND :endDate
        GROUP BY categoryName
    """)
    suspend fun getCategoryTotalsSync(userId: Int, startDate: String, endDate: String): List<CategoryTotal>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalSpent(userId: Int, startDate: String, endDate: String): Double?

    @Query("SELECT COUNT(*) FROM expenses WHERE userId = :userId")
    suspend fun getExpenseCount(userId: Int): Int

    @Query("SELECT COUNT(*) FROM expenses WHERE userId = :userId AND date = :date")
    suspend fun getExpenseCountForDate(userId: Int, date: String): Int

    @Query("""
        SELECT SUM(amount) FROM expenses 
        WHERE userId = :userId 
        AND strftime('%Y-%m', date) = :yearMonth
    """)
    suspend fun getMonthlyTotal(userId: Int, yearMonth: String): Double?
}

data class CategoryTotal(
    val categoryName: String,
    val total: Double
)
