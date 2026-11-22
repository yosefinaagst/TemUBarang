package com.example.temu.data.remote

import com.example.temu.data.model.FormTemuan
import com.example.temu.data.model.Temuan
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
import java.util.UUID

class TemuanRemoteDataSource {

    private val client
        get() = SupabaseClientProvider.client

    private val bucket
        get() = client.storage.from("foto-temuan")


    suspend fun daftar(): List<Temuan> =
        client.from("reports").select {
            filter {
                eq("report_type", "found")
                exact("deleted_at", null)
            }

            order("created_at", Order.DESCENDING)
            limit(50)
        }.decodeList<JsonObject>().map { row ->

            fun value(key: String): String {
                return row.getValue(key).jsonPrimitive.content
            }

            Temuan(
                id = value("id"),
                nama = value("item_name"),
                lokasi = value("location"),
                tanggal = value("event_date"),
                foto = value("photo_path"),
                deskripsi = value("description"),
                status = value("status"),
                kategori = value("category"),
                ownerId = value("owner_id")
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

    //fungsi mengambil id
    suspend fun idPengguna(): String {
        client.auth.awaitInitialization()

        return requireNotNull(
            client.auth.currentUserOrNull()?.id
        ) {
            "Sesi pengguna tidak ditemukan. Silakan login kembali."
        }
    }

    //fungsi edit laporan


    suspend fun perbarui(
        data: FormTemuan,
        foto: ByteArray? = null,
        mime: String? = null
    ) {
        val uid = idPengguna()

        // Pastikan laporan dimiliki pengguna yang sedang login
        val laporan = client.from("reports").select {
            filter {
                eq("id", data.id)
                eq("owner_id", uid)
                eq("report_type", "found")
                exact("deleted_at", null)
            }
        }.decodeList<JsonObject>()

        require(laporan.isNotEmpty()) {
            "Laporan tidak ditemukan atau bukan milik akun ini."
        }

        // Upload foto baru dengan nama file unik.
        // Jika tidak ada foto baru, pertahankan foto lama.
        val uploadedPhotoPath: String? = if (foto != null || mime != null) {

            val imageBytes = requireNotNull(foto) {
                "Data foto tidak lengkap."
            }

            val imageMime = requireNotNull(mime) {
                "Format foto tidak diketahui."
            }

            require(
                imageMime == "image/jpeg" ||
                        imageMime == "image/png"
            ) {
                "Foto harus JPG atau PNG."
            }

            val extension = if (imageMime == "image/png") {
                "png"
            } else {
                "jpg"
            }

            // Nama UUID baru agar tidak menimpa foto lama
            val fileId = UUID.randomUUID().toString()
            val newPath = "$uid/$fileId.$extension"

            // Upload sebagai objek baru, bukan UPDATE objek lama
            bucket.upload(newPath, imageBytes) {
                contentType = ContentType.parse(imageMime)
                upsert = false
            }

            newPath

        } else {
            null
        }

        // Susun data laporan yang diperbarui
        val payload = buildJsonObject {
            put("item_name", data.nama.trim())
            put("category", data.kategori)
            put("description", data.deskripsi.trim())
            put("location", data.lokasi.trim())
            put("event_date", data.tanggal)

            // Hanya ganti path jika pengguna memilih foto baru
            if (uploadedPhotoPath != null) {
                put("photo_path", uploadedPhotoPath)
            }

            put(
                "updated_at",
                java.time.Instant.now().toString()
            )
        }

        // Update laporan milik pengguna
        val hasil = client.from("reports").update(payload) {
            select()

            filter {
                eq("id", data.id)
                eq("owner_id", uid)
                eq("report_type", "found")
                exact("deleted_at", null)
            }
        }.decodeList<JsonObject>()

        require(hasil.isNotEmpty()) {
            "Laporan gagal diperbarui. Periksa izin akses akun."
        }
    }


    //delete laporan

    suspend fun hapus(id: String) {
        val uid = idPengguna()

        // Pastikan laporan ada dan dimiliki pengguna
        val laporan = client.from("reports").select {
            filter {
                eq("id", id)
                eq("owner_id", uid)
                eq("report_type", "found")
                exact("deleted_at", null)
            }
        }.decodeList<JsonObject>()

        require(laporan.isNotEmpty()) {
            "Laporan tidak ditemukan atau bukan milik akun ini."
        }

        val sekarang = java.time.Instant.now().toString()

        // Soft delete: tidak menghapus baris secara permanen
        val payload = buildJsonObject {
            put("deleted_at", sekarang)
            put("updated_at", sekarang)
        }

        val hasil = client.from("reports").update(payload) {
            select()

            filter {
                eq("id", id)
                eq("owner_id", uid)
                eq("report_type", "found")
                exact("deleted_at", null)
            }
        }.decodeList<JsonObject>()

        require(hasil.isNotEmpty()) {
            "Laporan gagal dihapus atau tidak memiliki izin."
        }
    }


}