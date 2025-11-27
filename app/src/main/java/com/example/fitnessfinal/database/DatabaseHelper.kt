package com.example.fitnessfinal.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.fitnessfinal.model.User
import com.example.fitnessfinal.model.Workout
import com.example.fitnessfinal.model.Progress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FitnessApp.db"
        private const val DATABASE_VERSION = 3

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
        println("✅ DatabaseHelper.onCreate() - Creating tables...")
        createAllTables(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        println("✅ DatabaseHelper.onUpgrade() - from $oldVersion to $newVersion")
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
        checkAndCreateTables(db)
    }

    private fun createAllTables(db: SQLiteDatabase) {
        try {
            // Создание таблицы пользователей
            val createUsersTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                    $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_USER_EMAIL TEXT UNIQUE NOT NULL,
                    $COLUMN_USER_PASSWORD TEXT NOT NULL,
                    $COLUMN_USER_NAME TEXT NOT NULL
                )
            """.trimIndent()

            // Создание таблицы тренировок
            val createWorkoutsTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_WORKOUTS (
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
                CREATE TABLE IF NOT EXISTS $TABLE_PROGRESS (
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

            println("✅ All tables created successfully!")

        } catch (e: Exception) {
            println("❌ ERROR creating tables: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun checkAndCreateTables(db: SQLiteDatabase) {
        val tables = arrayOf(TABLE_USERS, TABLE_WORKOUTS, TABLE_PROGRESS)

        tables.forEach { tableName ->
            val cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'",
                null
            )
            val exists = cursor.count > 0
            cursor.close()

            if (!exists) {
                println("❌ Table $tableName does not exist! Creating...")
                when (tableName) {
                    TABLE_USERS -> db.execSQL("""
                        CREATE TABLE $TABLE_USERS (
                            $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                            $COLUMN_USER_EMAIL TEXT UNIQUE NOT NULL,
                            $COLUMN_USER_PASSWORD TEXT NOT NULL,
                            $COLUMN_USER_NAME TEXT NOT NULL
                        )
                    """.trimIndent())
                    TABLE_WORKOUTS -> db.execSQL("""
                        CREATE TABLE $TABLE_WORKOUTS (
                            $COLUMN_WORKOUT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                            $COLUMN_WORKOUT_USER_ID INTEGER NOT NULL,
                            $COLUMN_WORKOUT_TITLE TEXT NOT NULL,
                            $COLUMN_WORKOUT_DESCRIPTION TEXT,
                            $COLUMN_WORKOUT_DURATION INTEGER NOT NULL,
                            $COLUMN_WORKOUT_DATE TEXT NOT NULL,
                            FOREIGN KEY ($COLUMN_WORKOUT_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID) ON DELETE CASCADE
                        )
                    """.trimIndent())
                    TABLE_PROGRESS -> db.execSQL("""
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
                    """.trimIndent())
                }
                println("✅ Table $tableName created")
            } else {
                println("✅ Table $tableName exists")
            }
        }
    }

    // === МЕТОДЫ ДЛЯ РАБОТЫ С ПОЛЬЗОВАТЕЛЯМИ ===

    fun registerUser(email: String, password: String, name: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USER_EMAIL, email)
            put(COLUMN_USER_PASSWORD, password)
            put(COLUMN_USER_NAME, name)
        }

        return try {
            val result = db.insert(TABLE_USERS, null, values) != -1L
            println("✅ User registered: $result")
            result
        } catch (e: Exception) {
            println("❌ Error registering user: ${e.message}")
            false
        } finally {
            db.close()
        }
    }

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

    // === МЕТОДЫ ДЛЯ РАБОТЫ С ТРЕНИРОВКАМИ ===

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
            val result = db.insert(TABLE_WORKOUTS, null, values) != -1L
            println("✅ Workout added: $result")
            result
        } catch (e: Exception) {
            println("❌ Error adding workout: ${e.message}")
            false
        } finally {
            db.close()
        }
    }

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
            println("Error getting workouts: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }
        return workouts
    }

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

    // === МЕТОДЫ ДЛЯ РАБОТЫ С ПРОГРЕССОМ ===

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
            val result = db.insert(TABLE_PROGRESS, null, values) != -1L
            println("✅ Progress added: $result")
            result
        } catch (e: Exception) {
            println("❌ Error adding progress: ${e.message}")
            false
        } finally {
            db.close()
        }
    }

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
            println("Error getting progress: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }
        return progressList
    }

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

    fun updateWeightOnly(userId: Long, weight: Double): Boolean {
        val db = writableDatabase
        println("✅ updateWeightOnly - userId: $userId, weight: $weight")

        val values = ContentValues().apply {
            put(COLUMN_PROGRESS_USER_ID, userId)
            put(COLUMN_PROGRESS_WEIGHT, weight)
            put(COLUMN_PROGRESS_DATE, getCurrentDate())
        }

        return try {
            val result = db.insert(TABLE_PROGRESS, null, values) != -1L
            println("✅ Insert result: $result")
            result
        } catch (e: Exception) {
            println("❌ Error in updateWeightOnly: ${e.message}")
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    // === ОТЛАДОЧНЫЕ МЕТОДЫ ===

    fun debugGetAllProgress(userId: Long): List<Progress> {
        val progressList = mutableListOf<Progress>()
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_PROGRESS 
            WHERE $COLUMN_PROGRESS_USER_ID = ? 
            ORDER BY $COLUMN_PROGRESS_DATE DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        println("✅ debugGetAllProgress - cursor count: ${cursor.count}")

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
                println("✅ Progress record: $progress")
            }
        } catch (e: Exception) {
            println("❌ Error in debugGetAllProgress: ${e.message}")
        } finally {
            cursor.close()
            db.close()
        }
        return progressList
    }

    // Получение текущей даты в формате YYYY-MM-DD
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // Метод для принудительного создания базы
    fun initializeDatabase() {
        val db = writableDatabase
        checkAndCreateTables(db)
        db.close()
    }
}