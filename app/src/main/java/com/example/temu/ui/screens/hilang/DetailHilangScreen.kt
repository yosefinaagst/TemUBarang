package com.example.temu.ui.screens.hilang

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temu.data.model.DetailHilang
import com.example.temu.data.model.kategoriBarang

/**
 * F1 · Detail laporan hilang (tampilan disamakan dengan Detail temuan F3)
 * - Pengguna lain : baca saja
 * - Pemilik       : Edit laporan, Hapus laporan (+ Ubah status bila fitur Status Saya tersambung)
 *
 * Screen ini TIDAK memakai ViewModel: hanya menerima [state] dan aksi (lambda).
 * [onUbahStatus]    → Detail di Laporan Saya (F1 Halaman 2). Null = tombol disembunyikan.
 * [onSimpanLaporan] → Tersimpan (F4). Null = tombol disembunyikan.
 */
@Composable
fun DetailHilangScreen(
    state: DetailHilangUiState,
    onKembali: () -> Unit,
    onCobaLagi: () -> Unit,
    onEdit: () -> Unit,
    onHapus: () -> Unit,
    onPesanTampil: () -> Unit,
    onUbahStatus: (() -> Unit)? = null,
    onSimpanLaporan: (() -> Unit)? = null
) {
    val detail = state.detail
    val error = state.error
    val menghapus = state.menghapus

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.pesan) {
        val pesan = state.pesan
        if (pesan != null) {
            snackbar.showSnackbar(pesan)
            onPesanTampil()
        }
    }

    var konfirmasiHapus by rememberSaveable { mutableStateOf(false) }

    // Dialog ditutup otomatis setelah penghapusan selesai / gagal.
    LaunchedEffect(menghapus) {
        if (!menghapus) konfirmasiHapus = false
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WarnaHilang.Latar)
                .safeDrawingPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onKembali,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Text("‹", fontSize = 32.sp, color = WarnaHilang.Rose)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Detail laporan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarnaHilang.Teks
                )
            }

            HorizontalDivider(color = WarnaHilang.Garis)

            when {
                detail != null -> IsiDetail(
                    detail = detail,
                    urlFoto = state.urlFoto,
                    menghapus = menghapus,
                    onEdit = onEdit,
                    onMintaHapus = { konfirmasiHapus = true },
                    onUbahStatus = onUbahStatus,
                    onSimpanLaporan = onSimpanLaporan,
                    modifier = Modifier.weight(1f)
                )

                error != null -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.tidakTersedia) {
                        PesanKeadaan(
                            judul = "Laporan ini sudah tidak tersedia.",
                            keterangan = "Pelapor mungkin sudah menghapusnya.",
                            teksTombol = null,
                            onTombol = {}
                        )
                    } else {
                        PesanKeadaan(
                            judul = "Detail belum bisa dimuat",
                            keterangan = error,
                            teksTombol = "Coba lagi",
                            onTombol = onCobaLagi
                        )
                    }
                }

                else -> Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WarnaHilang.Rose)
                }
            }

            HorizontalDivider(color = WarnaHilang.Garis)

            Button(
                onClick = onKembali,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .height(50.dp),
                shape = BentukHilang.Tombol,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarnaHilang.Rose,
                    contentColor = Color.White
                )
            ) {
                Text("Kembali ke Jelajah")
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 84.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = WarnaHilang.Teks,
                contentColor = Color.White
            )
        }
    }

    // Dialog konfirmasi penghapusan
    if (konfirmasiHapus && detail != null) {
        AlertDialog(
            onDismissRequest = { if (!menghapus) konfirmasiHapus = false },
            title = { Text("Hapus laporan?") },
            text = {
                Text(
                    "Laporan ${detail.nama} akan disembunyikan dari daftar barang hilang. Data riwayat tidak dihapus permanen."
                )
            },
            confirmButton = {
                TextButton(enabled = !menghapus, onClick = onHapus) {
                    if (menghapus) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Ya, hapus", color = WarnaHilang.Merah)
                    }
                }
            },
            dismissButton = {
                TextButton(enabled = !menghapus, onClick = { konfirmasiHapus = false }) {
                    Text("Batal", color = WarnaHilang.TeksSekunder)
                }
            }
        )
    }
}

