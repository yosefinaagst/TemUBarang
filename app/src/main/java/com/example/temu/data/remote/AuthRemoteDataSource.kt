
package com.example.temu.data.remote

import com.example.temubarang.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRemoteDataSource {

    private val client
        get() = SupabaseClientProvider.client

    // Memeriksa sesi pengguna
    suspend fun sudahMasuk(): Boolean {
        client.auth.awaitInitialization()
        return client.auth.currentUserOrNull() != null
    }

    // Login
    suspend fun masuk(email: String, sandi: String) {
        client.auth.signInWith(Email) {
            this.email = email.trim()
            password = sandi
        }
    }

    // Register
    suspend fun daftar(
        namaLengkap: String,
        email: String,
        sandi: String
    ) {
        client.auth.signUpWith(Email) {
            this.email = email.trim()
            password = sandi

            data = buildJsonObject {
                put("full_name", namaLengkap.trim())
            }
        }
    }

    // Logout
    suspend fun keluar() {
        client.auth.signOut()
    }
}
