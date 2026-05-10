package com.example.taller2.Activities.main.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.taller2.R
import com.example.taller2.data.UsuarioRepository
import kotlinx.coroutines.launch

class EditarPerfilFragment : Fragment() {

    private lateinit var inputNombre: EditText
    private lateinit var inputApellido: EditText
    private lateinit var inputCorreo: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_editar_perfil, container, false)

        inputNombre   = view.findViewById(R.id.inputEditNombres)
        inputApellido = view.findViewById(R.id.inputEditApellidos)
        inputCorreo   = view.findViewById(R.id.inputEditCorreo)

        val btnGuardar: Button = view.findViewById(R.id.btnGuardarPerfil)

        // Pre-cargar datos recibidos desde PerfilFragment
        arguments?.let {
            inputNombre.setText(it.getString("nombre", ""))
            inputApellido.setText(it.getString("apellido", ""))
            inputCorreo.setText(it.getString("correo", ""))
        }

        btnGuardar.setOnClickListener { guardarCambios() }

        return view
    }

    private fun guardarCambios() {
        val nombre   = inputNombre.text.toString().trim()
        val apellido = inputApellido.text.toString().trim()
        val correo   = inputCorreo.text.toString().trim()

        if (nombre.isEmpty() || apellido.isEmpty()) {
            Toast.makeText(requireContext(), "Nombre y apellido son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                UsuarioRepository.actualizarPerfil(
                    nombre   = nombre,
                    apellido = apellido,
                    correo   = correo,
                    fotoUrl  = null
                )
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Perfil actualizado ✓", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack() // volver al perfil
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Error al guardar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}