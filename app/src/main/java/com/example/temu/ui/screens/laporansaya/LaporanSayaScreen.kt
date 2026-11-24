package com.example.temu.ui.screens.laporansaya

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temu.R
import com.example.temu.data.model.LaporanHilang
import com.example.temu.data.model.StatusLaporan
import com.example.temu.ui.screens.hilang.BentukHilang
import com.example.temu.ui.screens.hilang.FotoLaporan
import com.example.temu.ui.screens.hilang.Glyph
import com.example.temu.ui.screens.hilang.LabelStatus
import com.example.temu.ui.screens.hilang.NavigasiBawahJelajah
import com.example.temu.ui.screens.hilang.PesanKeadaan
import com.example.temu.ui.screens.hilang.WarnaHilang
import com.example.temu.ui.screens.hilang.tanggalPanjang
import kotlinx.coroutines.launch

/**
 * F1 Halaman 2 · Laporan Saya (layar 07)
 * ViewPager (HorizontalPager) tiga tab: Dilaporkan, Dalam Proses, Selesai.
 * Tiap tab berisi LazyColumn laporan hilang milik akun aktif.
 * Screen tidak memakai ViewModel; ViewModel dipasang di NavigasiLaporanSaya.kt.
 *
 * @param tabDiminta indeks tab yang harus dibuka (mis. setelah simpan status); null = tidak ada.
 */
@Composable
fun LaporanSayaScreen(
    state: LaporanSayaUiState,
    tabAwal: Int,
    tabDiminta: Int?,
    onTabDibuka: () -> Unit,
    onMuatUlang: (String) -> Unit,
    onMuatLagi: (String) -> Unit,
    onPesanTampil: () -> Unit,
    onDetail: (String) -> Unit,
    onBukaJelajah: () -> Unit,
    onNavigasiBawah: (Int) -> Unit
) {
    val semuaStatus = StatusLaporan.semua
    val pager = rememberPagerState(initialPage = tabAwal.coerceIn(0, semuaStatus.lastIndex)) { semuaStatus.size }
    val scope = rememberCoroutineScope()

    // Pindah tab otomatis (setelah status disimpan → tab status terbaru).
    LaunchedEffect(tabDiminta) {
        if (tabDiminta != null) {
            pager.animateScrollToPage(tabDiminta.coerceIn(0, semuaStatus.lastIndex))
            onTabDibuka()
        }
    }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.pesan) {
        val pesan = state.pesan
        if (pesan != null) {
            snackbar.showSnackbar(pesan)
            onPesanTampil()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarnaHilang.Latar,
        contentColor = WarnaHilang.Teks
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            // Judul
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Laporan saya",
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Hanya laporan milikmu. Di sini kamu bisa mengubah, menghapus, dan memperbarui status.",
                    color = WarnaHilang.TeksSekunder,
                    fontSize = 13.sp
                )
            }

            // Tab dengan jumlah; ketuk atau geser untuk berpindah
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .background(WarnaHilang.Blush, BentukHilang.Kartu)
                    .padding(4.dp)
            ) {
                semuaStatus.forEachIndexed { index, status ->
                    val aktif = pager.currentPage == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .clip(BentukHilang.Kartu)
                            .background(if (aktif) Color.White else WarnaHilang.Blush)
                            .clickable { scope.launch { pager.animateScrollToPage(index) } },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${labelTab(status)} (${state.jumlah.untuk(status)})",
                            color = if (aktif) WarnaHilang.Rose else WarnaHilang.TeksSekunder,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                HorizontalPager(
                    state = pager,
                    modifier = Modifier.fillMaxSize(),
                    key = { semuaStatus[it] }
                ) { halaman ->
                    val status = semuaStatus[halaman]
                    IsiTab(
                        status = status,
                        tab = state.isiTab(status),
                        onMuatUlang = { onMuatUlang(status) },
                        onMuatLagi = { onMuatLagi(status) },
                        onDetail = onDetail,
                        onBukaJelajah = onBukaJelajah
                    )
                }

                SnackbarHost(
                    hostState = snackbar,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = WarnaHilang.Teks,
                        contentColor = Color.White
                    )
                }
            }

            NavigasiBawahJelajah(onPilih = onNavigasiBawah, aktif = INDEKS_NAV_LAPORAN_SAYA)
        }
    }
}

/** Posisi "Laporan Saya" pada navigasi bawah. */
const val INDEKS_NAV_LAPORAN_SAYA = 3

