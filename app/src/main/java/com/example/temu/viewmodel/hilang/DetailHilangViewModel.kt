package com.example.temu.viewmodel.hilang

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temu.data.repository.hilang.LaporanHilangRepository
import com.example.temu.data.repository.hilang.PerubahanHilang
import com.example.temu.data.repository.hilang.PerubahanLaporanHilang
import com.example.temu.ui.screens.hilang.DetailHilangUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * F1 · ViewModel layar Detail laporan hilang
 * Id laporan diambil dari argumen navigasi "hilang/detail/{id}?baru={baru}".
 * Alur: init → muat() → repository.detail(id) → uiState
 */
class DetailHilangViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = LaporanHilangRepository()
    private val id: String = savedStateHandle.get<String>("id").orEmpty()

    private val _uiState = MutableStateFlow(DetailHilangUiState())
    val uiState: StateFlow<DetailHilangUiState> = _uiState.asStateFlow()

    init {
        // Dibuka tepat setelah laporan baru terbit.
        if (savedStateHandle.get<Boolean>("baru") == true) {
            _uiState.update { it.copy(pesan = "Laporan berhasil diterbitkan.") }
        }

        muat()

        // Setelah laporan ini diedit di layar Form, muat ulang detailnya.
        viewModelScope.launch {
            PerubahanLaporanHilang.kabar.collect { perubahan ->
                if (perubahan.id == id && perubahan.jenis == PerubahanHilang.Jenis.EDIT) {
                    _uiState.update { it.copy(pesan = "Perubahan disimpan.") }
                    muat()
                }
            }
        }
    }

    fun muat() {
        viewModelScope.launch {
            _uiState.update { it.copy(memuat = true, error = null, tidakTersedia = false) }
            try {
                val detail = repository.detail(id)
                val url = repository.fotoUrl(detail.foto)
                _uiState.update { it.copy(detail = detail, urlFoto = url) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        detail = null,
                        error = pesanErrorHilang(e),
                        tidakTersedia = kodeErrorHilang(e) == "TB404"
                    )
                }
            } finally {
                _uiState.update { it.copy(memuat = false) }
            }
        }
    }

    /** Soft delete. Setelah berhasil, navigasi kembali ke Jelajah. */
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
