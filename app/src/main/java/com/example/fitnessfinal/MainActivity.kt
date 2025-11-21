package com.example.fitnessfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnessfinal.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

        val textView: TextView = findViewById(R.id.textView)
        val button: Button = findViewById(R.id.button)

        // Показываем имя пользователя
        val userDetails = sessionManager.getUserDetails()
        val userName = userDetails[SessionManager.KEY_USER_NAME]
        textView.text = if (userName.isNullOrEmpty()) {
            "Добро пожаловать в Фитнес Трекер!"
        } else {
            "Добро пожаловать, $userName!"
        }

        // Выход из системы
        button.setOnClickListener {
            sessionManager.logoutUser()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}