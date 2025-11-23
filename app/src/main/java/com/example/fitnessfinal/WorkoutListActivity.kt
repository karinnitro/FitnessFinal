package com.example.fitnessfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnessfinal.database.DatabaseHelper
import com.example.fitnessfinal.model.Workout
import com.example.fitnessfinal.utils.SessionManager

class WorkoutListActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var layoutWorkouts: LinearLayout
    private lateinit var btnAddWorkout: Button

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
            println("✅ Views found")

            btnAddWorkout.setOnClickListener {
                println("✅ Add workout button clicked")
                val intent = Intent(this, AddWorkoutActivity::class.java)
                startActivity(intent)
            }

            loadWorkouts()
            println("✅ Workouts loaded successfully")

        } catch (e: Exception) {
            println("❌ ERROR in WorkoutListActivity: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "Ошибка загрузки тренировок: ${e.message}", Toast.LENGTH_LONG).show()
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
            val workoutLayout = LinearLayout(this)
            workoutLayout.orientation = LinearLayout.VERTICAL
            workoutLayout.setPadding(0, 16, 0, 16)

            val tvTitle = TextView(this)
            tvTitle.text = workout.title
            tvTitle.textSize = 18f
            tvTitle.setPadding(16, 8, 16, 4)

            val tvDetails = TextView(this)
            tvDetails.text = "${workout.duration} мин • ${workout.date}"
            tvDetails.textSize = 14f
            tvDetails.setPadding(16, 4, 16, 8)

            val tvDescription = TextView(this)
            tvDescription.text = workout.description
            tvDescription.textSize = 12f
            tvDescription.setPadding(16, 4, 16, 16)

            val btnDelete = Button(this)
            btnDelete.text = "Удалить"
            btnDelete.setPadding(16, 8, 16, 8)
            btnDelete.setOnClickListener {
                deleteWorkout(workout.id)
            }

            workoutLayout.addView(tvTitle)
            workoutLayout.addView(tvDetails)
            workoutLayout.addView(tvDescription)
            workoutLayout.addView(btnDelete)

            // Добавляем разделитель
            val divider = TextView(this)
            divider.text = "―".repeat(50)
            divider.textSize = 12f
            divider.setPadding(0, 8, 0, 8)

            layoutWorkouts.addView(workoutLayout)
            layoutWorkouts.addView(divider)

            println("✅ Workout view added: ${workout.title}")
        } catch (e: Exception) {
            println("❌ ERROR adding workout view: ${e.message}")
        }
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