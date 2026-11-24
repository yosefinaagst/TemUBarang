package com.example.temu.ui.screens.laporansaya

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.example.temu.data.model.RiwayatStatus
import com.example.temu.data.model.StatusLaporan
import com.example.temu.data.model.ZONA_WAKTU_KAMPUS
import com.example.temu.data.model.kategoriBarang
import com.example.temu.ui.screens.hilang.BentukHilang
import com.example.temu.ui.screens.hilang.FotoLaporan
import com.example.temu.ui.screens.hilang.LabelStatus
import com.example.temu.ui.screens.hilang.PesanKeadaan
import com.example.temu.ui.screens.hilang.WarnaHilang
import com.example.temu.ui.screens.hilang.gayaStatus
import com.example.temu.ui.screens.hilang.tanggalPanjang
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * F1 Halaman 2 · Detail laporan milik sendiri + riwayat status (layar 08)
 * Aksi: Ubah status (disembunyikan bila Selesai), Edit laporan, Hapus laporan (dengan konfirmasi).
 */
@Composable
fun DetailLaporanSayaScreen(
    state: DetailLaporanSayaUiState,
    onKembali: () -> Unit,
    onCobaLagi: () -> Unit,
    onUbahStatus: () -> Unit,
    onEdit: () -> Unit,
    onHapus: () -> Unit,
    onPesanTampil: () -> Unit
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
            HeaderKembali(judul = "Detail laporan", onKembali = onKembali)
            HorizontalDivider(color = WarnaHilang.Garis)

            when {
                detail != null -> IsiDetail(
                    detail = detail,
                    riwayat = state.riwayat,
                    urlFoto = state.urlFoto,
                    menghapus = menghapus,
                    onUbahStatus = onUbahStatus,
                    onEdit = onEdit,
                    onMintaHapus = { konfirmasiHapus = true },
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
                            keterangan = "Laporan mungkin sudah dihapus.",
                            teksTombol = "Kembali",
                            onTombol = onKembali
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
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = WarnaHilang.Teks,
                contentColor = Color.White
            )
        }
    }

    if (konfirmasiHapus && detail != null) {
        AlertDialog(
            onDismissRequest = { if (!menghapus) konfirmasiHapus = false },
            title = { Text("Hapus laporan?") },
            text = {
                Text(
                    "Laporan ${detail.nama} akan hilang dari Laporan Saya dan Laporan Hilang. Riwayat tidak dihapus permanen."
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

/** Header dengan tombol kembali, dipakai layar detail dan ubah status. */
@Composable
internal fun HeaderKembali(judul: String, onKembali: () -> Unit) {
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
        Text(judul, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WarnaHilang.Teks)
    }
}

@Composable
private fun IsiDetail(
    detail: DetailHilang,
    riwayat: List<RiwayatStatus>,
    urlFoto: String?,
    menghapus: Boolean,
    onUbahStatus: () -> Unit,
    onEdit: () -> Unit,
    onMintaHapus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selesai = detail.status == StatusLaporan.SELESAI

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FotoLaporan(
            adaFoto = detail.foto.isNotBlank(),
            url = urlFoto,
            deskripsi = "Foto ${detail.nama}",
            teksTanpaFoto = "Tanpa foto",
            modifier = Modifier
                .fillMaxWidth()
                .height(if (detail.foto.isNotBlank()) 220.dp else 90.dp)
                .clip(BentukHilang.FotoDetail)
        )

        LabelStatus(detail.status, besar = true)

        Text(
            detail.nama,
            fontSize = 26.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            color = WarnaHilang.Teks
        )
        Text(
            "${kategoriBarang[detail.kategori] ?: "Lainnya"} · hilang ${tanggalPanjang(detail.tanggal)} · ${detail.lokasi}",
            color = WarnaHilang.TeksSekunder,
            fontSize = 13.sp
        )

        if (detail.deskripsi.isNotBlank()) {
            Text(detail.deskripsi, color = WarnaHilang.Teks, fontSize = 14.sp)
        }
        if (detail.ciriPublik.isNotBlank()) {
            Text("Ciri khusus: ${detail.ciriPublik}", color = WarnaHilang.TeksSekunder, fontSize = 13.sp)
        }

        HorizontalDivider(color = WarnaHilang.Garis)

        Text(
            "RIWAYAT STATUS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = WarnaHilang.TeksSekunder
        )
        if (riwayat.isEmpty()) {
            Text("Belum ada riwayat.", color = WarnaHilang.TeksSekunder, fontSize = 13.sp)
        } else {
            Column {
                riwayat.forEachIndexed { index, item ->
                    BarisRiwayat(item, terakhir = index == riwayat.lastIndex)
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        if (!selesai) {
            Button(
                onClick = onUbahStatus,
                enabled = !menghapus,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarnaHilang.Rose,
                    contentColor = Color.White
                )
            ) {
                Text("⇄  Ubah status", fontWeight = FontWeight.SemiBold)
            }
        } else {
            Text(
                "Laporan sudah selesai, jadi statusnya tidak bisa diubah lagi.",
                color = WarnaHilang.TeksSekunder,
                fontSize = 12.sp
            )
        }

        OutlinedButton(
            onClick = onEdit,
            enabled = !menghapus,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, WarnaHilang.Garis)
        ) {
            Text("✎  Edit laporan", color = WarnaHilang.Rose, fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
            onClick = onMintaHapus,
            enabled = !menghapus,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, WarnaHilang.Garis)
        ) {
            Text("Hapus laporan", color = WarnaHilang.Merah, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Satu titik pada garis waktu riwayat. */
@Composable
private fun BarisRiwayat(item: RiwayatStatus, terakhir: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(5.dp))
            Box(
                Modifier
                    .size(9.dp)
                    .background(WarnaHilang.Rose, CircleShape)
            )
            if (!terakhir) {
                Box(
                    Modifier
                        .width(1.dp)
                        .weight(1f)
                        .background(WarnaHilang.Garis)
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp, bottom = if (terakhir) 0.dp else 14.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                if (item.ke == StatusLaporan.SELESAI) "Selesai · Ditemukan" else gayaStatus(item.ke).label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = WarnaHilang.Teks
            )
            Text(waktuRiwayat(item.waktu), fontSize = 11.sp, color = WarnaHilang.TeksSekunder)
            if (item.catatan.isNotBlank()) {
                Text(item.catatan, fontSize = 13.sp, color = WarnaHilang.Teks)
            }
        }
    }
}

private val formatRiwayat = DateTimeFormatter.ofPattern("d MMM yyyy, HH.mm", Locale.forLanguageTag("id-ID"))

/** Timestamp database (UTC) → waktu kampus, mis. "4 Nov 2026, 14.05". */
internal fun waktuRiwayat(iso: String): String = try {
    OffsetDateTime.parse(iso).atZoneSameInstant(ZONA_WAKTU_KAMPUS).format(formatRiwayat)
} catch (_: DateTimeParseException) {
    iso.take(10)
}
