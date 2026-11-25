package com.example.temu.ui.screens.tersimpan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temu.data.model.LaporanTersimpan
import com.example.temu.data.model.bucketFoto
import com.example.temu.data.repository.tersimpan.kunciFoto
import com.example.temu.ui.screens.components.NavigasiBawah
import com.example.temu.ui.screens.components.TAB_TERSIMPAN

/**
 * F4 · Halaman Daftar Tersimpan
 * Grid (LazyVerticalGrid) kartu laporan tersimpan + tombol "Tambah ke Tersimpan".
 * Screen ini TIDAK memakai ViewModel: hanya menerima [state] dan aksi (lambda).
 */
@Composable
fun DaftarTersimpanScreen(
    state: DaftarTersimpanUiState,
    onMuatUlang: () -> Unit,
    onTambah: () -> Unit,
    onDetail: (String) -> Unit,
    onPesanTampil: () -> Unit,
    onNavigasiBawah: (Int) -> Unit
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.pesan) {
        val pesan = state.pesan
        if (pesan != null) {
            snackbar.showSnackbar(pesan)
            onPesanTampil()
        }
    }

    Scaffold(
        containerColor = WarnaTersimpan.Latar,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigasiBawah(
                terpilih = TAB_TERSIMPAN,
                onPilih = onNavigasiBawah,
                tambahInsetBawah = true
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onTambah,
                containerColor = WarnaTersimpan.Rose,
                contentColor = WarnaTersimpan.Putih
            ) {
                Text("+   Tambah ke Tersimpan")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ---------- Judul ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Tersimpan",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarnaTersimpan.Teks
                    )
                    Text(
                        "${state.daftar.size} laporan di koleksimu",
                        fontSize = 13.sp,
                        color = WarnaTersimpan.TeksSekunder
                    )
                }
                TextButton(onClick = onMuatUlang, enabled = !state.memuat) {
                    Text("Muat ulang", color = WarnaTersimpan.Rose)
                }
            }

            // ---------- Isi ----------
            val error = state.error
            when {
                state.memuat && state.daftar.isEmpty() -> Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WarnaTersimpan.Rose)
                }

                error != null && state.daftar.isEmpty() -> Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PesanKeadaanTersimpan(
                        judul = "Daftar tersimpan gagal dimuat",
                        keterangan = error,
                        teksTombol = "Coba lagi",
                        onTombol = onMuatUlang
                    )
                }

                state.daftar.isEmpty() -> Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PesanKeadaanTersimpan(
                        judul = "Belum ada laporan tersimpan",
                        keterangan = "Simpan laporan barang hilang atau ditemukan supaya mudah kamu pantau.",
                        teksTombol = "Tambah ke Tersimpan",
                        onTombol = onTambah
                    )
                }

                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.daftar, key = { it.reportId }) { laporan ->
                        val bucket = bucketFoto(laporan.jenis)
                        KartuTersimpan(
                            laporan = laporan,
                            urlFoto = state.urlFoto[kunciFoto(bucket, laporan.foto)],
                            onClick = { onDetail(laporan.reportId) }
                        )
                    }
                }
            }
        }
    }
}

/** Kartu laporan tersimpan: foto, label Hilang/Ditemukan, nama barang, lokasi. */
@Composable
private fun KartuTersimpan(
    laporan: LaporanTersimpan,
    urlFoto: String?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BentukTersimpan.Kartu)
            .background(WarnaTersimpan.Putih)
            .border(1.dp, WarnaTersimpan.Garis, BentukTersimpan.Kartu)
            .clickable(onClick = onClick)
    ) {
        Box {
            FotoTersimpan(
                url = urlFoto,
                adaFoto = laporan.tersedia && laporan.foto.isNotBlank(),
                deskripsi = "Foto ${laporan.nama}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            if (laporan.tersedia) {
                LabelJenis(
                    hilang = laporan.hilang,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                laporan.nama,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarnaTersimpan.Teks,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                if (laporan.tersedia) "📍 ${laporan.lokasi}" else "Sudah dihapus pemiliknya",
                fontSize = 12.sp,
                color = WarnaTersimpan.TeksSekunder,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
