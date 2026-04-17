package com.example.taller2.Activities.Auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.example.taller2.MainActivity
import com.example.taller2.R
import com.example.taller2.data.CredencialesManager
import com.example.taller2.proccess.SupabaseClient
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etContrasena: EditText
    private lateinit var btnIngresar: Button
    private lateinit var tvRegistrate: TextView
    private lateinit var tvRecuperar: TextView
    private lateinit var tvHuella: TextView
    private lateinit var layoutGoogle: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_login)

        val rootView = findViewById<android.view.ViewGroup>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomPadding = maxOf(systemBars.bottom, imeInsets.bottom)
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding)
            insets
        }

        etCorreo = findViewById(R.id.ingresarusuario)
        etContrasena = findViewById(R.id.ingresarpassword)
        btnIngresar = findViewById(R.id.btnIngresar)
        tvRegistrate = findViewById(R.id.RegistratePregunta)
        tvRecuperar = findViewById(R.id.recuperarcontraseña)
        tvHuella = findViewById(R.id.ingresarHuella)
        layoutGoogle = findViewById(R.id.IngresoGoogle)

        btnIngresar.setOnClickListener { iniciarSesion() }

        tvRegistrate.setOnClickListener {
            startActivity(Intent(this, SignActivity::class.java))
        }

        tvRecuperar.setOnClickListener {
            Toast.makeText(this, "Proximamente", Toast.LENGTH_SHORT).show()
        }

        layoutGoogle.setOnClickListener { iniciarSesionConGoogle() }

        // Referencia al botón de huella
        configurarVisibilidadHuella()
        tvHuella.setOnClickListener { mostrarDialogoHuella() }
    }

    // Se ejecuta cada vez que la Activity vuelve a primer plano
    override fun onResume() {
        super.onResume()
        configurarVisibilidadHuella()
    }

    // Inicio de sesión con correo y contraseña usando Supabase
    private fun iniciarSesion() {
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString()

        if (correo.isEmpty() || contrasena.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        if (contrasena.length < 6) {
            Toast.makeText(this, "La contrasena debe tener minimo 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    email = correo
                    password = contrasena
                }
                // Guardar credenciales para uso posterior con la huella
                CredencialesManager.guardarCredenciales(this@LoginActivity, correo,contrasena, huella_activa = true )
                irAPantallaPrincipal()
            } catch (e: Exception) {
                val mensaje = when {
                    e.message?.contains("Invalid login credentials") == true ->
                        "Correo o contrasena incorrectos"
                    else -> "Error al iniciar sesion: ${e.message}"
                }
                Toast.makeText(this@LoginActivity, mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }

    // Inicio de sesión con Google usando Credential Manager + Supabase
    private fun iniciarSesionConGoogle() {
        lifecycleScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("49815868735-q9fhaulk4o2v60r1dcr7gr6p13hnc3p5.apps.googleusercontent.com")
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(this@LoginActivity)
                val result = credentialManager.getCredential(this@LoginActivity, request)

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)

                SupabaseClient.client.auth.signInWith(IDToken) {
                    idToken = googleIdTokenCredential.idToken
                    provider = Google
                }

                irAPantallaPrincipal()
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Error al iniciar con Google: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Muestra u oculta el botón de huella según credenciales guardadas y sensor disponible
    private fun configurarVisibilidadHuella() {
        val huellaActiva = CredencialesManager.huellaActiva(this)
        val biometricManager = BiometricManager.from(this)
        val biometriaDisponible = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS

        tvHuella.visibility = if (huellaActiva && biometriaDisponible) View.VISIBLE else View.GONE
    }

    // Muestra el diálogo de autenticación biométrica
    private fun mostrarDialogoHuella() {
        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val correo = CredencialesManager.obtenerCorreo(this@LoginActivity)
                    val contrasena = CredencialesManager.obtenerContrasena(this@LoginActivity)

                    if (correo != null && contrasena != null) {
                        lifecycleScope.launch {
                            try {
                                SupabaseClient.client.auth.signInWith(Email) {
                                    email = correo
                                    password = contrasena
                                }
                                irAPantallaPrincipal()
                            } catch (e: Exception) {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@LoginActivity,
                                        "Error al iniciar sesion: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Sesion expirada. Inicia sesion con tu correo.",
                            Toast.LENGTH_LONG
                        ).show()
                        CredencialesManager.limpiarCredenciales(this@LoginActivity)
                        configurarVisibilidadHuella()
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Error biometrico: $errString",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    Toast.makeText(
                        this@LoginActivity,
                        "Huella no reconocida, intenta de nuevo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso con huella")
            .setSubtitle("Usa tu huella dactilar para ingresar")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // Navega a la pantalla principal y limpia el back stack
    private fun irAPantallaPrincipal() {
        runOnUiThread {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finishAffinity()
        }
    }
}