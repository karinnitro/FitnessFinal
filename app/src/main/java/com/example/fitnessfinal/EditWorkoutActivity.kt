package com.example.fitnessfinal

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnessfinal.database.DatabaseHelper
import com.example.fitnessfinal.model.Workout

class EditWorkoutActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDuration: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private var workoutId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_workout)

        databaseHelper = DatabaseHelper(this)

        // Получаем ID тренировки из Intent
        workoutId = intent.getLongExtra("WORKOUT_ID", 0)
        if (workoutId == 0L) {
            Toast.makeText(this, "Ошибка: тренировка не найдена", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        loadWorkoutData()
        setupClickListeners()
    }

    private fun initViews() {
        etTitle = findViewById(R.id.etTitle)
        etDescription = findViewById(R.id.etDescription)
        etDuration = findViewById(R.id.etDuration)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
    }

    private fun loadWorkoutData() {
        val workout = databaseHelper.getWorkoutById(workoutId)

        if (workout != null) {
            etTitle.setText(workout.title)
            etDescription.setText(workout.description)
            etDuration.setText(workout.duration.toString())
        } else {
            Toast.makeText(this, "Тренировка не найдена", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupClickListeners() {
        btnSave.setOnClickListener {
            saveWorkout()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun saveWorkout() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val durationText = etDuration.text.toString().trim()

        if (title.isEmpty() || durationText.isEmpty()) {
            Toast.makeText(this, "Заполните название и продолжительность", Toast.LENGTH_SHORT).show()
            return
        }

        val duration = try {
            durationText.toInt()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Введите число в продолжительность", Toast.LENGTH_SHORT).show()
            return
        }

        // Получаем текущую тренировку для сохранения даты и userId
        val currentWorkout = databaseHelper.getWorkoutById(workoutId)

        if (currentWorkout != null) {
            val updatedWorkout = Workout(
                id = workoutId,
                userId = currentWorkout.userId,
                title = title,
                description = description,
                duration = duration,
                date = currentWorkout.date // Сохраняем оригинальную дату
            )

            if (databaseHelper.updateWorkout(updatedWorkout)) {
                Toast.makeText(this, "Тренировка обновлена!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Ошибка обновления", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseHelper.close()
    }
}