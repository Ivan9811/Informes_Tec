package com.example.taller2.Activities.Auth


import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.taller2.R
import com.example.taller2.proccess.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class SignActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etApellido: EditText
    private lateinit var etcorreoElectronico: EditText
    private lateinit var etcontraseña: EditText
    private lateinit var etrepetirContraseña: EditText
    private lateinit var checkTerminos: CheckBox
    private lateinit var btnRegsitro: Button
    private lateinit var tvCuenta: TextView


    @Serializable
    data class UsuarioData(
        val id: String,
        val nombre: String,
        val apellido: String
    )


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

        etNombre = findViewById(R.id.inputNombres)
        etApellido = findViewById(R.id.inputApellidos)
        etcorreoElectronico = findViewById(R.id.inputCorreo)
        etcontraseña = findViewById(R.id.inputPassword)
        etrepetirContraseña = findViewById(R.id.inputPasswordRepeat)
        checkTerminos = findViewById(R.id.checkTerminos)
        btnRegsitro = findViewById(R.id.btnRegistro)
        tvCuenta = findViewById(R.id.txtVolverLogin)

        // escuchar el btn de registro
        btnRegsitro.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val apellido = etApellido.text.toString().trim()
            val correoElectronico = etcorreoElectronico.text.toString().trim()
            val contraseña = etcontraseña.text.toString().trim()
            val repetircontraseña = etrepetirContraseña.text.toString().trim()

            // validaciones
            if (nombre.isEmpty() || apellido.isEmpty() || correoElectronico.isEmpty() || contraseña.isEmpty() || repetircontraseña.isEmpty()) {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (contraseña.length < 8) {
                Toast.makeText(
                    this,
                    "La contraseña debe tener al menos 8 caracteres",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            if (contraseña != repetircontraseña) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!checkTerminos.isChecked) {
                Toast.makeText(this, "Acepte los términos y condiciones", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Registro en Supabase
            lifecycleScope.launch {
                try {
                    // registrar el usuario en supabase
                    val resultado = SupabaseClient.client.auth.signUpWith(Email) {
                        email = correoElectronico
                        password = contraseña
                    }

                    // paso dos: guardar los datos adicionales
                    val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: ""
                    SupabaseClient.client.postgrest["usuarios"].insert(
                        UsuarioData(
                            id = userId,
                            nombre = nombre,
                            apellido = apellido
                        )
                    )

                    // paso 3: redirigir al usuario
                    runOnUiThread {
                        Toast.makeText(this@SignActivity, "Registro exitoso", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(Intent(this@SignActivity, LoginActivity::class.java))
                        finish()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(
                            this@SignActivity, "Error en el registro: ${e.message}", Toast.LENGTH_SHORT)
                    }

                }
            }
        }
        tvCuenta.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }
}

