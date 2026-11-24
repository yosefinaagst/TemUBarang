package com.example.temu.ui.screens.claim

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temu.data.model.Claim
import com.example.temu.data.remote.ClaimRemoteDataSource
import com.example.temu.data.repository.claim.ClaimRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailKlaimUiState(
    val klaim: Claim? = null,
    val fotoUrl: String? = null,
    val memuat: Boolean = true,
    val error: String? = null,          // gagal memuat detail
    val sedangEdit: Boolean = false,
    val menyimpan: Boolean = false,
    val menghapus: Boolean = false,
    val pesan: String? = null,          // pesan singkat (snackbar)
    val dihapus: Boolean = false        // true -> layar kembali ke Klaim Saya
) {
    /** Klaim hanya bisa diedit/dihapus sebelum laporan selesai (PRD F2). */
    val bisaDiubah: Boolean
        get() = klaim != null && klaim.reports?.status != ClaimRemoteDataSource.STATUS_SELESAI
}

class DetailKlaimViewModel(
    private val repo: ClaimRepository = ClaimRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailKlaimUiState())
    val uiState: StateFlow<DetailKlaimUiState> = _uiState.asStateFlow()

    // Isian saat mode edit
    var ciriEdit by mutableStateOf("")
        private set
    var keteranganEdit by mutableStateOf("")
        private set

    private var klaimId: String? = null

    fun muat(id: String, paksa: Boolean = false) {
        if (klaimId == id && !paksa) return
        klaimId = id
        viewModelScope.launch {
            _uiState.update { it.copy(memuat = true, error = null) }
            try {
                val klaim = repo.detail(id)
                val foto = repo.fotoUrl(klaim.reports?.fotoPath)
                _uiState.update { it.copy(memuat = false, klaim = klaim, fotoUrl = foto) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(memuat = false, error = "Detail klaim belum bisa dimuat. Coba lagi.")
                }
            }
        }
    }

    fun cobaLagi() {
        klaimId?.let { muat(it, paksa = true) }
    }

    // ---------------- Edit ----------------

    fun mulaiEdit() {
        val klaim = _uiState.value.klaim ?: return
        ciriEdit = klaim.ciriKhusus
        keteranganEdit = klaim.description.orEmpty()
        _uiState.update { it.copy(sedangEdit = true) }
    }

    fun batalEdit() {
        _uiState.update { it.copy(sedangEdit = false) }
    }

    fun ubahCiri(teks: String) {
        ciriEdit = teks
    }

    fun ubahKeterangan(teks: String) {
        keteranganEdit = teks
    }

    fun simpan() {
        val id = klaimId ?: return
        if (_uiState.value.menyimpan) return
        if (ciriEdit.isBlank()) {
            _uiState.update { it.copy(pesan = "Ciri khusus wajib diisi.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(menyimpan = true) }
            try {
                val baru = repo.ubah(id, ciriEdit, keteranganEdit.ifBlank { null })
                _uiState.update {
                    it.copy(
                        menyimpan = false,
                        sedangEdit = false,
                        // hasil update tidak membawa data laporan, jadi pakai yang lama
                        klaim = baru.copy(reports = it.klaim?.reports),
                        pesan = "Klaim berhasil diperbarui."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Gagal -> tetap di mode edit (PRD: validasi -> simpan)
                _uiState.update {
                    it.copy(menyimpan = false, pesan = pesanRamah(e, "Perubahan gagal disimpan."))
                }
            }
        }
    }

    // ---------------- Hapus ----------------

    fun hapus() {
        val id = klaimId ?: return
        if (_uiState.value.menghapus) return
        viewModelScope.launch {
            _uiState.update { it.copy(menghapus = true) }
            try {
                repo.hapus(id)
                _uiState.update { it.copy(menghapus = false, dihapus = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(menghapus = false, pesan = pesanRamah(e, "Klaim gagal dihapus."))
                }
            }
        }
    }

    fun pesanSudahTampil() {
        _uiState.update { it.copy(pesan = null) }
    }

    /** Pesan dari require()/IllegalStateException kita sudah ramah; error lain disederhanakan. */
    private fun pesanRamah(e: Exception, cadangan: String): String =
        when (e) {
            is IllegalArgumentException, is IllegalStateException -> e.message ?: cadangan
            else -> "$cadangan Periksa koneksi lalu coba lagi."
        }
}