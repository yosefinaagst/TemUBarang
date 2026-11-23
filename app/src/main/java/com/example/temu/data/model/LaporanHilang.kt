package com.example.temu.data.model

import java.time.LocalDate
import java.time.ZoneId

/**
 * F1 · Laporan Barang Hilang
 * Model data untuk tabel `reports` dengan report_type = 'lost'.
 * Label kategori memakai [kategoriBarang] yang sama dengan F3.
 */

/** Ringkasan satu laporan untuk kartu di grid Jelajah · Hilang. */
data class LaporanHilang(
    val id: String,
    val ownerId: String,
    val nama: String,
    val kategori: String,
    val lokasi: String,
    val tanggal: String, // format yyyy-MM-dd
    val status: String,
    val foto: String = "" // path di bucket foto-hilang, kosong = tanpa foto
)

/** Satu halaman hasil query (pagination 20 item) beserta total hasil. */
data class HalamanLaporanHilang(
    val item: List<LaporanHilang>,
    val total: Int
)

/** Data lengkap untuk halaman detail, termasuk nama dan kontak pelapor. */
data class DetailHilang(
    val id: String,
    val nama: String,
    val kategori: String,
    val deskripsi: String,
    val ciriPublik: String,
    val lokasi: String,
    val tanggal: String,
    val foto: String,
    val status: String,
    val versi: Int,
    val milikSaya: Boolean,
    val namaPelapor: String,
    val kontak: String
)

/** Isian form tambah / edit laporan hilang. */
data class FormHilang(
    val nama: String = "",
    val kategori: String = "",
    val deskripsi: String = "",
    val lokasi: String = "",
    val tanggal: LocalDate? = null,
    val ciriPublik: String = "",
    val fotoLama: String = "",      // path foto yang sudah tersimpan (mode edit)
    val fotoBaru: String? = null    // Uri foto baru dari galeri (belum diunggah)
)

/** Kolom wajib pada form, dipakai untuk menampilkan pesan error per kolom. */
enum class KolomFormHilang { NAMA, KATEGORI, DESKRIPSI, LOKASI, TANGGAL }

/** Nilai kolom `status` di database. */
object StatusLaporan {
    const val DILAPORKAN = "reported"
    const val DALAM_PROSES = "in_progress"
    const val SELESAI = "completed"

    val semua = listOf(DILAPORKAN, DALAM_PROSES, SELESAI)
}

/** Batas panjang isian (sama dengan validasi di function Supabase). */
object BatasFormHilang {
    const val NAMA = 80
    const val DESKRIPSI = 500
    const val CIRI = 300
}

/** Zona waktu kampus untuk validasi "tanggal tidak boleh melewati hari ini". */
val ZONA_WAKTU_KAMPUS: ZoneId = ZoneId.of("Asia/Jakarta")
