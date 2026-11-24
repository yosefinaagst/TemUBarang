package com.example.temu.ui.screens.claim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temu.data.repository.claim.ClaimRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Satu baris di daftar "Klaim Saya". */
data class ItemKlaimSaya(
    val id: String,
    val namaBarang: String,
    val lokasi: String,
    val ciriKhusus: String,
    val status: String?,
    val tanggalAjukan: String?,
    val fotoUrl: String?
)

data class KlaimSayaUiState(
    val items: List<ItemKlaimSaya> = emptyList(),
    val memuat: Boolean = true,
    val error: String? = null
)

class KlaimSayaViewModel(
    private val repo: ClaimRepository = ClaimRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(KlaimSayaUiState())
    val uiState: StateFlow<KlaimSayaUiState> = _uiState.asStateFlow()

    init {
        muat()
    }

    fun muat() {
        viewModelScope.launch {
            _uiState.update { it.copy(memuat = true, error = null) }
            try {
                // F2-AC2: hanya klaim milik user yang login (difilter di data source + RLS)
                val daftar = repo.daftarMilikSaya().map { klaim ->
                    ItemKlaimSaya(
                        id = klaim.id.orEmpty(),
                        namaBarang = klaim.reports?.namaBarang ?: "Barang",
                        lokasi = klaim.reports?.location.orEmpty(),
                        ciriKhusus = klaim.ciriKhusus,
                        status = klaim.status,
                        tanggalAjukan = klaim.createdAt,
                        fotoUrl = repo.fotoUrl(klaim.reports?.fotoPath)
                    )
                }
                _uiState.update { it.copy(memuat = false, items = daftar) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        memuat = false,
                        error = "Klaim belum bisa dimuat. Periksa koneksi lalu coba lagi."
                    )
                }
            }
        }
    }
}