package com.example.temu.data.model

/**
 * F1 Halaman 2 · Laporan Saya
 * Model tambahan untuk daftar laporan milik sendiri, riwayat, dan ubah status.
 * Ringkasan laporan memakai [LaporanHilang] dan detail memakai [DetailHilang] dari Halaman 1.
 */

/** Satu baris riwayat status (tabel status_history). */
data class RiwayatStatus(
    val dari: String,     // kosong untuk riwayat awal
    val ke: String,
    val catatan: String,
    val waktu: String     // timestamp ISO dari database
)

/** Angka pada tiap tab Laporan Saya. */
data class JumlahLaporanSaya(
    val dilaporkan: Int = 0,
    val dalamProses: Int = 0,
    val selesai: Int = 0
) {
    fun untuk(status: String): Int = when (status) {
        StatusLaporan.DILAPORKAN -> dilaporkan
        StatusLaporan.DALAM_PROSES -> dalamProses
        else -> selesai
    }
}

/** Aturan perpindahan status (sama dengan function ubah_status_hilang). */
object AturanStatus {
    const val CATATAN_MIN = 5
    const val CATATAN_MAKS = 300

    /** Status tujuan yang boleh dipilih dari [dari]. Selesai = terminal. */
    fun tujuan(dari: String): List<String> = when (dari) {
        StatusLaporan.DILAPORKAN -> listOf(StatusLaporan.DALAM_PROSES, StatusLaporan.SELESAI)
        StatusLaporan.DALAM_PROSES -> listOf(StatusLaporan.DILAPORKAN, StatusLaporan.SELESAI)
        else -> emptyList()
    }
}
