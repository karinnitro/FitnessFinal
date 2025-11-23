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
    private lateinit var etChest: EditText
    private lateinit var etWaist: EditText
    private lateinit var etHips: EditText
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

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        etWeight = findViewById(R.id.etWeight)
        etHeight = findViewById(R.id.etHeight)
        etChest = findViewById(R.id.etChest)
        etWaist = findViewById(R.id.etWaist)
        etHips = findViewById(R.id.etHips)
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
            navigateToMainActivity()
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

        // Получаем остальные параметры (они не обязательные)
        val height = etHeight.text.toString().trim().toDoubleOrNull()
        val chest = etChest.text.toString().trim().toDoubleOrNull()
        val waist = etWaist.text.toString().trim().toDoubleOrNull()
        val hips = etHips.text.toString().trim().toDoubleOrNull()

        // Проверка валидности необязательных полей
        if (height != null && height <= 0) {
            etHeight.error = "Введите корректный рост"
            return
        }
        if (chest != null && chest <= 0) {
            etChest.error = "Введите корректный обхват груди"
            return
        }
        if (waist != null && waist <= 0) {
            etWaist.error = "Введите корректный обхват талии"
            return
        }
        if (hips != null && hips <= 0) {
            etHips.error = "Введите корректный обхват бедер"
            return
        }

        // Создаем объект прогресса
        val currentDate = getCurrentDate()
        val progress = Progress(
            userId = userId,
            weight = weight,
            height = height,
            chest = chest,
            waist = waist,
            hips = hips,
            date = currentDate
        )

        // Сохраняем в базу данных
        val success = databaseHelper.addProgress(progress)

        if (success) {
            Toast.makeText(this, "Данные успешно сохранены!", Toast.LENGTH_SHORT).show()
            navigateToMainActivity()
        } else {
            Toast.makeText(this, "Ошибка при сохранении данных", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_ID", userId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseHelper.close()
    }
}