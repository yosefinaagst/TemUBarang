package com.example.temu.data.remote

import com.example.temu.data.model.DetailHilang
import com.example.temu.data.model.FormHilang
import com.example.temu.data.model.HalamanLaporanHilang
import com.example.temu.data.model.LaporanHilang
import com.example.temubarang.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.util.UUID
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

/** Foto yang sudah dibaca dari galeri dan siap diunggah. */
class FotoUnggah(val bytes: ByteArray, val mime: String) {
    val ekstensi: String get() = if (mime == "image/png") "png" else "jpg"
}

/**
 * F1 · Akses Supabase untuk laporan barang hilang.
 * - Read daftar  : SELECT langsung ke tabel `reports` (policy hilang_feed_read)
 * - Read detail  : RPC detail_hilang
 * - Insert       : upload foto (opsional) → RPC simpan_hilang
 * - Update       : upload foto baru (opsional) → RPC perbarui_hilang → hapus foto lama
 * - Delete       : RPC hapus_hilang (soft delete)
 * - Foto         : Supabase Storage, bucket "foto-hilang", path <user_id>/<file>
 * Semua function & policy ada di supabase/f1_laporan_hilang.sql.
 */
class LaporanHilangRemoteDataSource {

    private val client
        get() = SupabaseClientProvider.client

    private val bucket
        get() = client.storage.from(BUCKET)

    suspend fun idPengguna(): String {
        client.auth.awaitInitialization()
        return requireNotNull(client.auth.currentUserOrNull()?.id) {
            "Sesi berakhir. Silakan masuk kembali."
        }
    }

    suspend fun daftar(
        kataKunci: String,
        kategori: String?,
        status: Set<String>,
        dari: Int,
        jumlah: Int
    ): HalamanLaporanHilang {
        // Karakter wildcard dibuang agar pencarian tetap pencarian teks biasa.
        val kunci = kataKunci.replace(Regex("[%_*\\\\]"), "").trim()

        val hasil = client.from("reports").select(
            columns = Columns.list(
                "id", "owner_id", "item_name", "category",
                "location", "event_date", "status", "photo_path"
            )
        ) {
            count(Count.EXACT)
            filter {
                eq("report_type", "lost")
                exact("deleted_at", null)
                if (kunci.isNotEmpty()) ilike("item_name", "%$kunci%")
                if (kategori != null) eq("category", kategori)
                if (status.isNotEmpty()) isIn("status", status.toList())
            }
            order("event_date", Order.DESCENDING)
            order("created_at", Order.DESCENDING)
            range(dari.toLong(), (dari + jumlah - 1).toLong())
        }

        val item = hasil.decodeList<JsonObject>().map { baris ->
            LaporanHilang(
                id = baris.teks("id"),
                ownerId = baris.teks("owner_id"),
                nama = baris.teks("item_name"),
                kategori = baris.teks("category"),
                lokasi = baris.teks("location"),
                tanggal = baris.teks("event_date"),
                status = baris.teks("status"),
                foto = baris.teks("photo_path")
            )
        }

        return HalamanLaporanHilang(
            item = item,
            total = hasil.countOrNull()?.toInt() ?: (dari + item.size)
        )
    }

    suspend fun detail(id: String): DetailHilang {
        val hasil = client.postgrest.rpc(
            "detail_hilang",
            buildJsonObject { put("p_id", id) }
        )
        val json = Json.parseToJsonElement(hasil.data).jsonObject

        return DetailHilang(
            id = json.teks("id"),
            nama = json.teks("nama"),
            kategori = json.teks("kategori"),
            deskripsi = json.teks("deskripsi"),
            ciriPublik = json.teks("ciri"),
            lokasi = json.teks("lokasi"),
            tanggal = json.teks("tanggal"),
            foto = json.teks("foto"),
            status = json.teks("status"),
            versi = (json["versi"] as? JsonPrimitive)?.intOrNull ?: 1,
            milikSaya = (json["milik_saya"] as? JsonPrimitive)?.booleanOrNull ?: false,
            namaPelapor = json.teks("pelapor"),
            kontak = json.teks("kontak")
        )
    }

    /** Signed URL (berlaku 1 jam) untuk menampilkan foto dari bucket privat. */
    suspend fun fotoUrl(path: String): String = bucket.createSignedUrl(path, 1.hours)

    /** Mengembalikan id laporan yang tersimpan. */
    suspend fun simpan(id: String, form: FormHilang, foto: FotoUnggah?): String {
        val uid = idPengguna()

        // Nama file = id laporan, jadi kirim ulang memakai file yang sama.
        val pathFoto = foto?.let {
            val path = "$uid/$id.${it.ekstensi}"
            bucket.upload(path, it.bytes) {
                contentType = ContentType.parse(it.mime)
                upsert = true
            }
            path
        }

        client.postgrest.rpc(
            "simpan_hilang",
            buildJsonObject {
                put("p_id", id)
                put("p_nama", form.nama.trim())
                put("p_kategori", form.kategori)
                put("p_deskripsi", form.deskripsi.trim())
                put("p_lokasi", form.lokasi.trim())
                put("p_tanggal", requireNotNull(form.tanggal) { "Pilih tanggal kehilangan." }.toString())
                put("p_ciri", form.ciriPublik.trim())
                put("p_foto", pathFoto)
            }
        )
        return id
    }

    /** Mengembalikan nomor versi baru setelah laporan diperbarui. */
    suspend fun perbarui(
        id: String,
        versi: Int,
        form: FormHilang,
        fotoBaru: FotoUnggah?,
        hapusFoto: Boolean
    ): Int {
        val uid = idPengguna()

        // Foto pengganti memakai nama file baru agar foto lama tidak tertimpa
        // sebelum perubahan benar-benar tersimpan.
        val pathBaru = fotoBaru?.let {
            val path = "$uid/${UUID.randomUUID()}.${it.ekstensi}"
            bucket.upload(path, it.bytes) {
                contentType = ContentType.parse(it.mime)
                upsert = false
            }
            path
        }

        val hasil = try {
            client.postgrest.rpc(
                "perbarui_hilang",
                buildJsonObject {
                    put("p_id", id)
                    put("p_versi", versi)
                    put("p_nama", form.nama.trim())
                    put("p_kategori", form.kategori)
                    put("p_deskripsi", form.deskripsi.trim())
                    put("p_lokasi", form.lokasi.trim())
                    put("p_tanggal", requireNotNull(form.tanggal) { "Pilih tanggal kehilangan." }.toString())
                    put("p_ciri", form.ciriPublik.trim())
                    put("p_foto", pathBaru)
                    put("p_hapus_foto", hapusFoto)
                }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Perubahan gagal: buang foto baru yang terlanjur diunggah.
            if (pathBaru != null) hapusFileDiam(pathBaru)
            throw e
        }

        val json = Json.parseToJsonElement(hasil.data).jsonObject
        val fotoLama = json.teks("foto_lama")
        if (fotoLama.isNotBlank()) hapusFileDiam(fotoLama)

        return (json["versi"] as? JsonPrimitive)?.intOrNull ?: (versi + 1)
    }

    suspend fun hapus(id: String) {
        client.postgrest.rpc(
            "hapus_hilang",
            buildJsonObject { put("p_id", id) }
        )
    }

    /** Bersih-bersih file; kegagalan diabaikan karena data utama sudah benar. */
    private suspend fun hapusFileDiam(path: String) {
        try {
            bucket.delete(path)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    private fun JsonObject.teks(kunci: String): String =
        (this[kunci] as? JsonPrimitive)?.contentOrNull.orEmpty()

    companion object {
        const val BUCKET = "foto-hilang"
    }
}
