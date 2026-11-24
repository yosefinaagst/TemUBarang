package com.example.temu.data.repository.hilang

import com.example.temu.data.model.DetailHilang
import com.example.temu.data.model.HalamanLaporanHilang
import com.example.temu.data.model.JumlahLaporanSaya
import com.example.temu.data.model.RiwayatStatus
import com.example.temu.data.remote.LaporanSayaRemoteDataSource

/**
 * F1 Halaman 2 · Repository Laporan Saya.
 * Detail, foto, dan hapus diteruskan ke [LaporanHilangRepository] (Halaman 1)
 * supaya kabar perubahan ([PerubahanLaporanHilang]) tetap satu sumber,
 * sehingga perubahan di Halaman 2 langsung terbaca di Halaman 1 (F1-AC7).
 */
class LaporanSayaRepository(
    private val remote: LaporanSayaRemoteDataSource = LaporanSayaRemoteDataSource(),
    private val hilang: LaporanHilangRepository = LaporanHilangRepository()
) {
    suspend fun daftar(status: String, dari: Int, jumlah: Int): HalamanLaporanHilang =
        remote.daftar(status, dari, jumlah)

    suspend fun jumlah(): JumlahLaporanSaya = remote.jumlah()

    suspend fun detail(id: String): DetailHilang = hilang.detail(id)

    suspend fun riwayat(id: String): List<RiwayatStatus> = remote.riwayat(id)

    suspend fun fotoUrl(path: String): String? = hilang.fotoUrl(path)

    suspend fun fotoUrls(paths: Collection<String>): Map<String, String> = hilang.fotoUrls(paths)

    suspend fun hapus(id: String) = hilang.hapus(id)

    suspend fun ubahStatus(id: String, versi: Int, status: String, catatan: String): Int {
        val versiBaru = remote.ubahStatus(id, versi, status, catatan)
        PerubahanLaporanHilang.kirim(PerubahanHilang(PerubahanHilang.Jenis.STATUS, id))
        return versiBaru
    }
}
