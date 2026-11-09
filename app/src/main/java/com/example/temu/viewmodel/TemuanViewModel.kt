
package com.example.temu.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.temu.data.repository.auth.AuthRepository
import com.example.temu.data.repository.report.TemuanRepository
import com.example.temubarang.data.model.FormTemuan
import com.example.temubarang.data.model.Temuan
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Sementara dipertahankan untuk kompatibilitas dengan UI lama.
enum class Halaman {
    MEMUAT,
    MASUK,
    HOME,
    FORM
}

class TemuanViewModel(
    app: Application
) : AndroidViewModel(app) {

    // Repository autentikasi
    private val authRepo = AuthRepository()

    // Repository laporan barang ditemukan
    private val repo = TemuanRepository()

    // State halaman (akan dihapus setelah migrasi Navigation selesai)
    var halaman by mutableStateOf(Halaman.MEMUAT)
        private set

    // Daftar laporan barang ditemukan
    var daftar by mutableStateOf<List<Temuan>>(emptyList())
        private set

    // Status proses loading atau penyimpanan
    var sibuk by mutableStateOf(false)
        private set

    // Pesan informasi atau error
    var pesan by mutableStateOf("")
        private set

    init {
        periksaSesi()
    }

    // Menjalankan proses asynchronous dengan penanganan error terpusat
    private fun proses(aksi: suspend () -> Unit) {
        if (sibuk) return

        sibuk = true
        pesan = ""

        viewModelScope.launch {
            try {
                aksi()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                pesan = e.localizedMessage
                    ?: "Terjadi kesalahan. Coba lagi."
            } finally {
                sibuk = false
            }
        }
    }

    // Memeriksa apakah pengguna sudah memiliki sesi login
    fun periksaSesi() = proses {
        if (authRepo.sudahMasuk()) {
            halaman = Halaman.HOME
            daftar = repo.daftar()
        } else {
            halaman = Halaman.MASUK
        }
    }

    // Login menggunakan Supabase Auth
    fun masuk(email: String, sandi: String) = proses {
        require(email.isNotBlank()) {
            "Email harus diisi."
        }

        require(sandi.isNotBlank()) {
            "Kata sandi harus diisi."
        }

        authRepo.masuk(email, sandi)

        halaman = Halaman.HOME
        daftar = repo.daftar()
    }

    // Memuat ulang daftar laporan
    fun muat() = proses {
        daftar = repo.daftar()
    }

    // Membuat signed URL untuk foto laporan
    suspend fun urlFoto(path: String): String {
        return repo.fotoUrl(path)
    }

    // Membuka halaman form (sementara untuk UI lama)
    fun bukaForm() {
        if (!sibuk) {
            pesan = ""
            halaman = Halaman.FORM
        }
    }

    // Kembali ke halaman Home (sementara untuk UI lama)
    fun kembali() {
        if (!sibuk) {
            pesan = ""
            halaman = Halaman.HOME
        }
    }

    // Menyimpan laporan barang ditemukan beserta fotonya
    fun simpan(
        data: FormTemuan,
        uri: Uri?
    ) = proses {

        // Validasi data wajib
        require(data.nama.isNotBlank()) {
            "Nama barang harus diisi."
        }

        require(data.lokasi.isNotBlank()) {
            "Lokasi ditemukan harus diisi."
        }

        require(data.deskripsi.isNotBlank()) {
            "Deskripsi harus diisi."
        }

        // Validasi format tanggal
        val tanggal = try {
            LocalDate.parse(data.tanggal)
        } catch (e: DateTimeParseException) {
            error("Gunakan tanggal YYYY-MM-DD.")
        }

        // Tanggal tidak boleh melebihi hari ini
        require(
            !tanggal.isAfter(
                LocalDate.now(ZoneId.of("Asia/Jakarta"))
            )
        ) {
            "Tanggal tidak boleh di masa depan."
        }

        // Foto wajib dipilih
        val fotoUri = requireNotNull(uri) {
            "Pilih satu foto barang."
        }

        val resolver = getApplication<Application>().contentResolver

        // Validasi MIME type
        val mime = resolver.getType(fotoUri)

        require(mime == "image/png" || mime == "image/jpeg") {
            "Pilih foto JPG atau PNG."
        }

        // Membaca foto dan membatasi ukuran maksimal 5 MB
        val bytes = withContext(Dispatchers.IO) {

            val batasUkuran = 5 * 1024 * 1024

            val input = requireNotNull(
                resolver.openInputStream(fotoUri)
            ) {
                "Foto tidak bisa dibuka."
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

        // Upload foto dan simpan laporan melalui Repository
        repo.simpan(
            data = data,
            foto = bytes,
            mime = mime
        )

        // Kembali ke Home setelah penyimpanan berhasil
        halaman = Halaman.HOME

        // Muat ulang daftar laporan
        try {
            daftar = repo.daftar()
            pesan = "Laporan berhasil disimpan."
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            pesan = "Laporan tersimpan. Tekan Muat ulang untuk melihatnya."
        }
    }
}
