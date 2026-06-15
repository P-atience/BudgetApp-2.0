package com.budgettracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.budgettracker.data.dao.*
import com.budgettracker.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, Category::class, Expense::class, SpendingGoal::class, Badge::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun spendingGoalDao(): SpendingGoalDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_tracker_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed admin user and default categories
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.let { database ->
                                    seedDatabase(database)
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDatabase(db: AppDatabase) {
            // Create admin user
            val adminId = db.userDao().insert(
                User(
                    username = "admin",
                    password = "admin123",
                    isAdmin = true
                )
            ).toInt()

            // Create a demo user
            val userId = db.userDao().insert(
                User(
                    username = "demo",
                    password = "demo123",
                    isAdmin = false
                )
            ).toInt()

            // Seed default categories for demo user
            val defaultCategories = listOf(
                Category(userId = userId, name = "Food & Dining", iconName = "ic_food", colorHex = "#FF6B6B"),
                Category(userId = userId, name = "Transport", iconName = "ic_transport", colorHex = "#4ECDC4"),
                Category(userId = userId, name = "Entertainment", iconName = "ic_entertainment", colorHex = "#45B7D1"),
                Category(userId = userId, name = "Shopping", iconName = "ic_shopping", colorHex = "#96CEB4"),
                Category(userId = userId, name = "Health", iconName = "ic_health", colorHex = "#FFEAA7"),
                Category(userId = userId, name = "Utilities", iconName = "ic_utilities", colorHex = "#DDA0DD"),
                Category(userId = userId, name = "Education", iconName = "ic_education", colorHex = "#98D8C8"),
                Category(userId = userId, name = "Other", iconName = "ic_other", colorHex = "#B0BEC5")
            )
            defaultCategories.forEach { db.categoryDao().insert(it) }
        }
    }
}
