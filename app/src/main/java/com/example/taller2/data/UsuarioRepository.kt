package com.example.taller2.data
import com.example.taller2.proccess.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object UsuarioRepository {

    @Serializable
    data class UsuarioData(
        val id: String = "",
        val nombre: String = "",
        val apellido: String = "",
        val correo: String? = null,
        val rol: String = "cliente",
        @SerialName("foto_url")
        val fotoUrl: String? = null,
        @SerialName("created_at")
        val createdAt: String = ""
    )

    suspend fun existeUsuario(userId: String): Boolean {
        return try {
            val resultado = SupabaseClient.client
                .postgrest["usuarios"]
                .select(Columns.raw("id")) {
                    filter { eq("id", userId) }
                }
                .decodeList<Map<String, String>>()
            resultado.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun obtenerUsuarioActual(): UsuarioData? {
        val userId = SupabaseClient.client.auth
            .currentUserOrNull()?.id ?: return null
        return try {
            val resultado = SupabaseClient.client
                .postgrest["usuarios"]
                .select {
                    filter { eq("id", userId) }
                }
                .decodeList<UsuarioData>()

            android.util.Log.d("DEBUG_QUERY", "Resultado: $resultado")
            resultado.firstOrNull()
        } catch (e: Exception) {
            android.util.Log.e("DEBUG_QUERY", "Error: ${e.message}")
            null
        }
    }

    suspend fun insertarUsuario(id: String, nombre: String, apellido: String, correo: String) {
        SupabaseClient.client.postgrest["usuarios"].insert(
            UsuarioData(id = id, nombre = nombre, apellido = apellido, correo = correo)
        )
    }

    suspend fun obtenerRolActual(): String {
        return try {
            val userId = SupabaseClient.client.auth
                .currentUserOrNull()?.id ?: return "cliente"

            val resultado = SupabaseClient.client
                .postgrest["usuarios"]
                .select {
                    filter { eq("id", userId) }
                }
                .decodeList<UsuarioData>()

            resultado.firstOrNull()?.rol ?: "cliente"
        } catch (e: Exception) {
            "cliente"
        }
    }

    suspend fun actualizarPerfil(
        nombre: String,
        apellido: String,
        correo: String,
        fotoUrl: String? = null
    ) {
        val userId = SupabaseClient.client.auth
            .currentUserOrNull()?.id ?: return

        val datos = buildJsonObject {
            put("nombre", nombre)
            put("apellido", apellido)
            put("correo", correo)
            if (fotoUrl != null) put("foto_url", fotoUrl)
        }

        SupabaseClient.client.postgrest["usuarios"]
            .update(datos) {
                filter { eq("id", userId) }
            }
    }

    suspend fun subirFotoPerfil(
        contexto: android.content.Context,
        uri: android.net.Uri
    ): String {
        val userId = SupabaseClient.client.auth
            .currentUserOrNull()?.id ?: return ""

        val bytes = if (uri.scheme == "content") {
            contexto.contentResolver.openInputStream(uri)?.readBytes()
        } else {
            java.io.File(uri.path!!).readBytes()
        } ?: return ""

        android.util.Log.d("DEBUG_FOTO", "Bytes leídos: ${bytes.size}")

        val rutaArchivo = "perfil_$userId.jpg"

        SupabaseClient.client.storage["avatars"]
            .upload(path = rutaArchivo, data = bytes, options = { upsert = true })

        val url = SupabaseClient.client.storage["avatars"].publicUrl(rutaArchivo)

        android.util.Log.d("DEBUG_FOTO", "URL generada: $url")

        return url
    }
}
