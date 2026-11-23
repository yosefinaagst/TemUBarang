package com.example.temu.viewmodel.hilang

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.temu.data.model.DetailHilang
import com.example.temu.data.model.FormHilang
import com.example.temu.data.model.KolomFormHilang
import com.example.temu.data.model.ZONA_WAKTU_KAMPUS
import com.example.temu.data.remote.FotoUnggah
import com.example.temu.data.repository.hilang.LaporanHilangRepository
import com.example.temu.ui.screens.hilang.FormHilangUiState
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * F1 · ViewModel layar Form tambah / edit laporan hilang
 * Route "hilang/tambah"      → mode tambah (id laporan baru dibuat di sini)
 * Route "hilang/edit/{id}"   → mode edit   (init → muatUntukEdit() → repository.detail(id))
 * AndroidViewModel dipakai karena perlu membaca file foto dari galeri.
 */
class FormHilangViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    private val repository = LaporanHilangRepository()
    private val editId: String? = savedStateHandle.get<String>("id")

    /** Id laporan: dibuat sekali, jadi kirim ulang tidak membuat laporan ganda. */
    private val idLaporan: String = editId ?: UUID.randomUUID().toString()
    private var versi = 1

    private val _uiState = MutableStateFlow(FormHilangUiState(modeEdit = editId != null))
    val uiState: StateFlow<FormHilangUiState> = _uiState.asStateFlow()

    init {
        if (editId != null) muatUntukEdit()
    }

    /** Mengambil data laporan untuk diisi ke form edit (juga dipakai tombol "Muat ulang"). */
    fun muatUntukEdit() {
        val id = editId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(memuat = true, errorForm = null, konflikVersi = false, errorKolom = emptyMap()) }
            try {
                val detail = repository.detail(id)
                val url = repository.fotoUrl(detail.foto)
                isiDariDetail(detail, url)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(errorForm = pesanErrorHilang(e)) }
            } finally {
                _uiState.update { it.copy(memuat = false) }
            }
        }
    }

    fun ubahForm(baru: FormHilang) {
        _uiState.update { state ->
            if (state.errorKolom.isEmpty()) {
                state.copy(form = baru)
            } else {
                // Hilangkan pesan error kolom yang sudah diperbaiki.
                val masihSalah = validasi(baru)
                val errorKolom = state.errorKolom.filterKeys { it in masihSalah }
                state.copy(
                    form = baru,
                    errorKolom = errorKolom,
                    errorForm = if (errorKolom.isEmpty() && !state.konflikVersi) null else state.errorForm
                )
            }
        }
    }

    fun pilihFoto(uri: String) {
        ubahForm(_uiState.value.form.copy(fotoBaru = uri))
    }

    /** Membatalkan foto baru, atau menghapus foto lama pada mode edit. */
    fun hapusFoto() {
        val form = _uiState.value.form
        ubahForm(if (form.fotoBaru != null) form.copy(fotoBaru = null) else form.copy(fotoLama = ""))
    }

    fun kirim() {
        val state = _uiState.value
        if (state.mengirim || state.memuat) return

        val salah = validasi(state.form)
        if (salah.isNotEmpty()) {
            _uiState.update {
                it.copy(errorKolom = salah, errorForm = "Lengkapi ${salah.size} kolom wajib yang ditandai.")
            }
            return
        }

        val data = state.form
        viewModelScope.launch {
            _uiState.update {
                it.copy(mengirim = true, errorKolom = emptyMap(), errorForm = null, konflikVersi = false)
            }
            try {
                val foto = data.fotoBaru?.let { bacaFoto(it) }
                if (state.modeEdit) {
                    val hapusFotoLama = state.formAwal.fotoLama.isNotBlank() &&
                        data.fotoLama.isBlank() && data.fotoBaru == null
                    versi = repository.perbarui(idLaporan, versi, data, foto, hapusFotoLama)
                    _uiState.update { it.copy(formAwal = data, editSelesai = true) }
                } else {
                    val id = repository.simpan(idLaporan, data, foto)
                    _uiState.update { it.copy(formAwal = data, idTerbit = id) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Isian tidak dihapus, pengguna bisa langsung mencoba lagi.
                _uiState.update {
                    it.copy(errorForm = pesanErrorHilang(e), konflikVersi = kodeErrorHilang(e) == "TB409")
                }
            } finally {
                _uiState.update { it.copy(mengirim = false) }
            }
        }
    }

    /** Dipanggil navigasi setelah berpindah layar karena terbit / edit selesai. */
    fun navigasiSelesai() {
        _uiState.update { it.copy(idTerbit = null, editSelesai = false) }
    }

    // ------------------------------------------------------------------

    private fun isiDariDetail(detail: DetailHilang, urlFoto: String?) {
        val form = FormHilang(
            nama = detail.nama,
            kategori = detail.kategori,
            deskripsi = detail.deskripsi,
            lokasi = detail.lokasi,
            tanggal = try {
                LocalDate.parse(detail.tanggal)
            } catch (_: DateTimeParseException) {
                null
            },
            ciriPublik = detail.ciriPublik,
            fotoLama = detail.foto
        )
        versi = detail.versi
        _uiState.update {
            it.copy(
                form = form,
                formAwal = form,
                urlFotoLama = urlFoto,
                errorForm = if (detail.milikSaya) null else "Hanya pembuat laporan yang dapat mengubahnya."
            )
        }
    }

    private fun validasi(f: FormHilang): Map<KolomFormHilang, String> = buildMap {
        if (f.nama.isBlank()) put(KolomFormHilang.NAMA, "Nama barang wajib diisi.")
        if (f.kategori.isBlank()) put(KolomFormHilang.KATEGORI, "Pilih kategori barang.")
        if (f.deskripsi.isBlank()) put(KolomFormHilang.DESKRIPSI, "Deskripsi wajib diisi.")
        if (f.lokasi.isBlank()) put(KolomFormHilang.LOKASI, "Lokasi terakhir wajib diisi.")

        val tanggal = f.tanggal
        if (tanggal == null) {
            put(KolomFormHilang.TANGGAL, "Pilih tanggal kehilangan.")
        } else if (tanggal.isAfter(LocalDate.now(ZONA_WAKTU_KAMPUS))) {
            put(KolomFormHilang.TANGGAL, "Tanggal tidak boleh melewati hari ini.")
        }
    }

    /** Membaca foto dari galeri: hanya JPG/PNG, maksimal 5 MB (sama dengan F3). */
    private suspend fun bacaFoto(uriTeks: String): FotoUnggah {
        val resolver = getApplication<Application>().contentResolver
        val uri = Uri.parse(uriTeks)

        val mime = resolver.getType(uri).orEmpty()
        require(mime == "image/jpeg" || mime == "image/png") {
            "Foto harus berformat JPG atau PNG."
        }

        val bytes = withContext(Dispatchers.IO) {
            val batas = 5 * 1024 * 1024
            val input = requireNotNull(resolver.openInputStream(uri)) {
                "Foto tidak dapat dibuka. Pilih foto lain."
            }
            input.use { stream ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val jumlah = stream.read(buffer)
                    if (jumlah < 0) break
                    require(output.size() + jumlah <= batas) { "Ukuran foto maksimal 5 MB." }
                    output.write(buffer, 0, jumlah)
                }
                require(output.size() > 0) { "File foto kosong." }
                output.toByteArray()
            }
        }
        return FotoUnggah(bytes, mime)
    }
}
