package com.example.fitnessfinal

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnessfinal.database.DatabaseHelper
import com.example.fitnessfinal.utils.SessionManager
import android.content.Intent
import androidx.core.content.ContextCompat

class AdminActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var layoutUsersList: LinearLayout
    private lateinit var tvTotalUsers: TextView
    private lateinit var btnLogout: Button

    private var currentAdminId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        databaseHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        currentAdminId = sessionManager.getUserId()

        // Проверяем, является ли пользователь администратором
        if (!databaseHelper.isUserAdmin(currentAdminId)) {
            Toast.makeText(this, "Доступ запрещен", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        loadUsers()
    }

    private fun initViews() {
        layoutUsersList = findViewById(R.id.layoutUsersList)
        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        btnLogout = findViewById(R.id.btnLogout)

        btnLogout.setOnClickListener {
            logoutAdmin()
        }
    }

    private fun loadUsers() {
        layoutUsersList.removeAllViews()

        // Получаем ВСЕХ пользователей
        val users = databaseHelper.getAllUsers()
        tvTotalUsers.text = users.size.toString()

        // Фильтруем, чтобы не показывать самого админа в списке
        val otherUsers = users.filter { it.id != currentAdminId }

        if (otherUsers.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "Нет других пользователей"
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@AdminActivity, android.R.color.darker_gray))
                setPadding(0, 32, 0, 32)
            }
            layoutUsersList.addView(emptyView)
            return
        }

        otherUsers.forEach { user ->
            addUserView(user)
        }
    }

    private fun addUserView(user: com.example.fitnessfinal.model.User) {
        // Создаем карточку пользователя
        val userCard = layoutInflater.inflate(R.layout.item_user, layoutUsersList, false)

        val tvUserName: TextView = userCard.findViewById(R.id.tvUserName)
        val tvUserEmail: TextView = userCard.findViewById(R.id.tvUserEmail)
        val tvUserId: TextView = userCard.findViewById(R.id.tvUserId)
        val btnDeleteUser: Button = userCard.findViewById(R.id.btnDeleteUser)

        // Заполняем данными
        tvUserName.text = user.name
        tvUserEmail.text = user.email
        tvUserId.text = "ID: ${user.id}"

        // Кнопка удаления
        btnDeleteUser.setOnClickListener {
            showDeleteConfirmationDialog(user)
        }

        layoutUsersList.addView(userCard)
    }

    private fun showDeleteConfirmationDialog(user: com.example.fitnessfinal.model.User) {
        AlertDialog.Builder(this)
            .setTitle("Удаление пользователя")
            .setMessage("Вы уверены, что хотите удалить пользователя:\n${user.name}\n(${user.email})?")
            .setPositiveButton("Удалить") { dialog, which ->
                deleteUser(user.id)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun deleteUser(userId: Long) {
        if (databaseHelper.deleteUser(userId)) {
            Toast.makeText(this, "Пользователь удален", Toast.LENGTH_SHORT).show()
            loadUsers() // Перезагружаем список
        } else {
            Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logoutAdmin() {
        sessionManager.logoutUser()
        Toast.makeText(this, "Вы вышли из системы", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        loadUsers()
    }
}