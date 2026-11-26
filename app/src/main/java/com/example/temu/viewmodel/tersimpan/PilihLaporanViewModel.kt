package com.example.temu.viewmodel.tersimpan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.temu.data.model.bucketFoto
import com.example.temu.data.repository.tersimpan.TersimpanRepository
import com.example.temu.ui.screens.tersimpan.PilihLaporanUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * F4 · ViewModel layar Pilih Laporan ("Tambah ke Tersimpan")
 * Menampilkan laporan hilang & ditemukan, lalu menyimpan laporan yang dipilih (Insert).
 */
class PilihLaporanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TersimpanRepository(application)

    private val _uiState = MutableStateFlow(PilihLaporanUiState())
    val uiState: StateFlow<PilihLaporanUiState> = _uiState.asStateFlow()

    private var jobMuat: Job? = null

    init {
        muat()
    }

    /** Filter jenis laporan: null = semua, "lost" = hilang, "found" = ditemukan. */
    fun pilihJenis(jenis: String?) {
        if (jenis == _uiState.value.jenis) return
        _uiState.update { it.copy(jenis = jenis) }
        muat()
    }

    fun muat() {
        val jenis = _uiState.value.jenis
        jobMuat?.cancel()
        jobMuat = viewModelScope.launch {
            _uiState.update { it.copy(memuat = true, error = null) }
            try {
                val daftar = repository.pilihan(jenis)
                val idTersimpan = repository.idTersimpan()
                val url = repository.fotoUrls(daftar.map { bucketFoto(it.jenis) to it.foto })
                _uiState.update {
                    it.copy(daftar = daftar, idTersimpan = idTersimpan, urlFoto = url)
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

    /** Insert: menambahkan satu laporan ke daftar tersimpan. */
    fun simpan(reportId: String) {
        val state = _uiState.value
        if (reportId in state.idTersimpan || reportId in state.sedangDisimpan) return

        viewModelScope.launch {
            _uiState.update { it.copy(sedangDisimpan = it.sedangDisimpan + reportId) }
            try {
                repository.simpan(reportId)
                _uiState.update {
                    it.copy(
                        idTersimpan = it.idTersimpan + reportId,
                        pesan = "Ditambahkan ke Tersimpan."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(pesan = pesanErrorTersimpan(e)) }
            } finally {
                _uiState.update { it.copy(sedangDisimpan = it.sedangDisimpan - reportId) }
            }
        }
    }

    fun pesanSudahTampil() {
        _uiState.update { it.copy(pesan = null) }
    }
}
