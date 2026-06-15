# 💰 Budget Tracker — Android App

A full-featured personal budget tracking app built with **Kotlin** and **Android Studio**, using Room (SQLite), MVVM architecture, MPAndroidChart, and gamification.

---

## 🚀 Quick Setup in Android Studio

### Requirements
- **Android Studio Hedgehog** (2023.1.1) or newer
- **JDK 11+**
- **Android SDK** (API 26+)
- Internet connection (first build downloads Gradle dependencies)

### Steps

1. **Extract** the ZIP into a folder
2. **Open Android Studio** → `File > Open` → select the `BudgetTracker` folder
3. Wait for **Gradle sync** to complete (downloads all dependencies automatically)
4. Connect a device or start an **emulator** (API 26+)
5. Click **▶ Run**

---

## 🔑 Default Login Credentials

| Username | Password | Role  |
|----------|----------|-------|
| `admin`  | `admin123` | Admin |
| `demo`   | `demo123`  | User  |

Or **Register** a new account from the login screen.

---

## 📱 Features

### User Features
- **Login / Register** — secure username & password authentication
- **Dashboard** — monthly spending overview, goal progress bar, points & badges
- **Expenses** — add/edit/delete entries with date, time range, description, category, optional photo
- **Categories** — preset list + preset chips + "Other" custom entry
- **Goals** — set monthly min/max spending goals
- **Charts** — Bar chart (by category) + Pie chart; limit lines show goals; selectable date range
- **Photos** — camera or gallery, stored locally, visible from expense list
- **Gamification** — earn badges and points for milestones
- **Budget Quiz Game** — 10 True/False questions; score ≥70% = Game Champion badge

### Admin Features
- **Admin Dashboard** — see all registered users and their stats
- **User Detail** — view spending, goal status, badge list
- **Award Badge** — manually award Budget Master badge to any user

---

## 🏅 Badges

| Badge | Trigger |
|-------|---------|
| 🌟 First Step! | Log first expense |
| 🎯 On a Roll! | 10 total expenses |
| 💪 Expense Expert | 50 expenses |
| 🏆 Budget Legend | 100 expenses |
| 🔥 3-Day Streak | 3 consecutive days |
| ⚡ Week Warrior | 7 consecutive days |
| 💰 Budget Master | Stay within goal this month |
| 📸 Photo Pro | Add a photo to an expense |
| 🗂️ Category Creator | Create a custom category |
| 🎮 Game Champion | Score ≥70% in the quiz game |

---

## 🗂️ Project Structure

```
app/src/main/java/com/budgettracker/
├── data/
│   ├── dao/          # Room DAOs (UserDao, ExpenseDao, CategoryDao, etc.)
│   ├── database/     # AppDatabase (Room) + seeding
│   └── entity/       # Data classes (User, Expense, Category, SpendingGoal, Badge)
├── ui/
│   ├── auth/         # LoginActivity, RegisterActivity
│   ├── dashboard/    # MainActivity, DashboardFragment
│   ├── expenses/     # ExpensesFragment, AddEditExpenseActivity, ExpenseAdapter
│   ├── categories/   # CategoriesFragment, CategoryAdapter
│   ├── goals/        # GoalsFragment
│   ├── graphs/       # GraphsFragment (MPAndroidChart)
│   ├── gamification/ # GameActivity (quiz), GameFragment, BadgeAdapter
│   └── admin/        # AdminDashboardActivity, UserDetailActivity, AdminUserAdapter
└── util/
    ├── SessionManager.kt      # SharedPreferences session
    ├── DateUtils.kt           # Date formatting helpers
    └── GamificationManager.kt # Badge & points logic
```

---

## 📦 Key Dependencies

- **Room** — local SQLite database
- **MPAndroidChart** (JitPack) — bar & pie charts
- **Glide** — image loading
- **Material Components** — UI widgets
- **Coroutines** — async database operations
- **ViewBinding** — type-safe view access

---

## 🔧 Troubleshooting

- **Gradle sync fails?** Check internet connection and ensure JDK 11+ is configured in `File > Project Structure > SDK Location`
- **MPAndroidChart not found?** Ensure `jitpack.io` is in `settings.gradle` repositories
- **Camera crashes?** The app requests permissions at runtime; allow camera access when prompted
- **Build error on Room?** Make sure KSP plugin version matches Kotlin version in `build.gradle`
