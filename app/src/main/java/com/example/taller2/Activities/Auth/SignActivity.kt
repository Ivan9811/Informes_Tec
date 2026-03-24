package com.example.taller2.Activities.Auth

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taller2.R

class SignActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign)

        val rootView = findViewById<ViewGroup>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomPadding = maxOf(systemBars.bottom, imeInsets.bottom)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding)
            insets
        }

        // Botón registrarse → va al MainActivity
        val btnRegistro = findViewById<android.widget.Button>(R.id.btnRegistro)
        btnRegistro.setOnClickListener {
            val intent = android.content.Intent(this, com.example.taller2.MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // "Inicia sesión" → regresa al LoginActivity
        val txtVolverLogin = findViewById<android.widget.TextView>(R.id.txtVolverLogin)
        txtVolverLogin.setOnClickListener {
            val intent = android.content.Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}