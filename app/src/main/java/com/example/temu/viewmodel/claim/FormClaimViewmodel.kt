package com.example.temu.ui.screens.claim

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temu.data.remote.KlaimSudahAda
import com.example.temu.data.remote.RingkasanLaporan
import com.example.temu.data.repository.claim.ClaimRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormClaimUiState(
    val ringkasan: RingkasanLaporan? = null,
    val memuat: Boolean = true,
    val mengirim: Boolean = false,
    val error: String? = null,
    val klaimId: String? = null,          // terisi -> tampilkan layar "Pengajuan berhasil dikirim"
    val sudahPernahKlaim: Boolean = false // F2-AC3
)

class FormClaimViewModel(
    private val repo: ClaimRepository = ClaimRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormClaimUiState())
    val uiState: StateFlow<FormClaimUiState> = _uiState.asStateFlow()

    var ciriKhusus by mutableStateOf("")
        private set
    var keterangan by mutableStateOf("")
        private set

    private var reportId: String? = null

    fun muat(id: String, paksa: Boolean = false) {
        if (reportId == id && !paksa) return   // jangan muat ulang saat layar diputar
        reportId = id
        viewModelScope.launch {
            _uiState.update { it.copy(memuat = true, error = null) }
            try {
                val ringkasan = repo.ringkasanLaporan(id)
                val lama = repo.klaimSayaUntuk(id)
                _uiState.update {
                    it.copy(
                        memuat = false,
                        ringkasan = ringkasan,
                        klaimId = lama?.id,
                        sudahPernahKlaim = lama != null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(memuat = false, error = e.message ?: "Gagal memuat laporan.") }
            }
        }
    }

    fun cobaLagi() {
        reportId?.let { muat(it, paksa = true) }
    }

    fun ubahCiri(teks: String) {
        ciriKhusus = teks
        _uiState.update { it.copy(error = null) }
    }

    fun ubahKeterangan(teks: String) {
        keterangan = teks
    }

    fun kirim() {
        val id = reportId ?: return
        if (_uiState.value.mengirim) return
        if (ciriKhusus.isBlank()) {
            _uiState.update { it.copy(error = "Ciri khusus wajib diisi.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(mengirim = true, error = null) }
            try {
                val klaim = repo.ajukan(id, ciriKhusus, keterangan.ifBlank { null })
                _uiState.update { it.copy(mengirim = false, klaimId = klaim.id.orEmpty()) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: KlaimSudahAda) {
                _uiState.update {
                    it.copy(mengirim = false, klaimId = e.klaimId, sudahPernahKlaim = true)
                }
            } catch (e: Exception) {
                // Gagal -> tetap di form (sesuai PRD)
                _uiState.update { it.copy(mengirim = false, error = e.message ?: "Pengajuan gagal dikirim.") }
            }
        }
    }
}