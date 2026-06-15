package com.budgettracker.util

import com.budgettracker.data.dao.BadgeDao
import com.budgettracker.data.dao.ExpenseDao
import com.budgettracker.data.dao.SpendingGoalDao
import com.budgettracker.data.dao.UserDao
import com.budgettracker.data.entity.Badge

object GamificationManager {

    data class BadgeInfo(
        val type: String,
        val name: String,
        val description: String,
        val points: Int,
        val emoji: String
    )

    val ALL_BADGES = listOf(
        BadgeInfo("FIRST_LOG", "First Step!", "Logged your first expense", 10, "🌟"),
        BadgeInfo("LOG_10", "On a Roll!", "Logged 10 expenses", 25, "🎯"),
        BadgeInfo("LOG_50", "Expense Expert", "Logged 50 expenses", 75, "💪"),
        BadgeInfo("LOG_100", "Budget Legend", "Logged 100 expenses", 150, "🏆"),
        BadgeInfo("STREAK_3", "3-Day Streak", "Logged expenses 3 days in a row", 30, "🔥"),
        BadgeInfo("STREAK_7", "Week Warrior", "Logged expenses 7 days in a row", 70, "⚡"),
        BadgeInfo("BUDGET_MASTER", "Budget Master", "Stayed within budget goal this month", 100, "💰"),
        BadgeInfo("PHOTO_PRO", "Photo Pro", "Added a photo to an expense", 15, "📸"),
        BadgeInfo("CATEGORY_CREATOR", "Category Creator", "Created a custom category", 20, "🗂️"),
        BadgeInfo("GAME_WINNER", "Game Champion", "Won the budget mini-game", 50, "🎮")
    )

    suspend fun checkAndAwardBadges(
        userId: Int,
        userDao: UserDao,
        expenseDao: ExpenseDao,
        badgeDao: BadgeDao,
        spendingGoalDao: SpendingGoalDao
    ): List<BadgeInfo> {
        val newBadges = mutableListOf<BadgeInfo>()

        val expenseCount = expenseDao.getExpenseCount(userId)

        // First log
        if (expenseCount >= 1) newBadges.add(tryAwardBadge(userId, "FIRST_LOG", badgeDao, userDao))
        if (expenseCount >= 10) newBadges.add(tryAwardBadge(userId, "LOG_10", badgeDao, userDao))
        if (expenseCount >= 50) newBadges.add(tryAwardBadge(userId, "LOG_50", badgeDao, userDao))
        if (expenseCount >= 100) newBadges.add(tryAwardBadge(userId, "LOG_100", badgeDao, userDao))

        // Budget goal check
        val now = java.util.Calendar.getInstance()
        val month = now.get(java.util.Calendar.MONTH) + 1
        val year = now.get(java.util.Calendar.YEAR)
        val goal = spendingGoalDao.getGoalForMonth(userId, month, year)
        if (goal != null) {
            val (start, end) = DateUtils.getMonthStartEnd(year, month)
            val spent = expenseDao.getTotalSpent(userId, start, end) ?: 0.0
            if (spent in goal.minimumGoal..goal.maximumGoal) {
                newBadges.add(tryAwardBadge(userId, "BUDGET_MASTER", badgeDao, userDao))
            }
        }

        return newBadges.filterNotNull()
    }

    private suspend fun tryAwardBadge(
        userId: Int,
        type: String,
        badgeDao: BadgeDao,
        userDao: UserDao
    ): BadgeInfo? {
        if (badgeDao.hasBadge(userId, type) > 0) return null
        val info = ALL_BADGES.find { it.type == type } ?: return null
        badgeDao.insert(Badge(userId = userId, badgeType = type, badgeName = info.name, badgeDescription = info.description))
        userDao.addPoints(userId, info.points)
        return info
    }

    suspend fun awardBadgeDirectly(
        userId: Int,
        type: String,
        badgeDao: BadgeDao,
        userDao: UserDao
    ): BadgeInfo? = tryAwardBadge(userId, type, badgeDao, userDao)
}
