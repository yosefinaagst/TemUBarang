package com.example.temu.viewmodel.profil

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.temu.data.model.Profil
import com.example.temu.data.repository.profil.ProfilRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class ProfilUiState(
    val profil: Profil? = null,
    val memuat: Boolean = true,
    val error: String? = null,
    val sedangEdit: Boolean = false,
    val menyimpan: Boolean = false,
    val gagalSimpan: Boolean = false,
    val sedangKeluar: Boolean = false,
    val keluarBerhasil: Boolean = false,
    val pesan: String? = null
)

class ProfilViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = ProfilRepository()

    private val _uiState = MutableStateFlow(ProfilUiState())
    val uiState: StateFlow<ProfilUiState> = _uiState.asStateFlow()

    // Isian saat mode edit
    var namaEdit by mutableStateOf("")
        private set
    var kontakEdit by mutableStateOf("")
        private set

    /** Foto yang baru dipilih dari galeri (belum diunggah). */
    var fotoBaru by mutableStateOf<Uri?>(null)
        private set

    /** true kalau pengguna menekan "Hapus foto" (kembali ke inisial). */
    var hapusFoto by mutableStateOf(false)
        private set

    init {
        muat()
    }

    fun muat() {
        viewModelScope.launch {
            _uiState.update { it.copy(memuat = true, error = null) }
            try {
                val profil = repo.ambil()
                _uiState.update { it.copy(memuat = false, profil = profil) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gagal memuat profil", e)
                _uiState.update {
                    it.copy(memuat = false, error = "Profil belum bisa dimuat. Periksa koneksi lalu coba lagi.")
                }
            }
        }
    }

    // ---------------- Edit ----------------

    fun mulaiEdit() {
        val profil = _uiState.value.profil ?: return
        namaEdit = profil.namaLengkap
        kontakEdit = profil.kontak.orEmpty()
        fotoBaru = null
        hapusFoto = false
        _uiState.update { it.copy(sedangEdit = true, gagalSimpan = false) }
    }

    fun batalEdit() {
        if (_uiState.value.menyimpan) return
        fotoBaru = null
        hapusFoto = false
        _uiState.update { it.copy(sedangEdit = false, gagalSimpan = false) }
    }

    fun ubahNama(teks: String) {
        namaEdit = teks
    }

    fun ubahKontak(teks: String) {
        kontakEdit = teks
    }

    fun pilihFoto(uri: Uri) {
        fotoBaru = uri
        hapusFoto = false
    }

    fun hapusFotoProfil() {
        fotoBaru = null
        hapusFoto = true
    }

    fun simpan() {
        if (_uiState.value.menyimpan || namaEdit.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(menyimpan = true, gagalSimpan = false) }
            try {
                val bytes = fotoBaru?.let { bacaFoto(it) }
                val baru = repo.simpan(
                    namaLengkap = namaEdit,
                    kontak = kontakEdit,
                    fotoBaru = bytes,
                    hapusFoto = hapusFoto,
                    fotoLama = _uiState.value.profil?.fotoPath
                )
                fotoBaru = null
                hapusFoto = false
                _uiState.update {
                    it.copy(
                        profil = baru,
                        menyimpan = false,
                        sedangEdit = false,
                        pesan = "Profil berhasil diperbarui."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gagal menyimpan profil", e)
                // Tetap di mode edit, isian & foto pilihan tidak hilang (PR-04)
                _uiState.update { it.copy(menyimpan = false, gagalSimpan = true) }
            }
        }
    }

    /**
     * Baca foto dari galeri, perkecil (sisi terpanjang maks. 720 px), lalu kompres ke JPEG.
     * Supaya upload cepat dan hemat kuota Storage.
     */
    private suspend fun bacaFoto(uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        val resolver = getApplication<Application>().contentResolver

        val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder otomatis memutar foto sesuai orientasi kamera (EXIF)
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                val w = info.size.width
                val h = info.size.height
                val skala = minOf(1f, UKURAN_MAKS.toFloat() / maxOf(w, h))
                decoder.setTargetSize(
                    (w * skala).toInt().coerceAtLeast(1),
                    (h * skala).toInt().coerceAtLeast(1)
                )
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            val ukuran = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, ukuran) }
            var sample = 1
            while (maxOf(ukuran.outWidth, ukuran.outHeight) / (sample * 2) >= UKURAN_MAKS) sample *= 2
            resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: throw IllegalStateException("Foto tidak dapat dibaca.")
        }

        ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            out.toByteArray()
        }
    }

    // ---------------- Keluar ----------------

    fun keluar() {
        if (_uiState.value.sedangKeluar) return
        viewModelScope.launch {
            _uiState.update { it.copy(sedangKeluar = true) }
            try {
                repo.keluar()
                _uiState.update { it.copy(sedangKeluar = false, keluarBerhasil = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gagal keluar", e)
                _uiState.update {
                    it.copy(sedangKeluar = false, pesan = "Gagal keluar. Periksa koneksi lalu coba lagi.")
                }
            }
        }
    }

    fun pesanSudahTampil() {
        _uiState.update { it.copy(pesan = null) }
    }

    private companion object {
        const val TAG = "Profil"
        const val UKURAN_MAKS = 720
    }
}
