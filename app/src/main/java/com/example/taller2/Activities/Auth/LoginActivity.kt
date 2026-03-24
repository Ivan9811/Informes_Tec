package com.example.taller2.Activities.Auth

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taller2.R

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón ingresar → va al MainActivity
        val btnIngresar = findViewById<android.widget.Button>(R.id.btnIngresar)
        btnIngresar.setOnClickListener {
            val intent = android.content.Intent(this, com.example.taller2.MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Texto "Regístrate" → va al SignActivity
        val registrate = findViewById<android.widget.TextView>(R.id.RegistratePregunta)
        registrate.setOnClickListener {
            val intent = android.content.Intent(this, com.example.taller2.Activities.Auth.SignActivity::class.java)
            startActivity(intent)
        }
    }
}