internal fun labelTab(status: String): String = when (status) {
    StatusLaporan.DALAM_PROSES -> "Dalam Proses"
    StatusLaporan.SELESAI -> "Selesai"
    else -> "Dilaporkan"
}

@Composable
private fun IsiTab(
    status: String,
    tab: TabLaporanSaya,
    onMuatUlang: () -> Unit,
    onMuatLagi: () -> Unit,
    onDetail: (String) -> Unit,
    onBukaJelajah: () -> Unit
) {
    val listState = rememberLazyListState()

    // Pagination: muat 20 berikutnya saat item ke-3 terakhir terlihat.
    val dekatAkhir by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val terakhir = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && terakhir >= info.totalItemsCount - 3
        }
    }
    LaunchedEffect(dekatAkhir, tab.daftar.size) {
        if (dekatAkhir) onMuatLagi()
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when {
            tab.memuat && tab.daftar.isEmpty() -> items(3) { BarisKerangka() }

            tab.error != null && tab.daftar.isEmpty() -> item {
                PesanKeadaan(
                    judul = "Laporan belum bisa dimuat",
                    keterangan = tab.error,
                    teksTombol = "Coba lagi",
                    onTombol = onMuatUlang
                )
            }

            tab.daftar.isEmpty() -> item {
                when (status) {
                    StatusLaporan.DILAPORKAN -> PesanKeadaan(
                        judul = "Belum ada laporan berstatus Dilaporkan.",
                        keterangan = "Laporan kehilangan yang kamu terbitkan di Laporan Hilang akan muncul di sini.",
                        teksTombol = "Buka Laporan Hilang",
                        onTombol = onBukaJelajah
                    )
                    StatusLaporan.DALAM_PROSES -> PesanKeadaan(
                        judul = "Belum ada laporan Dalam Proses.",
                        keterangan = "Laporan pindah ke sini setelah kamu mengubah statusnya, misalnya saat ada penemu yang menghubungimu.",
                        teksTombol = null,
                        onTombol = {}
                    )
                    else -> PesanKeadaan(
                        judul = "Belum ada laporan Selesai.",
                        keterangan = "Laporan muncul di sini setelah barangmu kembali dan statusnya kamu tandai Selesai.",
                        teksTombol = null,
                        onTombol = {}
                    )
                }
            }

            else -> {
                items(tab.daftar, key = { it.id }) { laporan ->
                    BarisLaporan(
                        laporan = laporan,
                        urlFoto = tab.urlFoto[laporan.foto],
                        onClick = { onDetail(laporan.id) }
                    )
                }
                if (tab.memuatLagi) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = WarnaHilang.Rose
                            )
                        }
                    }
                }
                item {
                    Text(
                        "Geser untuk melihat tab lainnya.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        color = WarnaHilang.TeksSekunder,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun BarisLaporan(
    laporan: LaporanHilang,
    urlFoto: String?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = BentukHilang.Kartu,
        color = Color.White,
        contentColor = WarnaHilang.Teks,
        border = BorderStroke(1.dp, WarnaHilang.Garis)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (laporan.foto.isNotBlank()) {
                FotoLaporan(
                    adaFoto = true,
                    url = urlFoto,
                    deskripsi = laporan.nama,
                    teksTanpaFoto = "",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(BentukHilang.Isian)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(WarnaHilang.Blush, BentukHilang.Isian),
                    contentAlignment = Alignment.Center
                ) {
                    Glyph(R.drawable.home_kategori, color = WarnaHilang.Rose, size = 22)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    laporan.nama,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${laporan.lokasi} · ${tanggalPanjang(laporan.tanggal)}",
                    color = WarnaHilang.TeksSekunder,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                LabelStatus(laporan.status)
            }

            Text("›", fontSize = 26.sp, color = WarnaHilang.TeksSekunder)
        }
    }
}

@Composable
private fun BarisKerangka() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, BentukHilang.Kartu)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(56.dp).background(WarnaHilang.Blush, BentukHilang.Isian))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(width = 140.dp, height = 14.dp).background(WarnaHilang.Blush, CircleShape))
            Box(Modifier.size(width = 100.dp, height = 12.dp).background(WarnaHilang.Blush, CircleShape))
            Box(Modifier.size(width = 70.dp, height = 14.dp).background(WarnaHilang.Blush, CircleShape))
        }
    }
}
