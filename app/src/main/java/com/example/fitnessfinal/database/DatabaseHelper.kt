package com.example.fitnessfinal.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.fitnessfinal.model.User
import com.example.fitnessfinal.model.Workout
import com.example.fitnessfinal.model.Progress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FitnessApp.db"
        private const val DATABASE_VERSION = 1

        // Таблица пользователей
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_EMAIL = "email"
        private const val COLUMN_USER_PASSWORD = "password"
        private const val COLUMN_USER_NAME = "name"

        // Таблица тренировок
        private const val TABLE_WORKOUTS = "workouts"
        private const val COLUMN_WORKOUT_ID = "id"
        private const val COLUMN_WORKOUT_USER_ID = "user_id"
        private const val COLUMN_WORKOUT_TITLE = "title"
        private const val COLUMN_WORKOUT_DESCRIPTION = "description"
        private const val COLUMN_WORKOUT_DURATION = "duration"
        private const val COLUMN_WORKOUT_DATE = "date"

        // Таблица прогресса
        private const val TABLE_PROGRESS = "progress"
        private const val COLUMN_PROGRESS_ID = "id"
        private const val COLUMN_PROGRESS_USER_ID = "user_id"
        private const val COLUMN_PROGRESS_WEIGHT = "weight"
        private const val COLUMN_PROGRESS_HEIGHT = "height"
        private const val COLUMN_PROGRESS_CHEST = "chest"
        private const val COLUMN_PROGRESS_WAIST = "waist"
        private const val COLUMN_PROGRESS_HIPS = "hips"
        private const val COLUMN_PROGRESS_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Создание таблицы пользователей
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_USER_PASSWORD TEXT NOT NULL,
                $COLUMN_USER_NAME TEXT NOT NULL
            )
        """.trimIndent()

        // Создание таблицы тренировок
        val createWorkoutsTable = """
            CREATE TABLE $TABLE_WORKOUTS (
                $COLUMN_WORKOUT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_WORKOUT_USER_ID INTEGER NOT NULL,
                $COLUMN_WORKOUT_TITLE TEXT NOT NULL,
                $COLUMN_WORKOUT_DESCRIPTION TEXT,
                $COLUMN_WORKOUT_DURATION INTEGER NOT NULL,
                $COLUMN_WORKOUT_DATE TEXT NOT NULL,
                FOREIGN KEY ($COLUMN_WORKOUT_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        // Создание таблицы прогресса
        val createProgressTable = """
            CREATE TABLE $TABLE_PROGRESS (
                $COLUMN_PROGRESS_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_PROGRESS_USER_ID INTEGER NOT NULL,
                $COLUMN_PROGRESS_WEIGHT REAL NOT NULL,
                $COLUMN_PROGRESS_HEIGHT REAL,
                $COLUMN_PROGRESS_CHEST REAL,
                $COLUMN_PROGRESS_WAIST REAL,
                $COLUMN_PROGRESS_HIPS REAL,
                $COLUMN_PROGRESS_DATE TEXT NOT NULL,
                FOREIGN KEY ($COLUMN_PROGRESS_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createWorkoutsTable)
        db.execSQL(createProgressTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROGRESS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORKOUTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (!db.isReadOnly) {
            db.execSQL("PRAGMA foreign_keys=ON;")
        }
    }

    // Регистрация пользователя
    fun registerUser(email: String, password: String, name: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_EMAIL, email)
            put(COLUMN_USER_PASSWORD, password)
            put(COLUMN_USER_NAME, name)
        }

        return try {
            db.insert(TABLE_USERS, null, values) != -1L
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    // Авторизация пользователя
    fun loginUser(email: String, password: String): User? {
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_USERS 
            WHERE $COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(email, password))

        return try {
            if (cursor.moveToFirst()) {
                User(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                    password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME))
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            cursor.close()
            db.close()
        }
    }

    // Проверка существования email
    fun isEmailExists(email: String): Boolean {
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_USERS 
            WHERE $COLUMN_USER_EMAIL = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(email))
        return try {
            cursor.count > 0
        } finally {
            cursor.close()
            db.close()
        }
    }

    // Добавление тренировки
    fun addWorkout(workout: Workout): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_WORKOUT_USER_ID, workout.userId)
            put(COLUMN_WORKOUT_TITLE, workout.title)
            put(COLUMN_WORKOUT_DESCRIPTION, workout.description)
            put(COLUMN_WORKOUT_DURATION, workout.duration)
            put(COLUMN_WORKOUT_DATE, workout.date)
        }

        return try {
            db.insert(TABLE_WORKOUTS, null, values) != -1L
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    // Получение всех тренировок пользователя
    fun getWorkoutsByUserId(userId: Long): List<Workout> {
        val workouts = mutableListOf<Workout>()
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_WORKOUTS 
            WHERE $COLUMN_WORKOUT_USER_ID = ?
            ORDER BY $COLUMN_WORKOUT_DATE DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        try {
            while (cursor.moveToNext()) {
                val workout = Workout(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_ID)),
                    userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_USER_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_TITLE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_DESCRIPTION)),
                    duration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_DURATION)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_DATE))
                )
                workouts.add(workout)
            }
        } catch (e: Exception) {
            // Логирование ошибки
            println("Error getting workouts: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }
        return workouts
    }

    // Обновление тренировки
    fun updateWorkout(workout: Workout): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_WORKOUT_TITLE, workout.title)
            put(COLUMN_WORKOUT_DESCRIPTION, workout.description)
            put(COLUMN_WORKOUT_DURATION, workout.duration)
            put(COLUMN_WORKOUT_DATE, workout.date)
        }

        return try {
            db.update(
                TABLE_WORKOUTS,
                values,
                "$COLUMN_WORKOUT_ID = ?",
                arrayOf(workout.id.toString())
            ) > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    // Удаление тренировки
    fun deleteWorkout(workoutId: Long): Boolean {
        val db = writableDatabase
        return try {
            db.delete(
                TABLE_WORKOUTS,
                "$COLUMN_WORKOUT_ID = ?",
                arrayOf(workoutId.toString())
            ) > 0
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    // Получение тренировки по ID
    fun getWorkoutById(workoutId: Long): Workout? {
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_WORKOUTS 
            WHERE $COLUMN_WORKOUT_ID = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(workoutId.toString()))

        return try {
            if (cursor.moveToFirst()) {
                Workout(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_ID)),
                    userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_USER_ID)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_TITLE)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_DESCRIPTION)),
                    duration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_DURATION)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORKOUT_DATE))
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            cursor.close()
            db.close()
        }
    }

    // Метод для добавления прогресса
    fun addProgress(progress: Progress): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PROGRESS_USER_ID, progress.userId)
            put(COLUMN_PROGRESS_WEIGHT, progress.weight)
            progress.height?.let { put(COLUMN_PROGRESS_HEIGHT, it) }
            progress.chest?.let { put(COLUMN_PROGRESS_CHEST, it) }
            progress.waist?.let { put(COLUMN_PROGRESS_WAIST, it) }
            progress.hips?.let { put(COLUMN_PROGRESS_HIPS, it) }
            put(COLUMN_PROGRESS_DATE, progress.date)
        }

        return try {
            db.insert(TABLE_PROGRESS, null, values) != -1L
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    // Получение последнего прогресса пользователя
    fun getLatestProgress(userId: Long): Progress? {
        println("DatabaseHelper: getLatestProgress for userId: $userId")
        val db = readableDatabase
        val query = """
        SELECT * FROM $TABLE_PROGRESS 
        WHERE $COLUMN_PROGRESS_USER_ID = ? 
        ORDER BY $COLUMN_PROGRESS_DATE DESC 
        LIMIT 1
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        println("DatabaseHelper: Cursor count: ${cursor.count}")

        return try {
            if (cursor.moveToFirst()) {
                Progress(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_ID)),
                    userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_USER_ID)),
                    weight = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_WEIGHT)),
                    height = if (!cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_HEIGHT)))
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_HEIGHT)) else null,
                    chest = if (!cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_CHEST)))
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_CHEST)) else null,
                    waist = if (!cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_WAIST)))
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_WAIST)) else null,
                    hips = if (!cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_HIPS)))
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_HIPS)) else null,
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_DATE))
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            cursor.close()
            db.close()
        }
    }

    // Получение всей истории прогресса для графиков
    fun getAllUserProgress(userId: Long): List<Progress> {
        val progressList = mutableListOf<Progress>()
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_PROGRESS 
            WHERE $COLUMN_PROGRESS_USER_ID = ? 
            ORDER BY $COLUMN_PROGRESS_DATE ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        try {
            while (cursor.moveToNext()) {
                val progress = Progress(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_ID)),
                    userId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_USER_ID)),
                    weight = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_WEIGHT)),
                    height = if (!cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_HEIGHT)))
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_HEIGHT)) else null,
                    chest = if (!cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_CHEST)))
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_CHEST)) else null,
                    waist = if (!cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_WAIST)))
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_WAIST)) else null,
                    hips = if (!cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_HIPS)))
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_HIPS)) else null,
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS_DATE))
                )
                progressList.add(progress)
            }
        } catch (e: Exception) {
            // Логирование ошибки
            println("Error getting progress: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }
        return progressList
    }

    // Проверка есть ли у пользователя данные прогресса
    fun hasUserProgress(userId: Long): Boolean {
        val db = readableDatabase
        val query = """
            SELECT COUNT(*) FROM $TABLE_PROGRESS 
            WHERE $COLUMN_PROGRESS_USER_ID = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        return try {
            cursor.moveToFirst() && cursor.getInt(0) > 0
        } finally {
            cursor.close()
            db.close()
        }
    }

    // Получение роста пользователя (берем из первой записи)
    fun getUserHeight(userId: Long): Double? {
        val db = readableDatabase
        val query = """
            SELECT $COLUMN_PROGRESS_HEIGHT FROM $TABLE_PROGRESS 
            WHERE $COLUMN_PROGRESS_USER_ID = ? 
            AND $COLUMN_PROGRESS_HEIGHT IS NOT NULL
            ORDER BY $COLUMN_PROGRESS_DATE ASC 
            LIMIT 1
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        return try {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                cursor.getDouble(0)
            } else {
                null
            }
        } finally {
            cursor.close()
            db.close()
        }
    }

    // Обновление только веса (со старым ростом)
    fun updateWeightOnly(userId: Long, weight: Double): Boolean {
        val db = writableDatabase
        val height = getUserHeight(userId) // Получаем существующий рост

        val values = ContentValues().apply {
            put(COLUMN_PROGRESS_USER_ID, userId)
            put(COLUMN_PROGRESS_WEIGHT, weight)
            height?.let { put(COLUMN_PROGRESS_HEIGHT, it) }
            put(COLUMN_PROGRESS_DATE, getCurrentDate())
        }

        return try {
            db.insert(TABLE_PROGRESS, null, values) != -1L
        } catch (e: Exception) {
            false
        } finally {
            db.close()
        }
    }

    // Получение текущей даты в формате YYYY-MM-DD
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Метод для принудительного создания базы
    fun initializeDatabase() {
        val db = writableDatabase
        db.close()
    }
}