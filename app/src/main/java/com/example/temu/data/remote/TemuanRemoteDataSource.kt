package com.example.temubarang.data.remote

import com.example.temubarang.data.model.FormTemuan
import com.example.temubarang.data.model.Temuan
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.serialization.json.*
import kotlin.time.Duration.Companion.hours

class TemuanRemoteDataSource {
    private val client get() = SupabaseClientProvider.client
    private val bucket get() = client.storage.from("foto-temuan")

    suspend fun sudahMasuk(): Boolean {
        client.auth.awaitInitialization()
        return client.auth.currentUserOrNull() != null
    }

    suspend fun masuk(alamat: String, sandi: String) {
        client.auth.signInWith(Email) {
            email = alamat.trim()
            password = sandi
        }
    }

    suspend fun daftar(): List<Temuan> =
        client.from("reports").select {
            filter { eq("report_type", "found"); exact("deleted_at", null) }
            order("created_at", Order.DESCENDING)
            limit(50)
        }.decodeList<JsonObject>().map { row ->
            fun value(key: String) = row.getValue(key).jsonPrimitive.content
            Temuan(
                value("id"),
                value("item_name"),
                value("location"),
                value("event_date"),
                value("photo_path"),
                value("description"),
                value("status"),
                kategori = value("category")
            )
        }

    suspend fun fotoUrl(path: String) = bucket.createSignedUrl(path, 1.hours)

    suspend fun simpan(form: FormTemuan, bytes: ByteArray, mime: String) {
        val uid = requireNotNull(client.auth.currentUserOrNull()?.id) { "Silakan masuk kembali." }
        // Jika jawaban server sebelumnya terputus, cek hasil sebelum mengunggah ulang.
        val existing = client.from("reports").select {
            filter { eq("id", form.id); eq("owner_id", uid) }
        }.decodeList<JsonObject>()
        if (existing.isNotEmpty()) return
        val ext = if (mime == "image/png") "png" else "jpg"
        val path = "$uid/${form.id}.$ext"
        bucket.upload(path, bytes) {
            contentType = ContentType.parse(mime)
            upsert = true
        }
        client.postgrest.rpc("simpan_temuan", buildJsonObject {
            put("p_id", form.id)
            put("p_nama", form.nama)
            put("p_kategori", form.kategori)
            put("p_deskripsi", form.deskripsi)
            put("p_lokasi", form.lokasi)
            put("p_tanggal", form.tanggal)
            put("p_foto", path)
        })
    }
}
