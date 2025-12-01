package com.example.fitnessfinal

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.example.fitnessfinal.database.DatabaseHelper
import com.example.fitnessfinal.utils.SessionManager
import androidx.appcompat.app.AlertDialog

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

            // ИСПРАВЛЕННЫЙ КОД:
            val cardWorkouts = findViewById<MaterialCardView>(R.id.cardWorkouts)
            val cardProgress = findViewById<MaterialCardView>(R.id.cardProgress)
            val tvLogout = findViewById<TextView>(R.id.tvLogout)  // ← ИСПРАВЛЕНО: tvLogout вместо cardLogout
            println("✅ Views found")

            // Карточка тренировок
            cardWorkouts.setOnClickListener {
                Toast.makeText(this, "Переход к тренировкам", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, WorkoutListActivity::class.java)
                startActivity(intent)
            }

            // Карточка прогресса
            cardProgress.setOnClickListener {
                val intent = Intent(this, ProgressActivity::class.java)
                intent.putExtra("USER_ID", userId)
                startActivity(intent)
            }

            // Кнопка выхода (TextView)
            tvLogout.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Выход")
                    .setMessage("Вы уверены, что хотите выйти?")
                    .setPositiveButton("Выйти") { dialog, which ->
                        sessionManager.logoutUser()
                        Toast.makeText(this, "Вы вышли из системы", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
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