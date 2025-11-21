package com.example.fitnessfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnessfinal.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("✅ MainActivity onCreate started")

        try {
            sessionManager = SessionManager(this)
            println("✅ SessionManager created")

            setContentView(R.layout.activity_main)
            println("✅ Layout set")

            val textView: TextView = findViewById(R.id.textView)
            val button: Button = findViewById(R.id.button)
            val btnWorkouts: Button = findViewById(R.id.btnWorkouts)
            println("✅ Views found")

            // Временно уберем проверку логина
            textView.text = "Добро пожаловать в Фитнес Трекер!"
            println("✅ Text set")

            // Кнопка тренировок - простой переход
            btnWorkouts.setOnClickListener {
                Toast.makeText(this, "Переход к тренировкам", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, WorkoutListActivity::class.java))
            }

            // Выход
            button.setOnClickListener {
                println("✅ Logout button clicked")
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }

            println("✅ MainActivity created successfully")

        } catch (e: Exception) {
            println("❌ ERROR in MainActivity: ${e.message}")
            e.printStackTrace()
        }
    }
}