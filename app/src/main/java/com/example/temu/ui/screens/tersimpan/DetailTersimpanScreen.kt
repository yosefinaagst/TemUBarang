package com.example.temu.ui.screens.tersimpan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temu.data.model.LaporanTersimpan
import com.example.temu.data.model.kategoriBarang

/**
 * F4 · Halaman Detail Tersimpan
 * Informasi laporan, foto, dan tombol "Hapus dari Tersimpan".
 * Screen ini TIDAK memakai ViewModel: hanya menerima [state] dan aksi (lambda).
 */
@Composable
fun DetailTersimpanScreen(
    state: DetailTersimpanUiState,
    onKembali: () -> Unit,
    onCobaLagi: () -> Unit,
    onHapus: () -> Unit,
    onPesanTampil: () -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.pesan) {
        val pesan = state.pesan
        if (pesan != null) {
            snackbar.showSnackbar(pesan)
            onPesanTampil()
        }
    }

    var konfirmasiHapus by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = WarnaTersimpan.Latar,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { HeaderTersimpan("Detail Tersimpan", onKembali) }
    ) { padding ->
        val detail = state.detail
        val error = state.error
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.memuat && detail == null ->
                    CircularProgressIndicator(color = WarnaTersimpan.Rose)

                detail != null -> IsiDetail(
                    detail = detail,
                    urlFoto = state.urlFoto,
                    menghapus = state.menghapus,
                    onMintaHapus = { konfirmasiHapus = true }
                )

                state.tidakDitemukan -> PesanKeadaanTersimpan(
                    judul = "Tidak ada di Tersimpan",
                    keterangan = "Laporan ini sudah tidak ada di daftar tersimpanmu.",
                    teksTombol = "Kembali",
                    onTombol = onKembali
                )

                error != null -> PesanKeadaanTersimpan(
                    judul = "Detail gagal dimuat",
                    keterangan = error,
                    teksTombol = "Coba lagi",
                    onTombol = onCobaLagi
                )
            }
        }
    }

    if (konfirmasiHapus) {
        AlertDialog(
            onDismissRequest = { konfirmasiHapus = false },
            title = { Text("Hapus dari Tersimpan?") },
            text = { Text("Laporan aslinya tidak ikut terhapus. Kamu bisa menyimpannya lagi kapan saja.") },
            confirmButton = {
                TextButton(onClick = {
                    konfirmasiHapus = false
                    onHapus()
                }) {
                    Text("Hapus", color = WarnaTersimpan.Merah)
                }
            },
            dismissButton = {
                TextButton(onClick = { konfirmasiHapus = false }) {
                    Text("Batal", color = WarnaTersimpan.TeksSekunder)
                }
            }
        )
    }
}

@Composable
private fun IsiDetail(
    detail: LaporanTersimpan,
    urlFoto: String?,
    menghapus: Boolean,
    onMintaHapus: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FotoTersimpan(
            url = urlFoto,
            adaFoto = detail.tersedia && detail.foto.isNotBlank(),
            deskripsi = "Foto ${detail.nama}",
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(BentukTersimpan.Foto)
        )

        if (detail.tersedia) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LabelJenis(hilang = detail.hilang)
                Text(
                    labelStatus(detail.status),
                    fontSize = 12.sp,
                    color = WarnaTersimpan.TeksSekunder,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Text(
            detail.nama,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = WarnaTersimpan.Teks
        )

        if (detail.tersedia) {
            BarisInfo("Kategori", kategoriBarang[detail.kategori] ?: detail.kategori.ifBlank { "-" })
            BarisInfo(if (detail.hilang) "Lokasi terakhir terlihat" else "Lokasi ditemukan", detail.lokasi)
            BarisInfo(if (detail.hilang) "Tanggal hilang" else "Tanggal ditemukan", tanggalTampil(detail.tanggal))
            BarisInfo("Deskripsi", detail.deskripsi.ifBlank { "-" })
        } else {
            Text(
                "Laporan ini sudah dihapus oleh pemiliknya, jadi detailnya tidak bisa ditampilkan lagi.",
                fontSize = 14.sp,
                color = WarnaTersimpan.TeksSekunder
            )
        }

        HorizontalDivider(color = WarnaTersimpan.Garis)
        BarisInfo("Disimpan pada", tanggalTampil(detail.disimpanPada))

        Button(
            onClick = onMintaHapus,
            enabled = !menghapus,
            modifier = Modifier.fillMaxWidth(),
            shape = BentukTersimpan.Tombol,
            colors = ButtonDefaults.buttonColors(containerColor = WarnaTersimpan.Merah)
        ) {
            Text(if (menghapus) "Menghapus..." else "Hapus dari Tersimpan")
        }
    }
}

@Composable
private fun BarisInfo(label: String, isi: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontSize = 12.sp, color = WarnaTersimpan.TeksSekunder)
        Text(isi, fontSize = 15.sp, color = WarnaTersimpan.Teks)
    }
}
