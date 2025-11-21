package com.example.fitnessfinal.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.fitnessfinal.model.User
import com.example.fitnessfinal.model.Workout



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
                FOREIGN KEY ($COLUMN_WORKOUT_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createWorkoutsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WORKOUTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
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
        }
    }

    // Авторизация пользователя
    // Авторизация пользователя
    fun loginUser(email: String, password: String): User? {
        val db = readableDatabase
        val query = """
        SELECT * FROM $TABLE_USERS 
        WHERE $COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(email, password))

        return if (cursor.moveToFirst()) {
            // Безопасное получение колонок
            val idIndex = cursor.getColumnIndex(COLUMN_USER_ID)
            val emailIndex = cursor.getColumnIndex(COLUMN_USER_EMAIL)
            val passwordIndex = cursor.getColumnIndex(COLUMN_USER_PASSWORD)
            val nameIndex = cursor.getColumnIndex(COLUMN_USER_NAME)

            User(
                id = if (idIndex != -1) cursor.getLong(idIndex) else 0,
                email = if (emailIndex != -1) cursor.getString(emailIndex) else "",
                password = if (passwordIndex != -1) cursor.getString(passwordIndex) else "",
                name = if (nameIndex != -1) cursor.getString(nameIndex) else ""
            )
        } else {
            null
        }.also { cursor.close() }
    }

    // Проверка существования email
    fun isEmailExists(email: String): Boolean {
        val db = readableDatabase
        val query = """
        SELECT * FROM $TABLE_USERS 
        WHERE $COLUMN_USER_EMAIL = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(email))
        val exists = cursor.count > 0
        cursor.close()
        return exists
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
        cursor.close()
        return workouts
    }

    // Метод для принудительного создания базы
    fun initializeDatabase() {
        val db = writableDatabase
        db.close()
    }

    // Добавьте эти методы в DatabaseHelper.kt

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

        return if (cursor.moveToFirst()) {
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
        }.also { cursor.close() }
    }

}