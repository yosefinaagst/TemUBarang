package com.example.temu.viewmodel.hilang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temu.data.repository.hilang.LaporanHilangRepository
import com.example.temu.data.repository.hilang.PerubahanHilang
import com.example.temu.data.repository.hilang.PerubahanLaporanHilang
import com.example.temu.ui.screens.hilang.JelajahHilangUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * F1 Halaman 1 · ViewModel layar Jelajah · Hilang
 * Alur: init → muat() → repository.daftar() → uiState
 */
class JelajahHilangViewModel : ViewModel() {

    private val repository = LaporanHilangRepository()

    private val _uiState = MutableStateFlow(JelajahHilangUiState())
    val uiState: StateFlow<JelajahHilangUiState> = _uiState.asStateFlow()

    private var jobDaftar: Job? = null
    private var nomorPermintaan = 0

    init {
        muat()

        // Muat ulang bila ada laporan yang terbit / diedit / dihapus di layar lain.
        viewModelScope.launch {
            PerubahanLaporanHilang.kabar.collect { perubahan ->
                if (perubahan.jenis == PerubahanHilang.Jenis.HAPUS) {
                    _uiState.update { it.copy(pesan = "Laporan dihapus.") }
                }
                muat()
            }
        }
    }

    fun ubahKataKunci(nilai: String) {
        _uiState.update { it.copy(kataKunci = nilai) }
        muat(tunda = true) // tunggu pengguna selesai mengetik
    }

    fun pilihKategori(nilai: String?) {
        if (_uiState.value.kategori == nilai) return
        _uiState.update { it.copy(kategori = nilai) }
        muat()
    }

    fun terapkanFilter(kategori: String?, status: Set<String>) {
        _uiState.update { it.copy(kategori = kategori, statusDipilih = status) }
        muat()
    }

    fun resetPencarianDanFilter() {
        _uiState.update { it.copy(kataKunci = "", kategori = null, statusDipilih = emptySet()) }
        muat()
    }

    fun pesanSudahTampil() {
        _uiState.update { it.copy(pesan = null) }
    }

    /** Memuat halaman pertama (20 laporan) sesuai pencarian & filter. */
    fun muat(tunda: Boolean = false) {
        jobDaftar?.cancel()
        val nomor = ++nomorPermintaan
        jobDaftar = viewModelScope.launch {
            if (tunda) delay(350)
            _uiState.update { it.copy(memuat = true, memuatLagi = false, error = null) }
            try {
                val filter = _uiState.value
                val halaman = repository.daftar(
                    kataKunci = filter.kataKunci,
                    kategori = filter.kategori,
                    status = filter.statusDipilih,
                    dari = 0,
                    jumlah = UKURAN_HALAMAN
                )
                val url = repository.fotoUrls(halaman.item.map { it.foto })
                if (nomor == nomorPermintaan) {
                    _uiState.update {
                        it.copy(daftar = halaman.item, totalHasil = halaman.total, urlFoto = url)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (nomor == nomorPermintaan) _uiState.update { it.copy(error = pesanErrorHilang(e)) }
            } finally {
                if (nomor == nomorPermintaan) _uiState.update { it.copy(memuat = false) }
            }
        }
    }

    /** Pagination: memuat 20 laporan berikutnya saat grid mendekati akhir. */
    fun muatBerikutnya() {
        val sekarang = _uiState.value
        if (sekarang.memuat || sekarang.memuatLagi || !sekarang.masihAdaHalaman || sekarang.error != null) return

        val nomor = nomorPermintaan
        _uiState.update { it.copy(memuatLagi = true) }
        jobDaftar = viewModelScope.launch {
            try {
                val halaman = repository.daftar(
                    kataKunci = sekarang.kataKunci,
                    kategori = sekarang.kategori,
                    status = sekarang.statusDipilih,
                    dari = sekarang.daftar.size,
                    jumlah = UKURAN_HALAMAN
                )
                val url = repository.fotoUrls(halaman.item.map { it.foto })
                if (nomor == nomorPermintaan) {
                    _uiState.update {
                        it.copy(
                            daftar = (it.daftar + halaman.item).distinctBy { laporan -> laporan.id },
                            totalHasil = halaman.total,
                            urlFoto = it.urlFoto + url
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (nomor == nomorPermintaan) _uiState.update { it.copy(pesan = pesanErrorHilang(e)) }
            } finally {
                if (nomor == nomorPermintaan) _uiState.update { it.copy(memuatLagi = false) }
            }
        }
    }

    private companion object {
        const val UKURAN_HALAMAN = 20
    }
}
