package com.example.temu.ui.screens.laporansaya

import com.example.temu.data.model.DetailHilang
import com.example.temu.data.model.JumlahLaporanSaya
import com.example.temu.data.model.LaporanHilang
import com.example.temu.data.model.RiwayatStatus
import com.example.temu.data.model.StatusLaporan

/**
 * F1 Halaman 2 · State tiap layar Laporan Saya.
 * ViewModel mengisi state, navigasi mengirimkannya ke Screen.
 */

/** Isi satu tab (satu LazyColumn) di ViewPager. */
data class TabLaporanSaya(
    val daftar: List<LaporanHilang> = emptyList(),
    val urlFoto: Map<String, String> = emptyMap(),
    val total: Int = 0,
    val memuat: Boolean = true,
    val memuatLagi: Boolean = false,
    val error: String? = null
) {
    val masihAdaHalaman: Boolean
        get() = daftar.size < total
}

/** State layar daftar Laporan Saya (layar 07). */
data class LaporanSayaUiState(
    val tab: Map<String, TabLaporanSaya> = StatusLaporan.semua.associateWith { TabLaporanSaya() },
    val jumlah: JumlahLaporanSaya = JumlahLaporanSaya(),
    val pesan: String? = null
) {
    fun isiTab(status: String): TabLaporanSaya = tab[status] ?: TabLaporanSaya()
}

/** State layar detail + riwayat (layar 08). */
data class DetailLaporanSayaUiState(
    val detail: DetailHilang? = null,
    val riwayat: List<RiwayatStatus> = emptyList(),
    val urlFoto: String? = null,
    val memuat: Boolean = true,
    val error: String? = null,
    val tidakTersedia: Boolean = false,
    val menghapus: Boolean = false,
    val hapusSelesai: Boolean = false,
    val pesan: String? = null
)

/** State layar Ubah status (layar 09). */
data class UbahStatusUiState(
    val namaBarang: String = "",
    val statusSaatIni: String = "",
    val versi: Int = 0,
    val statusBaru: String? = null,
    val catatan: String = "",
    val errorStatus: String? = null,
    val errorCatatan: String? = null,
    val errorForm: String? = null,
    val konflikVersi: Boolean = false,
    val memuat: Boolean = true,
    val mengirim: Boolean = false,
    val statusTersimpan: String? = null // terisi setelah berhasil → kembali ke tab ini
)
