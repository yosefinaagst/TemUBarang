package com.example.temu.data.repository.tersimpan

import android.content.Context
import com.example.temu.data.local.TersimpanLokal
import com.example.temu.data.model.LaporanTersimpan
import com.example.temu.data.model.PilihanLaporan
import com.example.temu.data.remote.TersimpanRemoteDataSource
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Kabar perubahan daftar tersimpan.
 * Contoh: setelah laporan dihapus di halaman Detail Tersimpan,
 * ViewModel Daftar Tersimpan mendengar kabar ini lalu memuat ulang grid.
 */
object PerubahanTersimpan {
    private val _kabar = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val kabar: SharedFlow<String> = _kabar.asSharedFlow()

    fun kirim(reportId: String) {
        _kabar.tryEmit(reportId)
    }
}

/** Kunci peta URL foto. Bucket ikut dipakai karena foto hilang & ditemukan beda bucket. */
fun kunciFoto(bucket: String, path: String): String = "$bucket/$path"

/**
 * F4 · Repository Laporan Tersimpan. Setiap ViewModel F4 hanya berbicara dengan kelas ini.
 * - Daftar id yang disimpan  → lokal di HP  (TersimpanLokal)
 * - Isi laporan & foto       → Supabase     (TersimpanRemoteDataSource, tabel reports)
 */
class TersimpanRepository(
    context: Context,
    private val remote: TersimpanRemoteDataSource = TersimpanRemoteDataSource()
) {
    private val lokal = TersimpanLokal(context)

    /** Read: semua laporan tersimpan, yang terbaru disimpan di atas. */
    suspend fun daftar(): List<LaporanTersimpan> {
        val uid = remote.idPengguna()
        val tersimpan = lokal.semua(uid)
        val dataLaporan = remote.laporan(tersimpan.map { it.first })
        return tersimpan.map { (id, waktu) -> gabung(id, waktu, dataLaporan[id]) }
    }

    /** Read: satu laporan tersimpan. Null bila laporan ini tidak ada di daftar tersimpan. */
    suspend fun detail(reportId: String): LaporanTersimpan? {
        val uid = remote.idPengguna()
        val waktu = lokal.waktuDisimpan(uid, reportId) ?: return null
        return gabung(reportId, waktu, remote.laporan(listOf(reportId))[reportId])
    }

    suspend fun idTersimpan(): Set<String> =
        lokal.semua(remote.idPengguna()).map { it.first }.toSet()

    suspend fun pilihan(jenis: String?): List<PilihanLaporan> = remote.pilihan(jenis)

    /** Insert */
    suspend fun simpan(reportId: String) {
        lokal.simpan(remote.idPengguna(), reportId)
        PerubahanTersimpan.kirim(reportId)
    }

    /** Delete */
    suspend fun hapus(reportId: String) {
        lokal.hapus(remote.idPengguna(), reportId)
        PerubahanTersimpan.kirim(reportId)
    }

    /** Laporan yang sudah dihapus pemiliknya tetap tampil sebagai "tidak tersedia". */
    private fun gabung(id: String, waktu: Long, data: LaporanTersimpan?): LaporanTersimpan {
        val disimpanPada = Instant.ofEpochMilli(waktu)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toString()

        return data?.copy(disimpanPada = disimpanPada)
            ?: LaporanTersimpan(
                reportId = id,
                jenis = "",
                nama = "Laporan tidak tersedia",
                kategori = "",
                deskripsi = "",
                lokasi = "",
                tanggal = "",
                status = "",
                foto = "",
                disimpanPada = disimpanPada,
                tersedia = false
            )
    }

    /**
     * Signed URL untuk banyak foto sekaligus (dipanggil paralel).
     * Masukan: pasangan (bucket, path). Hasil: kunciFoto(bucket, path) → URL.
     * URL disimpan sementara (50 menit) karena berlaku 1 jam.
     */
    suspend fun fotoUrls(foto: Collection<Pair<String, String>>): Map<String, String> =
        coroutineScope {
            val sekarang = System.currentTimeMillis()
            foto.filter { it.second.isNotBlank() }
                .distinct()
                .map { (bucket, path) ->
                    async {
                        val kunci = kunciFoto(bucket, path)
                        val tersimpan = cacheUrl[kunci]
                        if (tersimpan != null && sekarang - tersimpan.second < MASA_CACHE_MS) {
                            kunci to tersimpan.first
                        } else {
                            try {
                                val url = remote.fotoUrl(bucket, path)
                                cacheUrl[kunci] = url to sekarang
                                kunci to url
                            } catch (e: CancellationException) {
                                throw e
                            } catch (_: Exception) {
                                null
                            }
                        }
                    }
                }
                .awaitAll()
                .filterNotNull()
                .toMap()
        }

    private companion object {
        const val MASA_CACHE_MS = 50 * 60 * 1000L
        val cacheUrl = ConcurrentHashMap<String, Pair<String, Long>>()
    }
}
