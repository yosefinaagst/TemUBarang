package com.example.temu.data.local

import android.content.Context

/**
 * F4 · Penyimpanan lokal daftar Laporan Tersimpan (di HP, pakai SharedPreferences).
 *
 * Yang disimpan hanya id laporan + waktu disimpan, dipisah per akun:
 *   kunci "tersimpan_<idPengguna>" → { "<reportId>;<waktuMillis>", ... }
 * Data laporannya sendiri tetap dibaca dari tabel `reports` di Supabase.
 *
 * Catatan: daftar ini hilang bila aplikasi di-uninstall / data aplikasi dihapus,
 * dan tidak ikut pindah ke HP lain.
 */
class TersimpanLokal(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(NAMA_FILE, Context.MODE_PRIVATE)

    /** Semua laporan tersimpan milik pengguna: (reportId, waktuMillis), terbaru di atas. */
    @Synchronized
    fun semua(idPengguna: String): List<Pair<String, Long>> =
        baca(idPengguna)
            .mapNotNull { entri ->
                val bagian = entri.split(PEMISAH)
                val id = bagian.getOrNull(0).orEmpty()
                val waktu = bagian.getOrNull(1)?.toLongOrNull() ?: 0L
                if (id.isBlank()) null else id to waktu
            }
            .sortedByDescending { it.second }

    @Synchronized
    fun waktuDisimpan(idPengguna: String, reportId: String): Long? =
        semua(idPengguna).firstOrNull { it.first == reportId }?.second

    /** Insert: menambahkan laporan. Bila sudah ada, tidak terjadi apa-apa. */
    @Synchronized
    fun simpan(idPengguna: String, reportId: String) {
        if (waktuDisimpan(idPengguna, reportId) != null) return
        val baru = baca(idPengguna) + "$reportId$PEMISAH${System.currentTimeMillis()}"
        tulis(idPengguna, baru)
    }

    /** Delete: menghapus laporan dari daftar tersimpan. */
    @Synchronized
    fun hapus(idPengguna: String, reportId: String) {
        val sisa = baca(idPengguna).filterNot { it.split(PEMISAH).firstOrNull() == reportId }
        tulis(idPengguna, sisa.toSet())
    }

    // Salinan baru, karena set dari getStringSet tidak boleh diubah langsung.
    private fun baca(idPengguna: String): Set<String> =
        prefs.getStringSet(kunci(idPengguna), emptySet())?.toSet() ?: emptySet()

    private fun tulis(idPengguna: String, isi: Set<String>) {
        prefs.edit().putStringSet(kunci(idPengguna), isi).apply()
    }

    private fun kunci(idPengguna: String) = "tersimpan_$idPengguna"

    private companion object {
        const val NAMA_FILE = "f4_laporan_tersimpan"
        const val PEMISAH = ";"
    }
}
