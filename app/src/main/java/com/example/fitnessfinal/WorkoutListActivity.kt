package com.example.fitnessfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnessfinal.database.DatabaseHelper
import com.example.fitnessfinal.model.Workout
import com.example.fitnessfinal.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutListActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var layoutWorkouts: LinearLayout
    private lateinit var btnAddWorkout: Button
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("✅ WorkoutListActivity started")

        try {
            setContentView(R.layout.activity_workout_list)
            println("✅ Workout layout set")

            databaseHelper = DatabaseHelper(this)
            sessionManager = SessionManager(this)
            println("✅ Database and session initialized")

            layoutWorkouts = findViewById(R.id.layoutWorkouts)
            btnAddWorkout = findViewById(R.id.btnAddWorkout)
            btnBack = findViewById(R.id.btnBack)
            println("✅ Views found")

            // Кнопка назад
            btnBack.setOnClickListener {
                println("✅ Back button clicked")
                finish() // Возврат на главный экран
            }

            btnAddWorkout.setOnClickListener {
                println("✅ Add workout button clicked")
                startActivity(Intent(this, AddWorkoutActivity::class.java))
            }

            loadWorkouts()
            println("✅ Workouts loaded successfully")

        } catch (e: Exception) {
            println("❌ ERROR in WorkoutListActivity: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Ошибка загрузки тренировок", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        println("✅ WorkoutListActivity onResume")
        loadWorkouts()
    }

    private fun loadWorkouts() {
        println("✅ Loading workouts...")

        try {
            layoutWorkouts.removeAllViews()

            val userId = sessionManager.getUserId()
            println("✅ User ID: $userId")

            if (userId == -1L) {
                Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show()
                return
            }

            val workouts = databaseHelper.getWorkoutsByUserId(userId)
            println("✅ Workouts found: ${workouts.size}")

            if (workouts.isEmpty()) {
                val tvEmpty = TextView(this)
                tvEmpty.text = "Нет тренировок. Добавьте первую!"
                tvEmpty.textSize = 16f
                tvEmpty.setPadding(0, 32, 0, 32)
                layoutWorkouts.addView(tvEmpty)
                println("✅ Empty message shown")
            } else {
                workouts.forEach { workout ->
                    addWorkoutView(workout)
                }
                println("✅ Workouts displayed: ${workouts.size}")
            }
        } catch (e: Exception) {
            println("❌ ERROR loading workouts: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun addWorkoutView(workout: Workout) {
        try {
            val workoutCard = layoutInflater.inflate(R.layout.item_workout, layoutWorkouts, false)

            val tvTitle: TextView = workoutCard.findViewById(R.id.tvWorkoutTitle)
            val tvDate: TextView = workoutCard.findViewById(R.id.tvWorkoutDate)
            val tvDuration: TextView = workoutCard.findViewById(R.id.tvWorkoutDuration)
            val tvDescription: TextView = workoutCard.findViewById(R.id.tvWorkoutDescription)
            val btnEdit: Button = workoutCard.findViewById(R.id.btnEditWorkout)
            val btnDelete: Button = workoutCard.findViewById(R.id.btnDeleteWorkout)

            // Заполняем данными
            tvTitle.text = workout.title
            tvDate.text = formatDateForDisplay(workout.date)
            tvDuration.text = "${workout.duration} мин"
            tvDescription.text = workout.description.ifEmpty { "Без описания" }

            // Кнопка редактирования
            btnEdit.setOnClickListener {
                editWorkout(workout.id)
            }

            // Кнопка удаления
            btnDelete.setOnClickListener {
                deleteWorkout(workout.id)
            }

            layoutWorkouts.addView(workoutCard)

            println("✅ Workout card added: ${workout.title}")
        } catch (e: Exception) {
            println("❌ ERROR adding workout view: ${e.message}")
        }
    }

    // Метод для форматирования даты
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

    // Метод для редактирования тренировки
    private fun editWorkout(workoutId: Long) {
        val intent = Intent(this, EditWorkoutActivity::class.java)
        intent.putExtra("WORKOUT_ID", workoutId)
        startActivity(intent)
    }

    private fun deleteWorkout(workoutId: Long) {
        println("✅ Deleting workout: $workoutId")

        try {
            if (databaseHelper.deleteWorkout(workoutId)) {
                Toast.makeText(this, "Тренировка удалена", Toast.LENGTH_SHORT).show()
                loadWorkouts()
                println("✅ Workout deleted successfully")
            } else {
                Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show()
                println("❌ Failed to delete workout")
            }
        } catch (e: Exception) {
            println("❌ ERROR deleting workout: ${e.message}")
            e.printStackTrace()
        }
    }
}