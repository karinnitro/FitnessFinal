package com.example.fitnessfinal

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnessfinal.database.DatabaseHelper
import com.example.fitnessfinal.model.Workout
import com.example.fitnessfinal.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddWorkoutActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDuration: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("✅ AddWorkoutActivity started")

        try {
            setContentView(R.layout.activity_add_workout)
            println("✅ Add workout layout set")

            databaseHelper = DatabaseHelper(this)
            sessionManager = SessionManager(this)
            println("✅ Database and session initialized")

            etTitle = findViewById(R.id.etTitle)
            etDescription = findViewById(R.id.etDescription)
            etDuration = findViewById(R.id.etDuration)
            btnSave = findViewById(R.id.btnSave)
            println("✅ Views found")

            btnSave.setOnClickListener {
                saveWorkout()
            }

            println("✅ AddWorkoutActivity created successfully")

        } catch (e: Exception) {
            println("❌ ERROR in AddWorkoutActivity: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun saveWorkout() {
        println("✅ Saving workout...")

        try {
            val title = etTitle.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val durationText = etDuration.text.toString().trim()

            println("✅ Title: $title, Description: $description, Duration: $durationText")

            if (title.isEmpty() || durationText.isEmpty()) {
                Toast.makeText(this, "Заполните название и продолжительность", Toast.LENGTH_SHORT).show()
                println("❌ Validation failed: empty fields")
                return
            }

            val duration = try {
                durationText.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Введите число в продолжительность", Toast.LENGTH_SHORT).show()
                println("❌ Validation failed: duration not a number")
                return
            }

            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            println("✅ Date: $date")

            val workout = Workout(
                userId = sessionManager.getUserId(),
                title = title,
                description = description,
                duration = duration,
                date = date
            )

            println("✅ Workout object created: $workout")

            if (databaseHelper.addWorkout(workout)) {
                Toast.makeText(this, "Тренировка сохранена!", Toast.LENGTH_SHORT).show()
                println("✅ Workout saved successfully")
                finish()
            } else {
                Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show()
                println("❌ Failed to save workout")
            }
        } catch (e: Exception) {
            println("❌ ERROR saving workout: ${e.message}")
            e.printStackTrace()
        }
    }
}