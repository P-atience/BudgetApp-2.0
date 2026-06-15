package com.budgettracker.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.budgettracker.data.entity.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE isAdmin = 0")
    fun getAllRegularUsers(): LiveData<List<User>>

    @Query("UPDATE users SET totalPoints = totalPoints + :points WHERE id = :userId")
    suspend fun addPoints(userId: Int, points: Int)

    @Query("UPDATE users SET currentStreak = :streak, lastLoginDate = :date WHERE id = :userId")
    suspend fun updateStreak(userId: Int, streak: Int, date: String)
}
