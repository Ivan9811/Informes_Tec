package com.example.taller2.Activities.main.perfil

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.taller2.R
import com.example.taller2.data.UsuarioRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

class PerfilFragment : Fragment() {

    private lateinit var imgFoto: ImageView
    private lateinit var txtNombreCompleto: TextView
    private lateinit var txtRol: TextView
    private lateinit var txtCorreo: TextView
    private lateinit var txtNombre: TextView
    private lateinit var txtApellido: TextView

    private var fotoUri: Uri? = null
    private var usuarioActual: UsuarioRepository.UsuarioData? = null

    // Archivo fijo para la cámara — siempre el mismo nombre
    private val NOMBRE_ARCHIVO_CAMARA = "foto_perfil_camara.jpg"

    // ── Cámara ───────────────────────────────────────────────────────────────
    private val tomarFoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        if (exito) {
            // Leer directamente el archivo fijo
            val archivo = File(requireContext().getExternalFilesDir(null), NOMBRE_ARCHIVO_CAMARA)
            if (archivo.exists() && archivo.length() > 0) {
                val uri = Uri.fromFile(archivo)
                subirFotoComprimida(uri)
            } else {
                Toast.makeText(requireContext(), "No se pudo leer la foto", Toast.LENGTH_SHORT).show()
            }
        }
        // Si exito=false simplemente no hacer nada (usuario canceló)
    }

    // ── Galería ──────────────────────────────────────────────────────────────
    private val elegirGaleria = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { subirFotoComprimida(it) }
    }

    // ── Permiso cámara ───────────────────────────────────────────────────────
    private val pedirPermiso = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                Toast.makeText(requireContext(), "Necesitas aceptar el permiso de cámara", Toast.LENGTH_SHORT).show()
            } else {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Permiso requerido")
                    .setMessage("Activa el permiso de cámara en Configuración.")
                    .setPositiveButton("Ir a Configuración") { _, _ ->
                        startActivity(android.content.Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            android.net.Uri.fromParts("package", requireContext().packageName, null)
                        ))
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        imgFoto           = view.findViewById(R.id.imgFotoPerfil)
        txtNombreCompleto = view.findViewById(R.id.txtNombreCompleto)
        txtRol            = view.findViewById(R.id.txtRol)
        txtCorreo         = view.findViewById(R.id.txtCorreo)
        txtNombre         = view.findViewById(R.id.txtNombre)
        txtApellido       = view.findViewById(R.id.txtApellido)

        view.findViewById<ImageButton>(R.id.btnCambiarFoto).setOnClickListener { mostrarOpcionesFoto() }
        view.findViewById<Button>(R.id.btnEditarPerfil).setOnClickListener { abrirEditarPerfil() }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarPerfil()
    }

    // ── Cargar perfil desde Supabase ─────────────────────────────────────────
    private fun cargarPerfil() {
        lifecycleScope.launch {
            try {
                val usuario = UsuarioRepository.obtenerUsuarioActual()
                if (usuario != null) {
                    usuarioActual = usuario
                    withContext(Dispatchers.Main) { mostrarDatos(usuario) }
                }
            } catch (e: Exception) {
                android.util.Log.e("PERFIL", "Error cargando perfil: ${e.message}")
            }
        }
    }

    private fun mostrarDatos(u: UsuarioRepository.UsuarioData) {
        txtNombreCompleto.text = "${u.nombre} ${u.apellido}"
        txtRol.text            = u.rol
        txtCorreo.text         = u.correo ?: "Sin correo"
        txtNombre.text         = u.nombre
        txtApellido.text       = u.apellido

        if (!u.fotoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load("${u.fotoUrl}?v=${System.currentTimeMillis()}")
                .diskCacheStrategy(DiskCacheStrategy.NONE) // no cachear
                .skipMemoryCache(true)                      // no memoria cache
                .circleCrop()
                .placeholder(R.drawable.account)
                .error(R.drawable.account)
                .into(imgFoto)
        }
    }

    // ── Opciones de foto ─────────────────────────────────────────────────────
    private fun mostrarOpcionesFoto() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Cambiar foto de perfil")
            .setItems(arrayOf("Tomar foto", "Elegir de galería")) { _, which ->
                when (which) {
                    0 -> verificarPermisoCamara()
                    1 -> elegirGaleria.launch("image/*")
                }
            }
            .show()
    }

    private fun verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) abrirCamara()
        else pedirPermiso.launch(Manifest.permission.CAMERA)
    }

    private fun abrirCamara() {
        try {
            val archivo = File(requireContext().getExternalFilesDir(null), NOMBRE_ARCHIVO_CAMARA)
            archivo.parentFile?.mkdirs()

            fotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                archivo
            )
            tomarFoto.launch(fotoUri)
        } catch (e: Exception) {
            android.util.Log.e("PERFIL", "Error abriendo cámara: ${e.message}")
            Toast.makeText(requireContext(), "Error al abrir cámara", Toast.LENGTH_SHORT).show()
        }
    }

    // ── Comprimir y subir foto ────────────────────────────────────────────────
    private fun subirFotoComprimida(uri: Uri) {
        // Mostrar imagen localmente de inmediato (sin esperar subida)
        Glide.with(this)
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .circleCrop()
            .into(imgFoto)

        lifecycleScope.launch {
            try {
                // Comprimir imagen en background
                val bytesComprimidos = withContext(Dispatchers.IO) {
                    comprimirImagen(uri)
                }

                if (bytesComprimidos == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error al procesar imagen", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                // Subir bytes comprimidos directamente
                val urlFoto = withContext(Dispatchers.IO) {
                    subirBytesAStorage(bytesComprimidos)
                }

                if (urlFoto.isNotEmpty()) {
                    // Actualizar en base de datos
                    UsuarioRepository.actualizarPerfil(
                        nombre   = usuarioActual?.nombre ?: "",
                        apellido = usuarioActual?.apellido ?: "",
                        correo   = usuarioActual?.correo ?: "",
                        fotoUrl  = urlFoto
                    )
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Foto actualizada ✓", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PERFIL", "Error subiendo foto: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Comprimir imagen a máximo 500KB
    private fun comprimirImagen(uri: Uri): ByteArray? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
                ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // Redimensionar si es muy grande
            val bitmapRedim = if (bitmap.width > 800 || bitmap.height > 800) {
                val ratio = minOf(800f / bitmap.width, 800f / bitmap.height)
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * ratio).toInt(),
                    (bitmap.height * ratio).toInt(),
                    true
                )
            } else bitmap

            val output = ByteArrayOutputStream()
            bitmapRedim.compress(Bitmap.CompressFormat.JPEG, 80, output)
            output.toByteArray()
        } catch (e: Exception) {
            android.util.Log.e("PERFIL", "Error comprimiendo: ${e.message}")
            null
        }
    }

    // Subir bytes al bucket avatars de Supabase
    private suspend fun subirBytesAStorage(bytes: ByteArray): String {
        val userId = com.example.taller2.proccess.SupabaseClient.client.auth
            .currentUserOrNull()?.id ?: return ""

        val rutaArchivo = "perfil_$userId.jpg"

        com.example.taller2.proccess.SupabaseClient.client
            .storage["avatars"]
            .upload(path = rutaArchivo, data = bytes, options = { upsert = true })

        return com.example.taller2.proccess.SupabaseClient.client
            .storage["avatars"]
            .publicUrl(rutaArchivo)
    }

    // ── Editar perfil ────────────────────────────────────────────────────────
    private fun abrirEditarPerfil() {
        val bundle = Bundle().apply {
            putString("nombre",   usuarioActual?.nombre ?: "")
            putString("apellido", usuarioActual?.apellido ?: "")
            putString("correo",   usuarioActual?.correo ?: "")
        }
        val fragment = EditarPerfilFragment().apply { arguments = bundle }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        if (usuarioActual == null) cargarPerfil()
    }
}

