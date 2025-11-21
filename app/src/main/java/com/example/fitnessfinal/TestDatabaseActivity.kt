package com.example.fitnessfinal

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnessfinal.database.DatabaseHelper
import com.example.fitnessfinal.model.Workout

import java.io.File
import java.io.IOException

class TestDatabaseActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Инициализация базы данных
        dbHelper = DatabaseHelper(this)

        // Принудительно создаем базу
        dbHelper.initializeDatabase()

        // Тестируем базу данных
        testDatabase()
    }

    private fun testDatabase() {
        // Генерируем уникальный email каждый раз
        val timestamp = System.currentTimeMillis()
        val testEmail = "test$timestamp@example.com"

        Log.d("DatabaseTest", "Testing with email: $testEmail")

        // 1. Регистрируем тестового пользователя
        val userRegistered = dbHelper.registerUser(
            testEmail,
            "password123",
            "Test User"
        )

        Log.d("DatabaseTest", "User registered: $userRegistered")

        // 2. Пробуем авторизоваться
        val user = dbHelper.loginUser(testEmail, "password123")
        if (user != null) {
            Log.d("DatabaseTest", "User logged in: ${user.name}")

            // 3. Добавляем тестовую тренировку
            val testWorkout = Workout(
                userId = user.id,
                title = "Первая тренировка",
                description = "Тестовая тренировка",
                duration = 60,
                date = "2024-01-15"
            )

            val workoutAdded = dbHelper.addWorkout(testWorkout)
            Log.d("DatabaseTest", "Workout added: $workoutAdded")

            // 4. Получаем тренировки пользователя
            val workouts = dbHelper.getWorkoutsByUserId(user.id)
            Log.d("DatabaseTest", "Workouts found: ${workouts.size}")

        } else {
            Log.e("DatabaseTest", "Failed to login user")
        }
    }

    private fun checkDatabaseLocation() {
        val db = dbHelper.writableDatabase
        val path = db.path
        Log.d("DatabaseLocation", "📁 Database path: $path")

        val file = File(path)
        if (file.exists()) {
            Log.d("DatabaseLocation", "✅ Database EXISTS: ${file.length()} bytes")

            // Показать расположение в понятном виде
            val appContext = applicationContext
            val databaseFile = appContext.getDatabasePath("FitnessApp.db")
            Log.d("DatabaseLocation", "📂 Full path: ${databaseFile.absolutePath}")
            Log.d("DatabaseLocation", "📂 Can read: ${databaseFile.canRead()}")
            Log.d("DatabaseLocation", "📂 Can write: ${databaseFile.canWrite()}")
        } else {
            Log.e("DatabaseLocation", "❌ Database NOT FOUND at: $path")
        }
        db.close()
    }
}