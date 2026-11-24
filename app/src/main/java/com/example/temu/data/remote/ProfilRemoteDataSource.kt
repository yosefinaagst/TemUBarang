package com.example.temu.data.remote

import com.example.temu.data.model.Profil
import com.example.temubarang.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.time.Instant
import kotlin.time.Duration.Companion.hours

class ProfilRemoteDataSource {

    private val client
        get() = SupabaseClientProvider.client

    private val bucketFoto
        get() = client.storage.from(BUCKET_FOTO)

    private suspend fun penggunaAktif(): UserInfo {
        client.auth.awaitInitialization()
        return requireNotNull(client.auth.currentUserOrNull()) {
            "Sesi pengguna tidak ditemukan. Silakan login kembali."
        }
    }

    private fun JsonObject.teks(key: String) = this[key]?.jsonPrimitive?.contentOrNull

    private suspend fun urlFoto(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return runCatching { bucketFoto.createSignedUrl(path, 1.hours) }.getOrNull()
    }

    private suspend fun keProfil(user: UserInfo, baris: JsonObject?, namaCadangan: String = ""): Profil {
        val fotoPath = baris?.teks(KOLOM_FOTO)?.ifBlank { null }
        return Profil(
            id = user.id,
            namaLengkap = baris?.teks(KOLOM_NAMA)?.ifBlank { null } ?: namaCadangan,
            email = user.email.orEmpty(),
            kontak = baris?.teks(KOLOM_KONTAK)?.ifBlank { null },
            fotoPath = fotoPath,
            fotoUrl = urlFoto(fotoPath)
        )
    }

    // ---------------- Ambil profil ----------------

    suspend fun ambil(): Profil {
        val user = penggunaAktif()

        val baris = client.from(TABEL).select {
            filter { eq("id", user.id) }
        }.decodeList<JsonObject>().firstOrNull()

        // Cadangan: nama yang diisi saat daftar (disimpan di metadata Auth)
        val namaSaatDaftar = user.userMetadata?.teks("full_name").orEmpty()

        return keProfil(user, baris, namaSaatDaftar)
    }

    // ---------------- Simpan (edit profil) ----------------

    /**
     * @param fotoBaru   foto JPEG baru (null = foto tidak diganti)
     * @param hapusFoto  true = kembali ke avatar inisial
     * @param fotoLama   path foto yang sekarang tersimpan (untuk dihapus dari Storage)
     *
     * Pakai upsert: kalau baris profil belum ada (mis. akun lama), baris dibuat.
     */
    suspend fun simpan(
        namaLengkap: String,
        kontak: String?,
        fotoBaru: ByteArray?,
        hapusFoto: Boolean,
        fotoLama: String?
    ): Profil {
        val user = penggunaAktif()
        require(namaLengkap.isNotBlank()) { "Nama lengkap wajib diisi." }

        // 1. Upload foto baru (nama file unik supaya tidak tertahan cache)
        val fotoPath: String? = when {
            fotoBaru != null -> {
                val path = "${user.id}/avatar-${System.currentTimeMillis()}.jpg"
                bucketFoto.upload(path, fotoBaru) {
                    upsert = true
                    contentType = ContentType.Image.JPEG
                }
                path
            }
            hapusFoto -> null
            else -> fotoLama
        }

        // 2. Simpan ke tabel profiles
        val payload = buildJsonObject {
            put("id", user.id)
            put(KOLOM_NAMA, namaLengkap.trim())
            put(KOLOM_KONTAK, kontak?.trim()?.ifBlank { null })
            put(KOLOM_FOTO, fotoPath)
            put("updated_at", Instant.now().toString())
        }

        val baris = client.from(TABEL).upsert(payload) {
            select()
        }.decodeList<JsonObject>().firstOrNull()
            ?: throw IllegalStateException("Profil gagal disimpan. Periksa izin akses akun.")

        // 3. Bersihkan foto lama yang sudah tidak dipakai (gagal pun tidak masalah)
        if (!fotoLama.isNullOrBlank() && fotoLama != fotoPath) {
            runCatching { bucketFoto.delete(fotoLama) }
        }

        return keProfil(user, baris)
    }

    // ---------------- Keluar ----------------

    suspend fun keluar() {
        client.auth.signOut()
    }

    companion object {
        // Nama tabel, kolom, dan bucket di Supabase
        private const val TABEL = "profiles"
        private const val KOLOM_NAMA = "full_name"
        private const val KOLOM_KONTAK = "contact"
        private const val KOLOM_FOTO = "avatar_path"
        private const val BUCKET_FOTO = "foto-profil"
    }
}
