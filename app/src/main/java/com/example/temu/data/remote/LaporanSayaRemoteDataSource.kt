package com.example.temu.data.remote

import com.example.temu.data.model.HalamanLaporanHilang
import com.example.temu.data.model.JumlahLaporanSaya
import com.example.temu.data.model.LaporanHilang
import com.example.temu.data.model.RiwayatStatus
import com.example.temubarang.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

/**
 * F1 Halaman 2 · Akses Supabase untuk Laporan Saya.
 * - Read daftar per tab : SELECT tabel `reports` dengan owner_id = akun aktif
 * - Jumlah per tab      : RPC jumlah_laporan_saya
 * - Riwayat status      : RPC riwayat_hilang
 * - Ubah status         : RPC ubah_status_hilang (update + insert riwayat, satu transaksi)
 * Detail, edit, dan hapus memakai [LaporanHilangRemoteDataSource] (Halaman 1).
 * Function ada di supabase/f1_halaman2_laporan_saya.sql.
 */
class LaporanSayaRemoteDataSource {

    private val client
        get() = SupabaseClientProvider.client

    private suspend fun idPengguna(): String {
        client.auth.awaitInitialization()
        return requireNotNull(client.auth.currentUserOrNull()?.id) {
            "Sesi berakhir. Silakan masuk kembali."
        }
    }

    suspend fun daftar(status: String, dari: Int, jumlah: Int): HalamanLaporanHilang {
        val uid = idPengguna()

        val hasil = client.from("reports").select(
            columns = Columns.list(
                "id", "owner_id", "item_name", "category",
                "location", "event_date", "status", "photo_path"
            )
        ) {
            count(Count.EXACT)
            filter {
                eq("owner_id", uid)
                eq("report_type", "lost")
                eq("status", status)
                exact("deleted_at", null)
            }
            order("updated_at", Order.DESCENDING)
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

    suspend fun jumlah(): JumlahLaporanSaya {
        val hasil = client.postgrest.rpc("jumlah_laporan_saya")
        val json = Json.parseToJsonElement(hasil.data).jsonObject
        return JumlahLaporanSaya(
            dilaporkan = json.angka("reported"),
            dalamProses = json.angka("in_progress"),
            selesai = json.angka("completed")
        )
    }

    suspend fun riwayat(id: String): List<RiwayatStatus> {
        val hasil = client.postgrest.rpc(
            "riwayat_hilang",
            buildJsonObject { put("p_id", id) }
        )
        return Json.parseToJsonElement(hasil.data).jsonArray.map { elemen ->
            val baris = elemen.jsonObject
            RiwayatStatus(
                dari = baris.teks("dari"),
                ke = baris.teks("ke"),
                catatan = baris.teks("catatan"),
                waktu = baris.teks("waktu")
            )
        }
    }

    /** Mengembalikan nomor versi baru setelah status berubah. */
    suspend fun ubahStatus(id: String, versi: Int, status: String, catatan: String): Int {
        val hasil = client.postgrest.rpc(
            "ubah_status_hilang",
            buildJsonObject {
                put("p_id", id)
                put("p_versi", versi)
                put("p_status", status)
                put("p_catatan", catatan.trim())
            }
        )
        val json = Json.parseToJsonElement(hasil.data).jsonObject
        return (json["versi"] as? JsonPrimitive)?.intOrNull ?: (versi + 1)
    }

    private fun JsonObject.teks(kunci: String): String =
        (this[kunci] as? JsonPrimitive)?.contentOrNull.orEmpty()

    private fun JsonObject.angka(kunci: String): Int =
        (this[kunci] as? JsonPrimitive)?.intOrNull ?: 0
}
