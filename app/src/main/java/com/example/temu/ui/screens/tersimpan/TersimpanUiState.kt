package com.example.temu.ui.screens.tersimpan

import com.example.temu.data.model.LaporanTersimpan
import com.example.temu.data.model.PilihanLaporan

/**
 * F4 · State tiap layar Laporan Tersimpan dijadikan satu data class.
 * ViewModel mengisi state ini, navigasi mengirimkannya ke Screen.
 * Screen hanya menerima state + aksi (lambda), tidak mengenal ViewModel.
 */

/** State layar Daftar Tersimpan (grid). */
data class DaftarTersimpanUiState(
    val daftar: List<LaporanTersimpan> = emptyList(),
    val urlFoto: Map<String, String> = emptyMap(), // kunciFoto(bucket, path) → signed URL
    val memuat: Boolean = true,
    val error: String? = null,
    val pesan: String? = null // snackbar sekali tampil
)

/** State layar Pilih Laporan ("Tambah ke Tersimpan"). */
data class PilihLaporanUiState(
    val jenis: String? = null,                    // null = semua, lost, found
    val daftar: List<PilihanLaporan> = emptyList(),
    val idTersimpan: Set<String> = emptySet(),     // laporan yang sudah ada di Tersimpan
    val sedangDisimpan: Set<String> = emptySet(),  // laporan yang sedang dikirim ke database
    val urlFoto: Map<String, String> = emptyMap(),
    val memuat: Boolean = true,
    val error: String? = null,
    val pesan: String? = null
)

/** State layar Detail Tersimpan. */
data class DetailTersimpanUiState(
    val detail: LaporanTersimpan? = null,
    val urlFoto: String? = null,
    val memuat: Boolean = true,
    val error: String? = null,
    val tidakDitemukan: Boolean = false,
    val menghapus: Boolean = false,
    val hapusSelesai: Boolean = false,
    val pesan: String? = null
)
