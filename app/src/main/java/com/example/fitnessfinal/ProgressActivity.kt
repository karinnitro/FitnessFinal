package com.example.fitnessfinal

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.fitnessfinal.database.DatabaseHelper
import com.example.fitnessfinal.utils.SessionManager  // Добавьте этот импорт
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProgressActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager  // Объявите SessionManager
    private var userId: Long = 0

    private lateinit var tvCurrentWeight: TextView
    private lateinit var tvCurrentHeight: TextView
    private lateinit var btnUpdateWeight: Button
    private lateinit var llProgressHistory: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("✅ ProgressActivity onCreate started")

        try {
            setContentView(R.layout.activity_progress)
            println("✅ Layout set successfully")

            databaseHelper = DatabaseHelper(this)
            sessionManager = SessionManager(this)  // Инициализируйте SessionManager

            // Получаем ID пользователя из Intent или из SessionManager
            userId = intent.getLongExtra("USER_ID", 0)
            if (userId == 0L) {
                // Если не получили из Intent, пробуем из SessionManager
                userId = sessionManager.getUserId()
                println("✅ User ID from SessionManager: $userId")
            }

            if (userId == 0L || userId == -1L) {
                Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            initViews()
            loadProgressData()
            setupClickListeners()

            println("✅ ProgressActivity created successfully")

        } catch (e: Exception) {
            println("❌ ERROR in ProgressActivity: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Ошибка загрузки: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initViews() {
        println("✅ Initializing views")
        try {
            tvCurrentWeight = findViewById(R.id.tvCurrentWeight)
            tvCurrentHeight = findViewById(R.id.tvCurrentHeight)
            btnUpdateWeight = findViewById(R.id.btnUpdateWeight)
            llProgressHistory = findViewById(R.id.llProgressHistory)
            println("✅ Views initialized successfully")
        } catch (e: Exception) {
            println("❌ ERROR initializing views: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private fun loadProgressData() {
        println("✅ Loading progress data")
        try {
            // Сначала проверим все записи в базе для отладки
            val allProgress = databaseHelper.debugGetAllProgress(userId)
            println("✅ All progress records: ${allProgress.size}")

            // Загружаем последние данные
            val latestProgress = databaseHelper.getLatestProgress(userId)
            println("✅ Latest progress: $latestProgress")

            if (latestProgress != null) {
                tvCurrentWeight.text = String.format("%.1f кг", latestProgress.weight)
                tvCurrentHeight.text = if (latestProgress.height != null)
                    String.format("%.1f см", latestProgress.height) else "не указан"
            } else {
                tvCurrentWeight.text = "нет данных"
                tvCurrentHeight.text = "не указан"
            }

            // Загружаем историю
            loadProgressHistory()
            println("✅ Progress data loaded successfully")

        } catch (e: Exception) {
            println("❌ ERROR loading progress data: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProgressHistory() {
        println("✅ Loading progress history")
        try {
            val progressList = databaseHelper.getAllUserProgress(userId)
            println("✅ Progress list size: ${progressList.size}")

            llProgressHistory.removeAllViews()

            if (progressList.isEmpty()) {
                val emptyView = TextView(this).apply {
                    text = "Нет данных о прогрессе"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(this@ProgressActivity, android.R.color.darker_gray))
                    setPadding(0, 16, 0, 16)
                }
                llProgressHistory.addView(emptyView)
                return
            }

            progressList.reversed().forEach { progress ->
                // Создаем элемент истории программно
                val historyItem = TextView(this).apply {
                    text = String.format("%s - %.1f кг", formatDateForDisplay(progress.date), progress.weight)
                    textSize = 14f
                    setPadding(32, 16, 32, 16)
                    background = ContextCompat.getDrawable(this@ProgressActivity, android.R.color.transparent)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 0, 0, 8)
                    }
                }

                llProgressHistory.addView(historyItem)
            }
            println("✅ Progress history loaded successfully")

        } catch (e: Exception) {
            println("❌ ERROR loading progress history: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupClickListeners() {
        println("✅ Setting up click listeners")
        btnUpdateWeight.setOnClickListener {
            println("✅ Update weight button clicked")
            showUpdateWeightDialog()
        }
    }

    private fun showUpdateWeightDialog() {
        println("✅ Showing update weight dialog")
        try {
            // Создаем диалог программно вместо использования layout
            val input = EditText(this).apply {
                hint = "Вес (кг)"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            }

            AlertDialog.Builder(this)
                .setTitle("Обновить вес")
                .setView(input)
                .setPositiveButton("Сохранить") { dialog, which ->
                    val weightText = input.text.toString().trim()
                    println("✅ Weight entered: $weightText")

                    if (weightText.isEmpty()) {
                        Toast.makeText(this, "Введите вес", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val weight = weightText.toDoubleOrNull()
                    if (weight == null || weight <= 0) {
                        Toast.makeText(this, "Введите корректный вес", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    println("✅ Saving weight: $weight for user: $userId")

                    // Сохраняем только вес, рост берем из предыдущих записей
                    val success = databaseHelper.updateWeightOnly(userId, weight)
                    println("✅ Save result: $success")

                    if (success) {
                        Toast.makeText(this, "Вес обновлен!", Toast.LENGTH_SHORT).show()
                        loadProgressData()
                    } else {
                        Toast.makeText(this, "Ошибка при сохранении", Toast.LENGTH_SHORT).show()
                        println("❌ Failed to save weight")
                    }
                }
                .setNegativeButton("Отмена", null)
                .show()

        } catch (e: Exception) {
            println("❌ ERROR showing dialog: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Ошибка открытия диалога", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatDateForDisplay(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseHelper.close()
        println("✅ ProgressActivity destroyed")
    }
}