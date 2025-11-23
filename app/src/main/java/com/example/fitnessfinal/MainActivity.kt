package com.example.fitnessfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnessfinal.database.DatabaseHelper
import com.example.fitnessfinal.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var databaseHelper: DatabaseHelper
    private var userId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("✅ MainActivity onCreate started")

        try {
            sessionManager = SessionManager(this)
            databaseHelper = DatabaseHelper(this)
            println("✅ SessionManager created")

            // Получаем ID пользователя из SessionManager
            userId = sessionManager.getUserId()
            println("✅ User ID from SessionManager: $userId")

            if (userId == -1L) {
                // Если пользователь не авторизован, переходим на экран входа
                Toast.makeText(this, "Пожалуйста, войдите в систему", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return
            }

            setContentView(R.layout.activity_main)
            println("✅ Layout set")

            // Инициализируем все кнопки
            val textView: TextView = findViewById(R.id.textView)
            val btnWorkouts: Button = findViewById(R.id.btnWorkouts)
            val btnProgress: Button = findViewById(R.id.btnProgress)
            val btnLogout: Button = findViewById(R.id.btnLogout)
            println("✅ Views found")

            textView.text = "Добро пожаловать в Фитнес Трекер!"
            println("✅ Text set")

            // Кнопка тренировок - ДОБАВЬТЕ ПРОВЕРКУ
            btnWorkouts.setOnClickListener {
                println("✅ Workouts button clicked")
                Toast.makeText(this, "Переход к тренировкам", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, WorkoutListActivity::class.java)
                startActivity(intent)
            }

            // Кнопка прогресса - ДОБАВЬТЕ ПРОВЕРКУ
            btnProgress.setOnClickListener {
                println("✅ Progress button clicked, userId: $userId")
                val intent = Intent(this, ProgressActivity::class.java)
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
            }

            // Кнопка выхода
            btnLogout.setOnClickListener {
                println("✅ Logout button clicked")
                sessionManager.logoutUser()
                Toast.makeText(this, "Вы вышли из системы", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

            println("✅ MainActivity created successfully")

        } catch (e: Exception) {
            println("❌ ERROR in MainActivity: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        println("✅ MainActivity onResume")
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseHelper.close()
    }
}