package com.example.temu.ui.screens.tersimpan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.example.temu.data.model.JenisLaporan
import com.example.temu.data.model.PilihanLaporan
import com.example.temu.data.model.bucketFoto
import com.example.temu.data.repository.tersimpan.kunciFoto

/**
 * F4 · Halaman Pilih Laporan ("Tambah ke Tersimpan")
 * Daftar (LazyColumn) laporan hilang & ditemukan. Tombol "Simpan" menambahkan
 * laporan ke daftar tersimpan.
 */
@Composable
fun PilihLaporanScreen(
    state: PilihLaporanUiState,
    onKembali: () -> Unit,
    onPilihJenis: (String?) -> Unit,
    onSimpan: (String) -> Unit,
    onMuatUlang: () -> Unit,
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

    Scaffold(
        containerColor = WarnaTersimpan.Latar,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { HeaderTersimpan("Tambah ke Tersimpan", onKembali) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ---------- Filter jenis ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipJenis("Semua", state.jenis == null) { onPilihJenis(null) }
                ChipJenis("Hilang", state.jenis == JenisLaporan.HILANG) {
                    onPilihJenis(JenisLaporan.HILANG)
                }
                ChipJenis("Ditemukan", state.jenis == JenisLaporan.DITEMUKAN) {
                    onPilihJenis(JenisLaporan.DITEMUKAN)
                }
            }

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
                        judul = "Laporan gagal dimuat",
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
                        judul = "Belum ada laporan",
                        keterangan = "Belum ada laporan yang bisa disimpan untuk filter ini.",
                        teksTombol = null,
                        onTombol = {}
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.daftar, key = { it.id }) { laporan ->
                        val bucket = bucketFoto(laporan.jenis)
                        BarisPilihan(
                            laporan = laporan,
                            urlFoto = state.urlFoto[kunciFoto(bucket, laporan.foto)],
                            sudahTersimpan = laporan.id in state.idTersimpan,
                            sedangDisimpan = laporan.id in state.sedangDisimpan,
                            onSimpan = { onSimpan(laporan.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipJenis(label: String, aktif: Boolean, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier
            .clip(CircleShape)
            .background(if (aktif) WarnaTersimpan.Rose else WarnaTersimpan.Putih)
            .border(1.dp, if (aktif) WarnaTersimpan.Rose else WarnaTersimpan.Garis, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        fontSize = 13.sp,
        fontWeight = if (aktif) FontWeight.SemiBold else FontWeight.Normal,
        color = if (aktif) WarnaTersimpan.Putih else WarnaTersimpan.Teks
    )
}

/** Satu baris laporan: foto kecil, nama, lokasi & tanggal, tombol Simpan. */
@Composable
private fun BarisPilihan(
    laporan: PilihanLaporan,
    urlFoto: String?,
    sudahTersimpan: Boolean,
    sedangDisimpan: Boolean,
    onSimpan: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(BentukTersimpan.Kartu)
            .background(WarnaTersimpan.Putih)
            .border(1.dp, WarnaTersimpan.Garis, BentukTersimpan.Kartu)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FotoTersimpan(
            url = urlFoto,
            adaFoto = laporan.foto.isNotBlank(),
            deskripsi = "Foto ${laporan.nama}",
            modifier = Modifier
                .size(64.dp)
                .clip(BentukTersimpan.FotoKecil)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            LabelJenis(hilang = laporan.hilang)
            Text(
                laporan.nama,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = WarnaTersimpan.Teks,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${laporan.lokasi} · ${tanggalTampil(laporan.tanggal)}",
                fontSize = 12.sp,
                color = WarnaTersimpan.TeksSekunder,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (sudahTersimpan) {
            OutlinedButton(
                onClick = {},
                enabled = false,
                shape = BentukTersimpan.Tombol,
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Text("Tersimpan ✓", fontSize = 12.sp)
            }
        } else {
            Button(
                onClick = onSimpan,
                enabled = !sedangDisimpan,
                shape = BentukTersimpan.Tombol,
                contentPadding = PaddingValues(horizontal = 14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WarnaTersimpan.Rose)
            ) {
                Text(if (sedangDisimpan) "..." else "Simpan", fontSize = 12.sp)
            }
        }
    }
}
