package com.example.temu.viewmodel.laporansaya

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temu.data.model.StatusLaporan
import com.example.temu.data.repository.hilang.LaporanSayaRepository
import com.example.temu.data.repository.hilang.PerubahanHilang
import com.example.temu.data.repository.hilang.PerubahanLaporanHilang
import com.example.temu.ui.screens.laporansaya.LaporanSayaUiState
import com.example.temu.ui.screens.laporansaya.TabLaporanSaya
import com.example.temu.viewmodel.hilang.pesanErrorHilang
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * F1 Halaman 2 · ViewModel layar Laporan Saya (ViewPager 3 tab)
 * Alur: init → muatSemua() → tiap tab: repository.daftar(status) + repository.jumlah() → uiState
 */
class LaporanSayaViewModel : ViewModel() {

    private val repository = LaporanSayaRepository()

    private val _uiState = MutableStateFlow(LaporanSayaUiState())
    val uiState: StateFlow<LaporanSayaUiState> = _uiState.asStateFlow()

    private val jobTab = mutableMapOf<String, Job>()
    private val nomorTab = mutableMapOf<String, Int>()

    init {
        muatSemua()

        // Laporan terbit / diedit / dihapus / status berubah → muat ulang semua tab.
        viewModelScope.launch {
            PerubahanLaporanHilang.kabar.collect { perubahan ->
                when (perubahan.jenis) {
                    PerubahanHilang.Jenis.HAPUS -> _uiState.update { it.copy(pesan = "Laporan dihapus.") }
                    PerubahanHilang.Jenis.STATUS -> _uiState.update { it.copy(pesan = "Status diperbarui.") }
                    else -> Unit
                }
                muatSemua()
            }
        }
    }

    fun muatSemua() {
        muatJumlah()
        StatusLaporan.semua.forEach { muatTab(it) }
    }

    private fun muatJumlah() {
        viewModelScope.launch {
            try {
                val jumlah = repository.jumlah()
                _uiState.update { it.copy(jumlah = jumlah) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Angka tab tetap memakai nilai lama; error ditampilkan oleh tab.
            }
        }
    }

    /** Memuat halaman pertama (20 laporan) satu tab. */
    fun muatTab(status: String) {
        jobTab[status]?.cancel()
        val nomor = (nomorTab[status] ?: 0) + 1
        nomorTab[status] = nomor

        jobTab[status] = viewModelScope.launch {
            ubahTab(status) { it.copy(memuat = true, memuatLagi = false, error = null) }
            try {
                val halaman = repository.daftar(status, dari = 0, jumlah = UKURAN_HALAMAN)
                val url = repository.fotoUrls(halaman.item.map { it.foto })
                if (nomor == nomorTab[status]) {
                    ubahTab(status) { it.copy(daftar = halaman.item, total = halaman.total, urlFoto = url) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (nomor == nomorTab[status]) ubahTab(status) { it.copy(error = pesanErrorHilang(e)) }
            } finally {
                if (nomor == nomorTab[status]) ubahTab(status) { it.copy(memuat = false) }
            }
        }
    }

    /** Pagination per tab: 20 laporan berikutnya saat list mendekati akhir. */
    fun muatBerikutnya(status: String) {
        val tab = _uiState.value.isiTab(status)
        if (tab.memuat || tab.memuatLagi || !tab.masihAdaHalaman) return

        val nomor = nomorTab[status] ?: 0
        ubahTab(status) { it.copy(memuatLagi = true) }
        jobTab[status] = viewModelScope.launch {
            try {
                val halaman = repository.daftar(status, dari = tab.daftar.size, jumlah = UKURAN_HALAMAN)
                val url = repository.fotoUrls(halaman.item.map { it.foto })
                if (nomor == nomorTab[status]) {
                    ubahTab(status) {
                        it.copy(
                            daftar = (it.daftar + halaman.item).distinctBy { laporan -> laporan.id },
                            total = halaman.total,
                            urlFoto = it.urlFoto + url
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (nomor == nomorTab[status]) _uiState.update { it.copy(pesan = pesanErrorHilang(e)) }
            } finally {
                if (nomor == nomorTab[status]) ubahTab(status) { it.copy(memuatLagi = false) }
            }
        }
    }

    fun pesanSudahTampil() {
        _uiState.update { it.copy(pesan = null) }
    }

    private fun ubahTab(status: String, ubah: (TabLaporanSaya) -> TabLaporanSaya) {
        _uiState.update { state ->
            state.copy(tab = state.tab + (status to ubah(state.isiTab(status))))
        }
    }

    private companion object {
        const val UKURAN_HALAMAN = 20
    }
}
