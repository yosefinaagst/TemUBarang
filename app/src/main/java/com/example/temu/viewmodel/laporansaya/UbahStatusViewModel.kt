package com.example.temu.viewmodel.laporansaya

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temu.data.model.AturanStatus
import com.example.temu.data.model.StatusLaporan
import com.example.temu.data.repository.hilang.LaporanSayaRepository
import com.example.temu.ui.screens.laporansaya.UbahStatusUiState
import com.example.temu.viewmodel.hilang.kodeErrorHilang
import com.example.temu.viewmodel.hilang.pesanErrorHilang
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * F1 Halaman 2 · ViewModel layar Ubah status
 * Id diambil dari argumen navigasi "laporan_saya/status/{id}".
 * Alur: init → muat() (status & versi terbaru) → pilih status + catatan → simpan()
 *       → RPC ubah_status_hilang (update status + insert riwayat satu transaksi).
 */
class UbahStatusViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val repository = LaporanSayaRepository()
    private val id: String = savedStateHandle.get<String>("id").orEmpty()

    private val _uiState = MutableStateFlow(UbahStatusUiState())
    val uiState: StateFlow<UbahStatusUiState> = _uiState.asStateFlow()

    init {
        muat()
    }

    /** Membaca status dan versi terbaru (juga dipakai tombol "Muat ulang" saat konflik). */
    fun muat() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(memuat = true, errorForm = null, konflikVersi = false, errorStatus = null)
            }
            try {
                val detail = repository.detail(id)
                _uiState.update {
                    it.copy(
                        namaBarang = detail.nama,
                        statusSaatIni = detail.status,
                        versi = detail.versi,
                        // Pilihan lama dibuang bila tidak lagi valid untuk status terbaru.
                        statusBaru = it.statusBaru?.takeIf { s -> s in AturanStatus.tujuan(detail.status) }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorForm = pesanErrorHilang(e)) }
            } finally {
                _uiState.update { it.copy(memuat = false) }
            }
        }
    }

    fun pilihStatus(status: String) {
        _uiState.update { it.copy(statusBaru = status, errorStatus = null) }
    }

    fun ubahCatatan(nilai: String) {
        val dipotong = nilai.take(AturanStatus.CATATAN_MAKS)
        _uiState.update {
            it.copy(
                catatan = dipotong,
                errorCatatan = if (it.errorCatatan != null) validasiCatatan(dipotong) else null
            )
        }
    }

    /** true bila isian valid (dipakai layar untuk memunculkan konfirmasi "Tandai selesai?"). */
    fun validasi(): Boolean {
        val state = _uiState.value
        val errorStatus = if (state.statusBaru == null) "Pilih status baru." else null
        val errorCatatan = validasiCatatan(state.catatan)
        _uiState.update { it.copy(errorStatus = errorStatus, errorCatatan = errorCatatan) }
        return errorStatus == null && errorCatatan == null
    }

    fun simpan() {
        val state = _uiState.value
        if (state.mengirim || state.memuat || !validasi()) return
        val statusBaru = state.statusBaru ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(mengirim = true, errorForm = null, konflikVersi = false) }
            try {
                repository.ubahStatus(id, state.versi, statusBaru, state.catatan)
                _uiState.update { it.copy(statusTersimpan = statusBaru) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Gagal: status dan riwayat di database tidak berubah, isian tetap ada.
                _uiState.update {
                    it.copy(
                        errorForm = pesanErrorHilang(e),
                        konflikVersi = kodeErrorHilang(e) == "TB409"
                    )
                }
            } finally {
                _uiState.update { it.copy(mengirim = false) }
            }
        }
    }

    fun navigasiSelesai() {
        _uiState.update { it.copy(statusTersimpan = null) }
    }

    private fun validasiCatatan(catatan: String): String? =
        if (catatan.trim().length < AturanStatus.CATATAN_MIN) {
            "Catatan minimal ${AturanStatus.CATATAN_MIN} karakter."
        } else {
            null
        }

    companion object {
        /** Indeks tab ViewPager untuk sebuah status. */
        fun indeksTab(status: String): Int = StatusLaporan.semua.indexOf(status).coerceAtLeast(0)
    }
}
