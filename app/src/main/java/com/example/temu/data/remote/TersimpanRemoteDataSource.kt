package com.example.temu.data.remote

import com.example.temu.data.model.LaporanTersimpan
import com.example.temu.data.model.PilihanLaporan
import com.example.temubarang.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlin.time.Duration.Companion.hours
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * F4 · Akses Supabase untuk Laporan Tersimpan.
 * Hanya MEMBACA tabel `reports` yang sudah ada (tidak membuat tabel baru).
 * Daftar id laporan yang disimpan pengguna ada di HP (lihat data/local/TersimpanLokal.kt).
 */
class TersimpanRemoteDataSource {

    private val client
        get() = SupabaseClientProvider.client

    suspend fun idPengguna(): String {
        client.auth.awaitInitialization()
        return requireNotNull(client.auth.currentUserOrNull()?.id) {
            "Sesi berakhir. Silakan masuk kembali."
        }
    }

    /**
     * Read: data laporan untuk id-id yang disimpan.
     * Hasil: reportId → LaporanTersimpan (disimpanPada diisi oleh repository).
     * Laporan yang sudah dihapus / tidak boleh dibaca tidak ada di hasil.
     */
    suspend fun laporan(ids: List<String>): Map<String, LaporanTersimpan> {
        if (ids.isEmpty()) return emptyMap()
        return client.from("reports").select(
            columns = Columns.list(
                "id", "report_type", "item_name", "category", "description",
                "location", "event_date", "status", "photo_path", "deleted_at"
            )
        ) {
            filter { isIn("id", ids) }
        }.decodeList<JsonObject>()
            .map { baris ->
                LaporanTersimpan(
                    reportId = baris.teks("id"),
                    jenis = baris.teks("report_type"),
                    nama = baris.teks("item_name").ifBlank { "Tanpa nama" },
                    kategori = baris.teks("category"),
                    deskripsi = baris.teks("description"),
                    lokasi = baris.teks("location"),
                    tanggal = baris.teks("event_date"),
                    status = baris.teks("status"),
                    foto = baris.teks("photo_path"),
                    disimpanPada = "",
                    tersedia = baris.teks("deleted_at").isEmpty()
                )
            }
            .associateBy { it.reportId }
    }

    /** Read: laporan hilang & ditemukan yang bisa dipilih untuk disimpan. */
    suspend fun pilihan(jenis: String?): List<PilihanLaporan> =
        client.from("reports").select(
            columns = Columns.list(
                "id", "report_type", "item_name", "location", "event_date", "photo_path"
            )
        ) {
            filter {
                exact("deleted_at", null)
                if (jenis != null) eq("report_type", jenis)
            }
            order("created_at", Order.DESCENDING)
            limit(50)
        }.decodeList<JsonObject>().map { baris ->
            PilihanLaporan(
                id = baris.teks("id"),
                jenis = baris.teks("report_type"),
                nama = baris.teks("item_name"),
                lokasi = baris.teks("location"),
                tanggal = baris.teks("event_date"),
                foto = baris.teks("photo_path")
            )
        }

    /** Signed URL (berlaku 1 jam) untuk menampilkan foto dari bucket privat. */
    suspend fun fotoUrl(bucket: String, path: String): String =
        client.storage.from(bucket).createSignedUrl(path, 1.hours)

    private fun JsonObject.teks(kunci: String): String =
        (this[kunci] as? JsonPrimitive)?.contentOrNull.orEmpty()
}
