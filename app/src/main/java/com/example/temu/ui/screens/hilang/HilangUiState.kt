package com.example.temu.ui.screens.hilang

import com.example.temu.data.model.DetailHilang
import com.example.temu.data.model.FormHilang
import com.example.temu.data.model.KolomFormHilang
import com.example.temu.data.model.LaporanHilang

/**
 * F1 · State tiap layar dijadikan satu data class.
 * ViewModel mengisi state ini, navigasi mengirimkannya ke Screen.
 * Screen hanya menerima state + aksi (lambda), tidak mengenal ViewModel.
 */

/** State layar Jelajah · Hilang (JelajahHilangScreen). */
data class JelajahHilangUiState(
    val kataKunci: String = "",
    val kategori: String? = null,
    val statusDipilih: Set<String> = emptySet(),
    val daftar: List<LaporanHilang> = emptyList(),
    val urlFoto: Map<String, String> = emptyMap(), // path foto → signed URL
    val totalHasil: Int = 0,
    val memuat: Boolean = false,
    val memuatLagi: Boolean = false,
    val error: String? = null,
    val pesan: String? = null // snackbar sekali tampil
) {
    val adaPencarianAtauFilter: Boolean
        get() = kataKunci.isNotBlank() || kategori != null || statusDipilih.isNotEmpty()

    /** Angka pada tombol filter: kategori (bila dipilih) + jumlah status. */
    val jumlahFilterAktif: Int
        get() = (if (kategori != null) 1 else 0) + statusDipilih.size

    val masihAdaHalaman: Boolean
        get() = daftar.size < totalHasil
}

/** State layar Detail laporan hilang (DetailHilangScreen). */
data class DetailHilangUiState(
    val detail: DetailHilang? = null,
    val urlFoto: String? = null,
    val memuat: Boolean = true,
    val error: String? = null,
    val tidakTersedia: Boolean = false,
    val menghapus: Boolean = false,
    val hapusSelesai: Boolean = false,
    val pesan: String? = null
)

/** State layar Form tambah / edit laporan hilang (FormHilangScreen). */
data class FormHilangUiState(
    val modeEdit: Boolean = false,
    val form: FormHilang = FormHilang(),
    val formAwal: FormHilang = FormHilang(),
    val urlFotoLama: String? = null,
    val errorKolom: Map<KolomFormHilang, String> = emptyMap(),
    val errorForm: String? = null,
    val konflikVersi: Boolean = false,
    val memuat: Boolean = false,
    val mengirim: Boolean = false,
    val idTerbit: String? = null,   // terisi setelah laporan baru terbit → buka detail
    val editSelesai: Boolean = false
) {
    val formBerubah: Boolean
        get() = form != formAwal
}
