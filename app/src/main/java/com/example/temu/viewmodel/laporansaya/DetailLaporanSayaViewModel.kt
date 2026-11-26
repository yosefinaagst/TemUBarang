package com.example.temu.viewmodel.laporansaya

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temu.data.repository.hilang.LaporanSayaRepository
import com.example.temu.data.repository.hilang.PerubahanHilang
import com.example.temu.data.repository.hilang.PerubahanLaporanHilang
import com.example.temu.ui.screens.laporansaya.DetailLaporanSayaUiState
import com.example.temu.viewmodel.hilang.kodeErrorHilang
import com.example.temu.viewmodel.hilang.pesanErrorHilang
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * F1 Halaman 2 · ViewModel layar Detail laporan milik sendiri + riwayat status
 * Id diambil dari argumen navigasi "laporan_saya/detail/{id}".
 * Alur: init → muat() → repository.detail(id) + repository.riwayat(id) → uiState
 */
class DetailLaporanSayaViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = LaporanSayaRepository()
    private val id: String = savedStateHandle.get<String>("id").orEmpty()

    private val _uiState = MutableStateFlow(DetailLaporanSayaUiState())
    val uiState: StateFlow<DetailLaporanSayaUiState> = _uiState.asStateFlow()

    init {
        muat()

        // Setelah edit / ubah status laporan ini, muat ulang detail dan riwayat.
        viewModelScope.launch {
            PerubahanLaporanHilang.kabar.collect { perubahan ->
                if (perubahan.id != id) return@collect
                when (perubahan.jenis) {
                    PerubahanHilang.Jenis.EDIT -> {
                        _uiState.update { it.copy(pesan = "Perubahan disimpan.") }
                        muat()
                    }
                    PerubahanHilang.Jenis.STATUS -> {
                        _uiState.update { it.copy(pesan = "Status diperbarui.") }
                        muat()
                    }
                    else -> Unit
                }
            }
        }
    }

    fun muat() {
        viewModelScope.launch {
            _uiState.update { it.copy(memuat = true, error = null, tidakTersedia = false) }
            try {
                // coroutineScope: bila salah satu gagal, error ditangkap catch di bawah.
                val (detail, riwayat) = coroutineScope {
                    val detailAsync = async { repository.detail(id) }
                    val riwayatAsync = async { repository.riwayat(id) }
                    detailAsync.await() to riwayatAsync.await()
                }
                val url = repository.fotoUrl(detail.foto)
                _uiState.update { it.copy(detail = detail, riwayat = riwayat, urlFoto = url) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        detail = null,
                        riwayat = emptyList(),
                        error = pesanErrorHilang(e),
                        tidakTersedia = kodeErrorHilang(e) == "TB404"
                    )
                }
            } finally {
                _uiState.update { it.copy(memuat = false) }
            }
        }
    }

    /** Soft delete. Setelah berhasil, navigasi kembali ke daftar Laporan Saya. */
    fun hapus() {
        if (_uiState.value.menghapus) return
        viewModelScope.launch {
            _uiState.update { it.copy(menghapus = true) }
            try {
                repository.hapus(id)
                _uiState.update { it.copy(hapusSelesai = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(pesan = pesanErrorHilang(e)) }
            } finally {
                _uiState.update { it.copy(menghapus = false) }
            }
        }
    }

    fun hapusSelesaiDitangani() {
        _uiState.update { it.copy(hapusSelesai = false) }
    }

    fun pesanSudahTampil() {
        _uiState.update { it.copy(pesan = null) }
    }
}