@Composable
private fun IsiDetail(
    detail: DetailHilang,
    urlFoto: String?,
    menghapus: Boolean,
    onEdit: () -> Unit,
    onMintaHapus: () -> Unit,
    onUbahStatus: (() -> Unit)?,
    onSimpanLaporan: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        FotoLaporan(
            adaFoto = detail.foto.isNotBlank(),
            url = urlFoto,
            deskripsi = "Foto ${detail.nama}",
            teksTanpaFoto = "Pelapor tidak menyertakan foto",
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .clip(BentukHilang.FotoDetail)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LabelStatus(detail.status, besar = true)
            if (detail.milikSaya) {
                Text(
                    "Laporanmu",
                    modifier = Modifier
                        .background(WarnaHilang.Blush, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    color = WarnaHilang.Rose,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Text(
            detail.nama,
            fontSize = 27.sp,
            lineHeight = 33.sp,
            fontWeight = FontWeight.Bold,
            color = WarnaHilang.Teks
        )

        HorizontalDivider(color = WarnaHilang.Garis)

        InfoDetail("Kategori barang", kategoriBarang[detail.kategori] ?: "Lainnya")
        InfoDetail("Lokasi terakhir dilihat", detail.lokasi)
        InfoDetail("Tanggal kehilangan", tanggalPanjang(detail.tanggal))

        BagianTeks("Deskripsi barang", detail.deskripsi.ifBlank { "Tidak ada deskripsi." })
        if (detail.ciriPublik.isNotBlank()) {
            BagianTeks("Ciri khusus", detail.ciriPublik)
        }

        HorizontalDivider(color = WarnaHilang.Garis)

        InfoDetail("Pelapor", if (detail.milikSaya) "${detail.namaPelapor} (kamu)" else detail.namaPelapor)
        InfoDetail("Kontak pelapor", detail.kontak.ifBlank { "Pelapor tidak mencantumkan kontak." })

        if (detail.milikSaya) {
            // Hanya pemilik yang melihat tombol ini
            HorizontalDivider(color = WarnaHilang.Garis)

            Text("Kelola laporan", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WarnaHilang.Teks)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    enabled = !menghapus,
                    modifier = Modifier.weight(1f),
                    shape = BentukHilang.Tombol
                ) {
                    Text("Edit laporan", color = WarnaHilang.Rose)
                }
                Button(
                    onClick = onMintaHapus,
                    enabled = !menghapus,
                    modifier = Modifier.weight(1f),
                    shape = BentukHilang.Tombol,
                    colors = ButtonDefaults.buttonColors(containerColor = WarnaHilang.Merah)
                ) {
                    Text("Hapus laporan")
                }
            }

            if (onUbahStatus != null) {
                OutlinedButton(
                    onClick = onUbahStatus,
                    enabled = !menghapus,
                    modifier = Modifier.fillMaxWidth(),
                    shape = BentukHilang.Tombol
                ) {
                    Text("Kelola di Laporan Saya", color = WarnaHilang.Rose)
                }
            }
        } else if (onSimpanLaporan != null) {
            HorizontalDivider(color = WarnaHilang.Garis)
            OutlinedButton(
                onClick = onSimpanLaporan,
                modifier = Modifier.fillMaxWidth(),
                shape = BentukHilang.Tombol
            ) {
                Text("Simpan laporan", color = WarnaHilang.Rose)
            }
        }
    }
}

@Composable
private fun InfoDetail(label: String, nilai: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, fontSize = 12.sp, color = WarnaHilang.TeksSekunder)
        Text(
            nilai.ifBlank { "-" },
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = WarnaHilang.Teks
        )
    }
}

@Composable
private fun BagianTeks(judul: String, isi: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(judul, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WarnaHilang.Teks)
        Text(isi, fontSize = 14.sp, lineHeight = 22.sp, color = WarnaHilang.TeksSekunder)
    }
}
