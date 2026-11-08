package com.example.temubarang.ui.ditemukan

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.temubarang.data.model.*
import com.example.temubarang.data.repository.report.TemuanRepository
import java.time.LocalDate
import java.time.ZoneId
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.*

enum class Halaman { MEMUAT, MASUK, HOME, FORM }

class TemuanViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = TemuanRepository()
    var halaman by mutableStateOf(Halaman.MEMUAT)
        private set
    var daftar by mutableStateOf(emptyList<Temuan>())
        private set
    var sibuk by mutableStateOf(false)
        private set
    var pesan by mutableStateOf("")
        private set

    init { periksaSesi() }

    private fun proses(aksi: suspend () -> Unit) {
        if (sibuk) return
        sibuk = true
        pesan = ""
        viewModelScope.launch {
            try { aksi() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { pesan = e.localizedMessage ?: "Terjadi kesalahan. Coba lagi." }
            finally { sibuk = false }
        }
    }

    fun periksaSesi() = proses {
        if (repo.sudahMasuk()) {
            halaman = Halaman.HOME
            daftar = repo.daftar()
        } else halaman = Halaman.MASUK
    }

    fun masuk(email: String, sandi: String) = proses {
        repo.masuk(email, sandi)
        halaman = Halaman.HOME
        daftar = repo.daftar()
    }

    fun muat() = proses { daftar = repo.daftar() }
    suspend fun urlFoto(path: String) = repo.fotoUrl(path)

    fun bukaForm() { pesan = ""; halaman = Halaman.FORM }
    fun kembali() { if (!sibuk) { pesan = ""; halaman = Halaman.HOME } }

    fun simpan(data: FormTemuan, uri: Uri?) = proses {
        require(data.nama.isNotBlank() && data.lokasi.isNotBlank() && data.deskripsi.isNotBlank())
            { "Isi nama barang, lokasi, dan deskripsi." }
        val tanggal = try { LocalDate.parse(data.tanggal) }
            catch (_: java.time.format.DateTimeParseException) { error("Gunakan tanggal YYYY-MM-DD.") }
        require(!tanggal.isAfter(LocalDate.now(ZoneId.of("Asia/Jakarta")))) { "Tanggal tidak boleh di masa depan." }
        requireNotNull(uri) { "Pilih satu foto barang." }
        val resolver = getApplication<Application>().contentResolver
        val mime = resolver.getType(uri)
        require(mime == "image/png" || mime == "image/jpeg") { "Pilih foto JPG atau PNG." }
        val bytes = withContext(Dispatchers.IO) {
            val limit = 5 * 1024 * 1024
            requireNotNull(resolver.openInputStream(uri)) { "Foto tidak bisa dibuka." }.use { stream ->
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                while (true) {
                    val n = stream.read(buffer)
                    if (n < 0) break
                    require(out.size() + n <= limit) { "Ukuran foto maksimal 5 MB." }
                    out.write(buffer, 0, n)
                }
                require(out.size() > 0) { "File foto kosong." }
                out.toByteArray()
            }
        }
        repo.simpan(data, bytes, requireNotNull(mime))
        halaman = Halaman.HOME
        try {
            daftar = repo.daftar()
            pesan = "Laporan berhasil disimpan."
        } catch (e: CancellationException) { throw e }
          catch (_: Exception) { pesan = "Laporan tersimpan. Tekan Muat ulang untuk melihatnya." }
    }
}
