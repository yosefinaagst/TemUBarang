package com.example.temu.data.repository.hilang

import com.example.temu.data.model.DetailHilang
import com.example.temu.data.model.FormHilang
import com.example.temu.data.model.HalamanLaporanHilang
import com.example.temu.data.remote.FotoUnggah
import com.example.temu.data.remote.LaporanHilangRemoteDataSource
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Jenis perubahan data laporan hilang yang perlu diketahui layar lain. */
data class PerubahanHilang(val jenis: Jenis, val id: String) {
    enum class Jenis { TERBIT, EDIT, HAPUS }
}

/**
 * Kabar perubahan data laporan hilang.
 * Contoh: setelah laporan dihapus di halaman Detail, ViewModel Jelajah
 * mendengar kabar ini lalu memuat ulang daftar.
 */
object PerubahanLaporanHilang {
    private val _kabar = MutableSharedFlow<PerubahanHilang>(extraBufferCapacity = 8)
    val kabar: SharedFlow<PerubahanHilang> = _kabar.asSharedFlow()

    fun kirim(perubahan: PerubahanHilang) {
        _kabar.tryEmit(perubahan)
    }
}

/** F1 · Repository laporan hilang. Setiap ViewModel F1 hanya berbicara dengan kelas ini. */
class LaporanHilangRepository(
    private val remote: LaporanHilangRemoteDataSource = LaporanHilangRemoteDataSource()
) {
    suspend fun idPengguna(): String = remote.idPengguna()

    suspend fun daftar(
        kataKunci: String,
        kategori: String?,
        status: Set<String>,
        dari: Int,
        jumlah: Int
    ): HalamanLaporanHilang = remote.daftar(kataKunci, kategori, status, dari, jumlah)

    suspend fun detail(id: String): DetailHilang = remote.detail(id)

    /** Signed URL untuk satu foto; null bila gagal dibuat. */
    suspend fun fotoUrl(path: String): String? = fotoUrls(listOf(path))[path]

    /**
     * Signed URL untuk banyak foto sekaligus (dipanggil paralel).
     * URL disimpan sementara (50 menit) karena berlaku 1 jam.
     */
    suspend fun fotoUrls(paths: Collection<String>): Map<String, String> = coroutineScope {
        val sekarang = System.currentTimeMillis()
        paths.filter { it.isNotBlank() }
            .distinct()
            .map { path ->
                async {
                    val tersimpan = cacheUrl[path]
                    if (tersimpan != null && sekarang - tersimpan.second < MASA_CACHE_MS) {
                        path to tersimpan.first
                    } else {
                        try {
                            val url = remote.fotoUrl(path)
                            cacheUrl[path] = url to sekarang
                            path to url
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

    suspend fun simpan(id: String, form: FormHilang, foto: FotoUnggah?): String {
        val hasil = remote.simpan(id, form, foto)
        PerubahanLaporanHilang.kirim(PerubahanHilang(PerubahanHilang.Jenis.TERBIT, hasil))
        return hasil
    }

    suspend fun perbarui(
        id: String,
        versi: Int,
        form: FormHilang,
        fotoBaru: FotoUnggah?,
        hapusFoto: Boolean
    ): Int {
        val versiBaru = remote.perbarui(id, versi, form, fotoBaru, hapusFoto)
        PerubahanLaporanHilang.kirim(PerubahanHilang(PerubahanHilang.Jenis.EDIT, id))
        return versiBaru
    }

    suspend fun hapus(id: String) {
        remote.hapus(id)
        PerubahanLaporanHilang.kirim(PerubahanHilang(PerubahanHilang.Jenis.HAPUS, id))
    }

    private companion object {
        const val MASA_CACHE_MS = 50 * 60 * 1000L
        val cacheUrl = ConcurrentHashMap<String, Pair<String, Long>>()
    }
}
