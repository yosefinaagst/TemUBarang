package com.example.temu.data.remote

import com.example.temubarang.data.model.FormTemuan
import com.example.temubarang.data.model.Temuan
import com.example.temubarang.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.serialization.json.*
import java.util.Objects.isNull
import kotlin.time.Duration.Companion.hours

class TemuanRemoteDataSource {

    private val client
        get() = SupabaseClientProvider.client

    private val bucket
        get() = client.storage.from("foto-temuan")

    suspend fun daftar(): List<Temuan> =
        client.from("reports").select {
            filter {
                eq("report_type", "found")
                isNull("deleted_at")
            }
            order("created_at", Order.DESCENDING)
            limit(50)
        }.decodeList<JsonObject>().map { row ->

            fun value(key: String): String =
                row[key]?.jsonPrimitive?.contentOrNull ?: ""

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

    suspend fun fotoUrl(path: String): String =
        bucket.createSignedUrl(path, 1.hours)

    suspend fun simpan(
        form: FormTemuan,
        bytes: ByteArray,
        mime: String
    ) {
        val uid = requireNotNull(
            client.auth.currentUserOrNull()?.id
        ) {
            "Silakan masuk kembali."
        }

        // Memeriksa apakah laporan dengan ID ini sudah tersimpan.
        val existing = client.from("reports").select {
            filter {
                eq("id", form.id)
                eq("owner_id", uid)
            }
        }.decodeList<JsonObject>()

        if (existing.isNotEmpty()) return

        val ext = if (mime == "image/png") "png" else "jpg"
        val path = "$uid/${form.id}.$ext"

        bucket.upload(path, bytes) {
            contentType = ContentType.parse(mime)
            upsert = true
        }

        client.postgrest.rpc(
            "simpan_temuan",
            buildJsonObject {
                put("p_id", form.id)
                put("p_nama", form.nama)
                put("p_kategori", form.kategori)
                put("p_deskripsi", form.deskripsi)
                put("p_lokasi", form.lokasi)
                put("p_tanggal", form.tanggal)
                put("p_foto", path)
            }
        )
    }
}