package com.example.fitnessfinal

import android.app.AlertDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.fitnessfinal.database.DatabaseHelper
import com.example.fitnessfinal.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProgressActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private var userId: Long = 0

    private lateinit var tvCurrentWeight: TextView
    private lateinit var tvCurrentHeight: TextView
    private lateinit var btnUpdateWeight: Button
    private lateinit var btnBack: ImageButton
    private lateinit var llProgressHistory: LinearLayout
    private lateinit var chartContainer: LinearLayout // Контейнер для графика

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("✅ ProgressActivity onCreate started")

        try {
            setContentView(R.layout.activity_progress)
            println("✅ Layout set successfully")

            databaseHelper = DatabaseHelper(this)
            sessionManager = SessionManager(this)

            // Получаем ID пользователя из Intent или из SessionManager
            userId = intent.getLongExtra("USER_ID", 0)
            if (userId == 0L) {
                userId = sessionManager.getUserId()
                println("✅ User ID from SessionManager: $userId")
            }

            if (userId == 0L || userId == -1L) {
                Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            initViews()
            setupClickListeners()
            loadProgressData() // Загружаем данные при создании активности

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
            btnBack = findViewById(R.id.btnBack)
            llProgressHistory = findViewById(R.id.llProgressHistory)
            chartContainer = findViewById(R.id.chartContainer) // Находим контейнер
            println("✅ Views initialized successfully")
        } catch (e: Exception) {
            println("❌ ERROR initializing views: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private fun setupClickListeners() {
        println("✅ Setting up click listeners")

        // Кнопка назад
        btnBack.setOnClickListener {
            println("✅ Back button clicked")
            finish()
        }

        // Кнопка обновления веса
        btnUpdateWeight.setOnClickListener {
            println("✅ Update weight button clicked")
            showUpdateWeightDialog()
        }
    }

    private fun loadProgressData() {
        println("✅ Loading progress data for user: $userId")
        try {
            // Загружаем последние данные
            val latestProgress = databaseHelper.getLatestProgress(userId)
            println("✅ Latest progress: $latestProgress")

            if (latestProgress != null) {
                // ОБНОВЛЯЕМ ДАННЫЕ НА ЭКРАНЕ
                tvCurrentWeight.text = String.format(Locale.getDefault(), "%.1f кг", latestProgress.weight)
                tvCurrentHeight.text = if (latestProgress.height != null)
                    String.format(Locale.getDefault(), "%.1f см", latestProgress.height) else "не указан"

                println("✅ UI updated - Weight: ${latestProgress.weight}, Height: ${latestProgress.height}")
            } else {
                tvCurrentWeight.text = "нет данных"
                tvCurrentHeight.text = "не указан"
                println("✅ No progress data found")
            }

            // Загружаем историю
            loadProgressHistory()

            // Настраиваем график веса
            setupSimpleWeightChart()

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
                val historyItem = TextView(this).apply {
                    text = String.format(Locale.getDefault(), "%s - %.1f кг", formatDateForDisplay(progress.date), progress.weight)
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

    private fun setupSimpleWeightChart() {
        println("✅ Setting up simple weight chart")

        val progressList = databaseHelper.getAllUserProgress(userId)

        // Очищаем контейнер графика
        chartContainer.removeAllViews()

        if (progressList.size < 2) {
            // Если мало данных, показываем сообщение
            val emptyView = TextView(this).apply {
                text = "Добавьте еще измерения веса, чтобы увидеть график"
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@ProgressActivity, android.R.color.darker_gray))
                setPadding(0, 32, 0, 32)
                gravity = android.view.Gravity.CENTER
            }
            chartContainer.addView(emptyView)
            return
        }

        // Создаем и добавляем наш кастомный график
        val chartView = SimpleLineChartView(this)
        chartContainer.addView(chartView)

        // Подготавливаем данные для графика
        val chartData = ArrayList<Pair<String, Float>>()

        // Сортируем по дате (старые -> новые)
        val sortedList = progressList.sortedBy { it.date }

        sortedList.forEach { progress ->
            chartData.add(Pair(formatDateForChart(progress.date), progress.weight.toFloat()))
        }

        // Устанавливаем данные в график
        chartView.setData(chartData)

        println("✅ Simple weight chart setup successfully with ${chartData.size} points")
    }

    // Кастомный View для рисования графика - МАКСИМАЛЬНО БОЛЬШОЙ
    class SimpleLineChartView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

        private var points: List<Pair<Float, Float>> = emptyList()
        private var labels: List<String> = emptyList()
        private var maxY = 0f
        private var minY = 0f

        fun setData(data: List<Pair<String, Float>>) {
            if (data.isEmpty()) return

            labels = data.map { it.first }
            points = data.mapIndexed { index, pair ->
                Pair(index.toFloat(), pair.second)
            }

            maxY = points.maxOfOrNull { it.second } ?: 0f
            minY = points.minOfOrNull { it.second } ?: 0f

            // Небольшие отступы для графика
            val range = maxY - minY
            if (range > 0) {
                maxY += range * 0.1f  // Только 10% сверху
                minY = maxOf(0f, minY)  // Снизу без отступа
            }

            invalidate()
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            // ФИКСИРОВАННАЯ большая высота
            val desiredHeight = 500
            val height = resolveSize(desiredHeight, heightMeasureSpec)
            val width = MeasureSpec.getSize(widthMeasureSpec)
            setMeasuredDimension(width, height)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            if (points.size < 2) {
                drawNoDataMessage(canvas)
                return
            }

            // МИНИМАЛЬНЫЕ ОТСТУПЫ - график займет почти весь View!
            val paddingLeft = 60f    // Минимум для подписей Y
            val paddingRight = 20f   // Почти нет
            val paddingTop = 50f     // Для заголовка
            val paddingBottom = 70f  // Для дат

            val chartWidth = width - paddingLeft - paddingRight
            val chartHeight = height - paddingTop - paddingBottom

            // Фон
            canvas.drawColor(Color.WHITE)

            // 1. СЕТКА (светлая)
            val gridPaint = Paint().apply {
                color = Color.parseColor("#EEEEEE")
                strokeWidth = 1f
            }

            // Горизонтальные линии
            for (i in 0..4) {
                val y = paddingTop + (i * chartHeight / 4)
                canvas.drawLine(paddingLeft, y, paddingLeft + chartWidth, y, gridPaint)
            }

            // 2. ОСИ (тонкие)
            val axisPaint = Paint().apply {
                color = Color.parseColor("#888888")
                strokeWidth = 2f
            }
            canvas.drawLine(paddingLeft, paddingTop + chartHeight,
                paddingLeft + chartWidth, paddingTop + chartHeight, axisPaint) // X
            canvas.drawLine(paddingLeft, paddingTop, paddingLeft, paddingTop + chartHeight, axisPaint) // Y

            // 3. ПОДПИСИ Y (маленькие, справа от оси)
            val yLabelPaint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.text_secondary)
                textSize = 12f
                textAlign = Paint.Align.RIGHT
            }

            for (i in 0..4) {
                val yValue = minY + (maxY - minY) * (4 - i) / 4
                val y = paddingTop + (i * chartHeight / 4)
                val label = String.format(Locale.getDefault(), "%.1f", yValue)
                canvas.drawText(label, paddingLeft - 5, y + 4, yLabelPaint)
            }

            // 4. ВЫЧИСЛЯЕМ КООРДИНАТЫ ТОЧЕК
            // МАЛЕНЬКОЕ расстояние между точками на оси X
            val pointSpacing = chartWidth / (points.size - 1).coerceAtLeast(1)

            val pointCoords = points.mapIndexed { index, point ->
                val x = paddingLeft + (index * pointSpacing)
                val y = paddingTop + chartHeight - ((point.second - minY) / (maxY - minY)) * chartHeight
                Pair(x, y)
            }

            // 5. ЛИНИЯ ГРАФИКА (толстая)
            val linePaint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.primary_gold)
                strokeWidth = 4f
                style = Paint.Style.STROKE
                isAntiAlias = true
                strokeCap = Paint.Cap.ROUND
            }

            // Рисуем линию
            val path = Path()
            pointCoords.forEachIndexed { index, (x, y) ->
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            canvas.drawPath(path, linePaint)

            // 6. ТОЧКИ (средние)
            val pointPaint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.primary_gold)
                style = Paint.Style.FILL
            }

            pointCoords.forEach { (x, y) ->
                canvas.drawCircle(x, y, 8f, pointPaint)
                canvas.drawCircle(x, y, 4f, Paint().apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                })
            }

            // 7. ПОДПИСИ X (даты) - МАЛЕНЬКИЕ и под углом если много
            val xLabelPaint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.text_secondary)
                textSize = 11f
                textAlign = Paint.Align.CENTER
            }

            // Если точек много, рисуем под углом
            val rotateDates = points.size > 6

            points.forEachIndexed { index, _ ->
                val x = paddingLeft + (index * pointSpacing)
                val date = labels.getOrNull(index) ?: ""

                if (rotateDates) {
                    canvas.save()
                    canvas.translate(x, paddingTop + chartHeight + 25)
                    canvas.rotate(-45f)
                    canvas.drawText(date, 0f, 0f, xLabelPaint)
                    canvas.restore()
                } else {
                    canvas.drawText(date, x, paddingTop + chartHeight + 20, xLabelPaint)
                }
            }

            // 8. ЗАГОЛОВОК
            val titlePaint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.text_primary)
                textSize = 16f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvas.drawText("Изменение веса",
                paddingLeft + chartWidth / 2, paddingTop - 15, titlePaint)

            // 9. ПОДПИСЬ ОСИ Y (вертикально)
            val yAxisLabelPaint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.text_secondary)
                textSize = 12f
                textAlign = Paint.Align.CENTER
            }
            canvas.save()
            canvas.translate(paddingLeft - 25, paddingTop + chartHeight / 2)
            canvas.rotate(-90f)
            canvas.drawText("кг", 0f, 0f, yAxisLabelPaint)
            canvas.restore()
        }

        private fun drawNoDataMessage(canvas: Canvas) {
            val textPaint = Paint().apply {
                color = ContextCompat.getColor(context, R.color.text_secondary)
                textSize = 16f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Нужно минимум 2 измерения",
                width / 2f, height / 2f, textPaint)
        }
    }

    private fun showUpdateWeightDialog() {
        println("✅ Showing update weight dialog")
        try {
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
                    val success = databaseHelper.updateWeightOnly(userId, weight)
                    println("✅ Save result: $success")

                    if (success) {
                        Toast.makeText(this, "Вес обновлен!", Toast.LENGTH_SHORT).show()

                        // Принудительная перезагрузка с задержкой для гарантии
                        Handler(Looper.getMainLooper()).postDelayed({
                            println("✅ Reloading data after save...")
                            loadProgressData()
                        }, 300)

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

    private fun formatDateForChart(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd.MM", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onResume() {
        super.onResume()
        println("✅ ProgressActivity onResume - reloading data")
        // Перезагружаем данные при возвращении на экран
        loadProgressData()
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseHelper.close()
        println("✅ ProgressActivity destroyed")
    }
}