
package com.example.temu.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.temu.data.model.FormTemuan
import com.example.temu.data.model.Temuan
import com.example.temu.data.repository.report.TemuanRepository
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TemuanViewModel(
    app: Application
) : AndroidViewModel(app) {

    private val repository = TemuanRepository()

    var daftar by mutableStateOf<List<Temuan>>(emptyList())
        private set

    var penggunaId by mutableStateOf<String?>(null)
        private set

    var sibuk by mutableStateOf(false)
        private set

    var pesan by mutableStateOf("")
        private set

    var simpanBerhasil by mutableStateOf(false)
        private set

    var perbaruiBerhasil by mutableStateOf(false)
        private set

    var hapusBerhasil by mutableStateOf(false)
        private set

    // Memuat laporan dan identitas pengguna
    fun muat() {
        proses {
            penggunaId = repository.idPengguna()
            daftar = repository.daftar()
        }
    }

    // Mendapatkan signed URL foto
    suspend fun urlFoto(path: String): String {
        return repository.fotoUrl(path)
    }

    // Validasi input umum
    private fun validasiData(data: FormTemuan) {
        require(data.nama.isNotBlank()) {
            "Nama barang harus diisi."
        }

        require(data.lokasi.isNotBlank()) {
            "Lokasi ditemukan harus diisi."
        }

        require(data.deskripsi.isNotBlank()) {
            "Deskripsi harus diisi."
        }

        try {
            val tanggal = LocalDate.parse(data.tanggal)

            require(
                !tanggal.isAfter(
                    LocalDate.now(ZoneId.of("Asia/Jakarta"))
                )
            ) {
                "Tanggal tidak boleh di masa depan."
            }
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException(
                "Gunakan format tanggal YYYY-MM-DD."
            )
        }
    }

    // Membaca dan memvalidasi foto maksimal 5 MB
    private suspend fun bacaFoto(uri: Uri): Pair<ByteArray, String> {
        val resolver = getApplication<Application>().contentResolver

        val mime = requireNotNull(resolver.getType(uri)) {
            "Jenis file foto tidak dapat dikenali."
        }

        require(
            mime == "image/jpeg" || mime == "image/png"
        ) {
            "Foto harus berformat JPG atau PNG."
        }

        val bytes = withContext(Dispatchers.IO) {
            val batasUkuran = 5 * 1024 * 1024

            val input = requireNotNull(
                resolver.openInputStream(uri)
            ) {
                "Foto tidak dapat dibuka."
            }

            input.use { stream ->
                val output = ByteArrayOutputStream()
                val buffer = ByteArray(8192)

                while (true) {
                    val jumlah = stream.read(buffer)

                    if (jumlah < 0) break

                    require(
                        output.size() + jumlah <= batasUkuran
                    ) {
                        "Ukuran foto maksimal 5 MB."
                    }

                    output.write(buffer, 0, jumlah)
                }

                require(output.size() > 0) {
                    "File foto kosong."
                }

                output.toByteArray()
            }
        }

        return bytes to mime
    }

    // Membuat laporan baru
    fun simpan(data: FormTemuan, uri: Uri?) {
        if (sibuk) return

        simpanBerhasil = false

        proses {
            validasiData(data)

            val fotoUri = requireNotNull(uri) {
                "Pilih foto barang terlebih dahulu."
            }

            val (bytes, mime) = bacaFoto(fotoUri)

            repository.simpan(
                data = data,
                foto = bytes,
                mime = mime
            )

            daftar = repository.daftar()
            simpanBerhasil = true
            pesan = "Laporan berhasil disimpan."
        }
    }

    // Memperbarui laporan
    // Foto boleh null jika pengguna mempertahankan foto lama
    fun perbarui(data: FormTemuan, uri: Uri?) {
        if (sibuk) return

        perbaruiBerhasil = false

        proses {
            validasiData(data)

            val fotoBaru = uri?.let { bacaFoto(it) }

            repository.perbarui(
                data = data,
                foto = fotoBaru?.first,
                mime = fotoBaru?.second
            )

            daftar = repository.daftar()
            perbaruiBerhasil = true
            pesan = "Laporan berhasil diperbarui."
        }
    }

    // Menghapus laporan secara soft delete

    fun hapus(id: String) {
        if (sibuk) return

        hapusBerhasil = false

        proses {
            // Lakukan soft delete di Supabase
            repository.hapus(id)

            // Ambil ulang data dari server
            daftar = repository.daftar()

            // Pastikan laporan tidak lagi muncul
            require(daftar.none { it.id == id }) {
                "Laporan masih ditemukan setelah proses penghapusan."
            }

            hapusBerhasil = true
            pesan = "Laporan berhasil dihapus."
        }
    }


    fun resetStatus() {
        pesan = ""
        simpanBerhasil = false
        perbaruiBerhasil = false
        hapusBerhasil = false
    }

    private fun proses(aksi: suspend () -> Unit) {
        if (sibuk) return

        viewModelScope.launch {
            sibuk = true
            pesan = ""

            try {
                aksi()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                pesan = pesanUntukPengguna(e)
            } finally {
                sibuk = false
            }
        }
    }
    private fun pesanUntukPengguna(e: Exception): String {
        // Pesan validasi lokal tetap ditampilkan
        if (e is IllegalArgumentException) {
            return e.message ?: "Data yang dimasukkan tidak valid."
        }

        val detail = e.message.orEmpty().lowercase()

        return when {
            "permission denied" in detail ||
                    "42501" in detail -> {
                "Akun tidak memiliki izin untuk mengubah laporan."
            }

            "not found" in detail -> {
                "Laporan tidak ditemukan atau sudah dihapus."
            }

            "timeout" in detail -> {
                "Koneksi terlalu lama. Silakan coba lagi."
            }

            else -> {
                "Terjadi kesalahan saat memproses laporan. Silakan coba lagi."
            }
        }
    }
}
