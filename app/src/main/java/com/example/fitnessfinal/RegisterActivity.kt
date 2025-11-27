package com.example.fitnessfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitnessfinal.database.DatabaseHelper

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initViews()
        databaseHelper = DatabaseHelper(this)

        // РЕАЛЬНАЯ регистрация
        btnRegister.setOnClickListener {
            registerUser()
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Проверяем что все поля заполнены
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверяем существует ли email
        if (databaseHelper.isEmailExists(email)) {
            Toast.makeText(this, "Пользователь с такой почтой уже существует", Toast.LENGTH_SHORT).show()
            return
        }

        // Регистрируем пользователя
        if (databaseHelper.registerUser(email, password, name)) {
            // Получаем ID только что зарегистрированного пользователя
            val user = databaseHelper.loginUser(email, password)

            if (user != null) {
                Toast.makeText(this, "Регистрация успешна! Заполните ваши параметры", Toast.LENGTH_SHORT).show()

                // Переходим на экран заполнения прогресса
                val intent = Intent(this, InitialProgressActivity::class.java)
                intent.putExtra("USER_ID", user.id)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Ошибка при получении данных пользователя", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        } else {
            Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show()
        }
    }
}