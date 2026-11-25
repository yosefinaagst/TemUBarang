package com.example.temu.data.model

/**
 * F4 · Laporan Tersimpan
 * Model data laporan tersimpan: id disimpan di HP (data/local/TersimpanLokal.kt),
 * isi laporannya dibaca dari tabel `reports` di Supabase.
 */

/** Nilai kolom `report_type` di tabel reports. */
object JenisLaporan {
    const val HILANG = "lost"
    const val DITEMUKAN = "found"
}

/** Bucket Storage tempat foto laporan disimpan, tergantung jenis laporannya. */
fun bucketFoto(jenis: String): String =
    if (jenis == JenisLaporan.HILANG) "foto-hilang" else "foto-temuan"

/** Satu laporan di daftar tersimpan milik pengguna. */
data class LaporanTersimpan(
    val reportId: String,
    val jenis: String,          // lost / found
    val nama: String,
    val kategori: String,
    val deskripsi: String,
    val lokasi: String,
    val tanggal: String,        // format yyyy-MM-dd
    val status: String,
    val foto: String,           // path foto di bucket, kosong = tanpa foto
    val disimpanPada: String,   // tanggal laporan disimpan (yyyy-MM-dd)
    val tersedia: Boolean       // false bila laporan aslinya sudah dihapus pemiliknya
) {
    val hilang: Boolean get() = jenis == JenisLaporan.HILANG
}

/** Satu laporan di halaman "Tambah ke Tersimpan" (pilihan dari semua laporan). */
data class PilihanLaporan(
    val id: String,
    val jenis: String,
    val nama: String,
    val lokasi: String,
    val tanggal: String,
    val foto: String
) {
    val hilang: Boolean get() = jenis == JenisLaporan.HILANG
}
