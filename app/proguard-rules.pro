# Add project specific ProGuard rules here.
-keep class com.budgettracker.data.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }
