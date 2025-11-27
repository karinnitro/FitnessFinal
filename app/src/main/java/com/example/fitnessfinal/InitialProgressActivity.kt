package com.example.fitnessfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnessfinal.database.DatabaseHelper
import com.example.fitnessfinal.model.Progress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InitialProgressActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private var userId: Long = 0

    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var btnSaveProgress: Button
    private lateinit var tvSkip: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_progress)

        databaseHelper = DatabaseHelper(this)

        // Получаем ID пользователя из Intent
        userId = intent.getLongExtra("USER_ID", 0)
        if (userId == 0L) {
            Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        println("✅ InitialProgressActivity - User ID: $userId")

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etWeight = findViewById(R.id.etWeight)
        etHeight = findViewById(R.id.etHeight)
        btnSaveProgress = findViewById(R.id.btnSaveProgress)
        tvSkip = findViewById(R.id.tvSkip)
    }

    private fun setupClickListeners() {
        // Сохранение прогресса
        btnSaveProgress.setOnClickListener {
            saveProgress()
        }

        // Пропуск заполнения
        tvSkip.setOnClickListener {
            navigateToLoginActivity()
        }
    }

    private fun saveProgress() {
        val weightText = etWeight.text.toString().trim()

        // Проверка обязательного поля - вес
        if (weightText.isEmpty()) {
            etWeight.error = "Введите вес"
            return
        }

        val weight = weightText.toDoubleOrNull()
        if (weight == null || weight <= 0) {
            etWeight.error = "Введите корректный вес"
            return
        }

        // Получаем рост (не обязательный)
        val height = etHeight.text.toString().trim().toDoubleOrNull()

        // Проверка валидности роста
        if (height != null && height <= 0) {
            etHeight.error = "Введите корректный рост"
            return
        }

        // Создаем объект прогресса
        val currentDate = getCurrentDate()
        val progress = Progress(
            userId = userId,
            weight = weight,
            height = height,
            chest = null,  // Убрали обхваты
            waist = null,  // Убрали обхваты
            hips = null,   // Убрали обхваты
            date = currentDate
        )

        // Сохраняем в базу данных
        val success = databaseHelper.addProgress(progress)

        if (success) {
            Toast.makeText(this, "Данные успешно сохранены! Теперь войдите в систему", Toast.LENGTH_SHORT).show()
            navigateToLoginActivity()
        } else {
            Toast.makeText(this, "Ошибка при сохранении данных", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseHelper.close()
    }
}