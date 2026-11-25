package com.example.temu.viewmodel.tersimpan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.temu.data.model.bucketFoto
import com.example.temu.data.repository.tersimpan.PerubahanTersimpan
import com.example.temu.data.repository.tersimpan.TersimpanRepository
import com.example.temu.ui.screens.tersimpan.DaftarTersimpanUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * F4 · ViewModel layar Daftar Tersimpan
 * Alur: init → muat() → repository.daftar() → uiState
 * Bila ada laporan ditambah / dihapus di layar lain, daftar dimuat ulang otomatis.
 */
class DaftarTersimpanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TersimpanRepository(application)

    private val _uiState = MutableStateFlow(DaftarTersimpanUiState())
    val uiState: StateFlow<DaftarTersimpanUiState> = _uiState.asStateFlow()

    private var jobMuat: Job? = null

    init {
        muat()
        viewModelScope.launch {
            PerubahanTersimpan.kabar.collect { muat() }
        }
    }

    fun muat() {
        jobMuat?.cancel()
        jobMuat = viewModelScope.launch {
            _uiState.update { it.copy(memuat = true, error = null) }
            try {
                val daftar = repository.daftar()
                val url = repository.fotoUrls(
                    daftar.filter { it.tersedia }.map { bucketFoto(it.jenis) to it.foto }
                )
                _uiState.update { it.copy(daftar = daftar, urlFoto = url) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = pesanErrorTersimpan(e)) }
            } finally {
                _uiState.update { it.copy(memuat = false) }
            }
        }
    }

    fun pesanSudahTampil() {
        _uiState.update { it.copy(pesan = null) }
    }
}
