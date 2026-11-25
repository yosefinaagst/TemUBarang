package com.example.temu.viewmodel.tersimpan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.temu.data.model.bucketFoto
import com.example.temu.data.repository.tersimpan.TersimpanRepository
import com.example.temu.data.repository.tersimpan.kunciFoto
import com.example.temu.ui.screens.tersimpan.DetailTersimpanUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * F4 · ViewModel layar Detail Tersimpan
 * Id laporan diambil dari argumen navigasi "tersimpan/detail/{id}?simpan={simpan}".
 * simpan = true → dibuka dari tombol "Simpan" di Detail laporan hilang (F1):
 *                 laporan disimpan dulu (Insert), lalu detailnya dimuat.
 */
class DetailTersimpanViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = TersimpanRepository(application)
    private val id: String = savedStateHandle.get<String>("id").orEmpty()

    private val _uiState = MutableStateFlow(DetailTersimpanUiState())
    val uiState: StateFlow<DetailTersimpanUiState> = _uiState.asStateFlow()

    init {
        if (savedStateHandle.get<Boolean>("simpan") == true) {
            // Hapus tanda agar laporan tidak disimpan ulang saat layar dibuat kembali.
            savedStateHandle["simpan"] = false
            simpanLaluMuat()
        } else {
            muat()
        }
    }

    private fun simpanLaluMuat() {
        viewModelScope.launch {
            _uiState.update { it.copy(memuat = true, error = null) }
            try {
                repository.simpan(id)
                _uiState.update { it.copy(pesan = "Ditambahkan ke Tersimpan.") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(pesan = pesanErrorTersimpan(e)) }
            }
            muat()
        }
    }

    fun muat() {
        viewModelScope.launch {
            _uiState.update { it.copy(memuat = true, error = null, tidakDitemukan = false) }
            try {
                val detail = repository.detail(id)
                if (detail == null) {
                    _uiState.update { it.copy(detail = null, tidakDitemukan = true) }
                } else {
                    val bucket = bucketFoto(detail.jenis)
                    val url = if (detail.tersedia) {
                        repository.fotoUrls(listOf(bucket to detail.foto))[kunciFoto(bucket, detail.foto)]
                    } else {
                        null
                    }
                    _uiState.update { it.copy(detail = detail, urlFoto = url) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = pesanErrorTersimpan(e)) }
            } finally {
                _uiState.update { it.copy(memuat = false) }
            }
        }
    }

    /** Delete: menghapus laporan dari daftar tersimpan. Setelah berhasil, layar ditutup. */
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
                _uiState.update { it.copy(pesan = pesanErrorTersimpan(e)) }
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
