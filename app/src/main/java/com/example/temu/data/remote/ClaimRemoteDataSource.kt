package com.example.temu.data.remote

import com.example.temu.data.model.Claim
import com.example.temu.data.model.LaporanDiklaim
import com.example.temubarang.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.hours

class ClaimRemoteDataSource {

    private val client
        get() = SupabaseClientProvider.client

    private val bucketFoto
        get() = client.storage.from("foto-temuan")

    private suspend fun idPengguna(): String {
        client.auth.awaitInitialization()
        return requireNotNull(client.auth.currentUserOrNull()?.id) {
            "Sesi pengguna tidak ditemukan. Silakan login kembali."
        }
    }

    /**
     * Ubah satu baris JSON dari tabel claims menjadi [Claim].
     * Dipetakan manual (sama seperti TemuanRemoteDataSource),
     * jadi tidak bergantung pada plugin kotlinx-serialization.
     */
    private fun JsonObject.keClaim(): Claim {
        fun s(key: String) = this[key]?.jsonPrimitive?.contentOrNull
        val laporan = this["reports"] as? JsonObject
        fun l(key: String) = laporan?.get(key)?.jsonPrimitive?.contentOrNull

        return Claim(
            id = s("id"),
            reportId = s("report_id").orEmpty(),
            claimantId = s("claimant_id"),
            ciriKhusus = s(KOLOM_CIRI).orEmpty(),
            description = s(KOLOM_KETERANGAN),
            status = s("status"),
            createdAt = s("created_at"),
            reports = laporan?.let {
                LaporanDiklaim(
                    namaBarang = l("item_name"),
                    location = l("location"),
                    tanggal = l("event_date"),
                    fotoPath = l("photo_path"),
                    status = l("status")
                )
            }
        )
    }

    // Kolom laporan yang ikut diambil bersama klaim (join ke tabel reports)
    private val kolomDenganLaporan =
        Columns.raw("*, reports(item_name, location, event_date, photo_path, status)")

    // ---------------- Laporan yang diklaim ----------------

    suspend fun ringkasanLaporan(reportId: String): RingkasanLaporan {
        val row = ambilLaporan(reportId)

        fun v(key: String) = row[key]?.jsonPrimitive?.contentOrNull.orEmpty()

        return RingkasanLaporan(
            nama = v("item_name"),
            lokasi = v("location"),
            tanggal = v("event_date"),
            fotoUrl = fotoUrl(v("photo_path")),
            status = v("status")
        )
    }

    suspend fun fotoUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return runCatching { bucketFoto.createSignedUrl(path, 1.hours) }.getOrNull()
    }

    private suspend fun ambilLaporan(reportId: String): JsonObject =
        client.from("reports").select {
            filter {
                eq("id", reportId)
                eq("report_type", "found")
                exact("deleted_at", null)
            }
        }.decodeList<JsonObject>().firstOrNull()
            ?: throw IllegalStateException("Laporan sudah tidak tersedia.")

    private suspend fun pastikanBisaDiubah(reportId: String) {
        val laporan = ambilLaporan(reportId)
        require(laporan["status"]?.jsonPrimitive?.contentOrNull != STATUS_SELESAI) {
            "Laporan sudah selesai, klaim tidak bisa diajukan atau diubah."
        }
    }

    // ---------------- Ajukan (Form klaim) ----------------

    suspend fun ajukan(reportId: String, ciriKhusus: String, keterangan: String?): Claim {
        val uid = idPengguna()
        val laporan = ambilLaporan(reportId)

        require(laporan["owner_id"]?.jsonPrimitive?.contentOrNull != uid) {
            "Kamu tidak bisa mengklaim laporanmu sendiri."
        }
        require(laporan["status"]?.jsonPrimitive?.contentOrNull != STATUS_SELESAI) {
            "Laporan sudah selesai, tidak bisa diklaim."
        }

        // F2-AC3: klaim duplikat -> arahkan ke klaim yang sudah ada
        klaimSayaUntuk(reportId)?.let { throw KlaimSudahAda(it.id.orEmpty()) }

        val payload = buildJsonObject {
            put("report_id", reportId)
            put("claimant_id", uid)
            put(KOLOM_CIRI, ciriKhusus.trim())
            put(KOLOM_KETERANGAN, keterangan?.trim()?.ifBlank { null })
        }

        return client.from("claims").insert(payload) {
            select()
        }.decodeSingle<JsonObject>().keClaim()
    }

    suspend fun klaimSayaUntuk(reportId: String): Claim? {
        val uid = idPengguna()
        return client.from("claims").select {
            filter {
                eq("report_id", reportId)
                eq("claimant_id", uid)
            }
        }.decodeList<JsonObject>().map { it.keClaim() }.firstOrNull()
    }

    // ---------------- Klaim Saya (daftar) ----------------

    suspend fun daftarMilikSaya(): List<Claim> {
        val uid = idPengguna()
        return client.from("claims").select(columns = kolomDenganLaporan) {
            filter { eq("claimant_id", uid) }
            order("created_at", Order.DESCENDING)
        }.decodeList<JsonObject>().map { it.keClaim() }
    }

    // ---------------- Detail klaim ----------------

    suspend fun detail(id: String): Claim {
        val uid = idPengguna()
        return client.from("claims").select(columns = kolomDenganLaporan) {
            filter {
                eq("id", id)
                eq("claimant_id", uid)
            }
        }.decodeList<JsonObject>().map { it.keClaim() }.firstOrNull()
            ?: throw IllegalStateException("Klaim tidak ditemukan atau bukan milikmu.")
    }

    // ---------------- Edit / Hapus ----------------

    suspend fun ubah(id: String, ciriKhusus: String, keterangan: String?): Claim {
        val uid = idPengguna()
        require(ciriKhusus.isNotBlank()) { "Ciri khusus wajib diisi." }

        val lama = detail(id)
        pastikanBisaDiubah(lama.reportId)

        val payload = buildJsonObject {
            put(KOLOM_CIRI, ciriKhusus.trim())
            put(KOLOM_KETERANGAN, keterangan?.trim()?.ifBlank { null })
        }

        return client.from("claims").update(payload) {
            select()
            filter {
                eq("id", id)
                eq("claimant_id", uid)
            }
        }.decodeList<JsonObject>().map { it.keClaim() }.firstOrNull()
            ?: throw IllegalStateException("Klaim gagal diperbarui. Periksa izin akses akun.")
    }

    suspend fun hapus(id: String) {
        val uid = idPengguna()

        val lama = detail(id)
        pastikanBisaDiubah(lama.reportId)

        client.from("claims").delete {
            filter {
                eq("id", id)
                eq("claimant_id", uid)
            }
        }
    }

    companion object {
        const val STATUS_SELESAI = "completed"

        // Nama kolom di tabel claims (Supabase)
        private const val KOLOM_CIRI = "special_characteristics"
        private const val KOLOM_KETERANGAN = "supporting_description"
    }
}

data class RingkasanLaporan(
    val nama: String,
    val lokasi: String,
    val tanggal: String,
    val fotoUrl: String?,
    val status: String
)

class KlaimSudahAda(val klaimId: String) :
    Exception("Kamu sudah mengajukan klaim untuk barang ini.")