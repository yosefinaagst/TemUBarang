package com.example.temu.ui.screens.hilang

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temu.R
import com.example.temu.data.model.LaporanHilang
import com.example.temu.data.model.StatusLaporan
import com.example.temu.data.model.kategoriBarang

/**
 * F1 Halaman 1 · Jelajah · tab Hilang
 * Tampilan disamakan dengan Jelajah · Ditemukan (F3).
 * Screen ini TIDAK memakai ViewModel: hanya menerima [state] dan aksi (lambda).
 * ViewModel-nya dipasang di navigasi (NavigasiHilang.kt).
 */
@Composable
fun JelajahHilangScreen(
    state: JelajahHilangUiState,
    onKataKunci: (String) -> Unit,
    onKategori: (String?) -> Unit,
    onTerapkanFilter: (String?, Set<String>) -> Unit,
    onReset: () -> Unit,
    onMuatUlang: () -> Unit,
    onMuatLagi: () -> Unit,
    onPesanTampil: () -> Unit,
    onBukaDitemukan: () -> Unit,
    onTambah: () -> Unit,
    onDetail: (String) -> Unit,
    onNavigasiBawah: (Int) -> Unit = {}
) {
    val daftar = state.daftar
    val error = state.error
    val memuat = state.memuat

    // Snackbar sekali tampil (mis. "Laporan dihapus.")
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.pesan) {
        val pesan = state.pesan
        if (pesan != null) {
            snackbar.showSnackbar(pesan)
            onPesanTampil()
        }
    }

    var tampilFilter by rememberSaveable { mutableStateOf(false) }
    val gridState = rememberLazyGridState()

    // Pagination: muat 20 laporan berikutnya saat item ke-3 terakhir terlihat.
    val dekatAkhir by remember {
        derivedStateOf {
            val info = gridState.layoutInfo
            val terakhir = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            info.totalItemsCount > 0 && terakhir >= info.totalItemsCount - 3
        }
    }
    LaunchedEffect(dekatAkhir, daftar.size) {
        if (dekatAkhir) onMuatLagi()
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 92.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        KepalaJelajah(
                            kataKunci = state.kataKunci,
                            kategori = state.kategori,
                            statusDipilih = state.statusDipilih,
                            jumlahFilterAktif = state.jumlahFilterAktif,
                            adaPencarianAtauFilter = state.adaPencarianAtauFilter,
                            totalHasil = state.totalHasil,
                            memuat = memuat,
                            adaData = daftar.isNotEmpty(),
                            adaError = error != null,
                            onKataKunci = onKataKunci,
                            onKategori = onKategori,
                            onBukaFilter = { tampilFilter = true },
                            onReset = onReset,
                            onBukaDitemukan = onBukaDitemukan
                        )
                    }

                    when {
                        // Pertama kali memuat: kartu kerangka
                        memuat && daftar.isEmpty() -> items(4) { KartuKerangka() }

                        error != null && daftar.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                            PesanKeadaan(
                                judul = "Koneksi bermasalah",
                                keterangan = error,
                                teksTombol = "Coba lagi",
                                onTombol = onMuatUlang
                            )
                        }

                        daftar.isEmpty() && state.adaPencarianAtauFilter -> item(span = { GridItemSpan(maxLineSpan) }) {
                            PesanKeadaan(
                                judul = "Barang yang kamu cari belum terlihat.",
                                keterangan = "Coba kata kunci lain atau reset filter.",
                                teksTombol = "Reset pencarian & filter",
                                onTombol = onReset
                            )
                        }

                        daftar.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                            PesanKeadaan(
                                judul = "Belum ada laporan kehilangan.",
                                keterangan = "Laporan barang hilang dari mahasiswa akan tampil di sini.",
                                teksTombol = "Laporkan kehilangan",
                                onTombol = onTambah
                            )
                        }

                        else -> {
                            items(daftar, key = { it.id }) { laporan ->
                                KartuHilang(
                                    laporan = laporan,
                                    urlFoto = state.urlFoto[laporan.foto],
                                    onClick = { onDetail(laporan.id) }
                                )
                            }
                            if (state.memuatLagi) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp,
                                            color = WarnaHilang.Rose
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = onTambah,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(14.dp),
                    containerColor = WarnaHilang.Rose,
                    contentColor = Color.White
                ) {
                    Text("+   Laporkan kehilangan")
                }

                SnackbarHost(
                    hostState = snackbar,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = 80.dp)
                ) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = WarnaHilang.Teks,
                        contentColor = Color.White
                    )
                }
            }

            NavigasiBawahJelajah(onNavigasiBawah)
        }
    }

    if (tampilFilter) {
        SheetFilter(
            kategoriAwal = state.kategori,
            statusAwal = state.statusDipilih,
            onTutup = { tampilFilter = false },
            onTerapkan = { k, s ->
                tampilFilter = false
                onTerapkanFilter(k, s)
            }
        )
    }
}

// ---------------------------------------------------------------------
// Kepala: sapaan, judul, pencarian + filter, Hilang|Ditemukan, kategori
// ---------------------------------------------------------------------

@Composable
private fun KepalaJelajah(
    kataKunci: String,
    kategori: String?,
    statusDipilih: Set<String>,
    jumlahFilterAktif: Int,
    adaPencarianAtauFilter: Boolean,
    totalHasil: Int,
    memuat: Boolean,
    adaData: Boolean,
    adaError: Boolean,
    onKataKunci: (String) -> Unit,
    onKategori: (String?) -> Unit,
    onBukaFilter: () -> Unit,
    onReset: () -> Unit,
    onBukaDitemukan: () -> Unit
) {
    val fokus = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Halo!", color = WarnaHilang.TeksSekunder)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(WarnaHilang.Blush, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Glyph(R.drawable.home_profil, label = "Profil", color = WarnaHilang.Rose)
            }
        }

        Text(
            text = "Temukan kembali\nbarangmu.",
            fontSize = 32.sp,
            lineHeight = 35.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold
        )

        // Pencarian + tombol filter
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .background(Color.White, BentukHilang.Kartu)
                    .border(1.dp, WarnaHilang.Garis, BentukHilang.Kartu)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                BasicTextField(
                    value = kataKunci,
                    onValueChange = onKataKunci,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = TextStyle(color = WarnaHilang.Teks, fontSize = 15.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { fokus.clearFocus() }),
                    decorationBox = { isian ->
                        Box {
                            if (kataKunci.isEmpty()) {
                                Text("Cari nama barang…", color = WarnaHilang.TeksSekunder, fontSize = 15.sp)
                            }
                            isian()
                        }
                    }
                )
                if (kataKunci.isNotEmpty()) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clickable { onKataKunci("") },
                        contentAlignment = Alignment.Center
                    ) {
                        Glyph(R.drawable.ic_hilang_tutup, label = "Hapus pencarian", size = 16)
                    }
                }
            }

            val filterAktif = jumlahFilterAktif > 0
            Box {
                OutlinedIconButton(
                    onClick = onBukaFilter,
                    modifier = Modifier.size(48.dp),
                    shape = BentukHilang.Kartu,
                    border = BorderStroke(1.dp, if (filterAktif) WarnaHilang.Rose else WarnaHilang.Garis),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = if (filterAktif) WarnaHilang.Rose else Color.Transparent
                    )
                ) {
                    Glyph(
                        R.drawable.ic_hilang_filter,
                        label = "Filter laporan",
                        color = if (filterAktif) Color.White else WarnaHilang.Rose
                    )
                }
                if (filterAktif) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .size(18.dp)
                            .background(WarnaHilang.Teks, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$jumlahFilterAktif", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Jenis laporan: Hilang aktif, Ditemukan membuka layar F3
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WarnaHilang.Blush, BentukHilang.Kartu)
                .padding(4.dp)
        ) {
            listOf("Hilang", "Ditemukan").forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = index == 1, onClick = onBukaDitemukan)
                        .background(
                            if (index == 0) Color.White else WarnaHilang.Blush,
                            BentukHilang.Kartu
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (index == 0) WarnaHilang.Rose else WarnaHilang.TeksSekunder,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Filter kategori
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ChipKategori("Semua", kategori == null) { onKategori(null) }
            kategoriBarang.forEach { (kode, label) ->
                ChipKategori(label, kategori == kode) { onKategori(kode) }
            }
        }

        if (memuat && adaData) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = WarnaHilang.Rose)
        }

        // Ringkasan hasil pencarian / filter
        if (adaPencarianAtauFilter && !adaError && !(memuat && !adaData)) {
            val bagian = buildList {
                if (kataKunci.isNotBlank()) add("untuk \"${kataKunci.trim()}\"")
                if (statusDipilih.isNotEmpty()) {
                    add(StatusLaporan.semua.filter { it in statusDipilih }.joinToString(", ") { gayaStatus(it).label })
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    buildString {
                        append("$totalHasil hasil")
                        if (bagian.isNotEmpty()) append(" ").append(bagian.joinToString(" · "))
                    },
                    modifier = Modifier.weight(1f),
                    color = WarnaHilang.TeksSekunder,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                TextButton(onClick = onReset) {
                    Text("Reset", color = WarnaHilang.Rose, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ChipKategori(label: String, terpilih: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (terpilih) WarnaHilang.Rose else Color.White,
        contentColor = if (terpilih) Color.White else WarnaHilang.Teks,
        border = BorderStroke(1.dp, if (terpilih) WarnaHilang.Rose else WarnaHilang.Garis)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            fontSize = 13.sp
        )
    }
}

// ---------------------------------------------------------------------
// Kartu grid (sama dengan kartu temuan)
// ---------------------------------------------------------------------

@Composable
private fun KartuHilang(
    laporan: LaporanHilang,
    urlFoto: String?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = BentukHilang.Kartu,
        color = Color.White,
        contentColor = WarnaHilang.Teks,
        border = BorderStroke(1.dp, WarnaHilang.Garis),
        shadowElevation = 1.dp
    ) {
        Column {
            FotoLaporan(
                adaFoto = laporan.foto.isNotBlank(),
                url = urlFoto,
                deskripsi = laporan.nama,
                teksTanpaFoto = "Tanpa foto",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            Column(
                modifier = Modifier.padding(11.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                LabelStatus(laporan.status)
                Text(
                    laporan.nama,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Meta(R.drawable.home_kategori, kategoriBarang[laporan.kategori] ?: "Lainnya")
                Meta(R.drawable.home_lokasi, laporan.lokasi)
            }
        }
    }
}

@Composable
private fun Meta(ikon: Int, nilai: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Glyph(ikon, size = 13)
        Text(
            nilai,
            color = WarnaHilang.TeksSekunder,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun KartuKerangka() {
    Column(
        Modifier
            .background(Color.White, BentukHilang.Kartu)
            .border(1.dp, WarnaHilang.Garis, BentukHilang.Kartu)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(WarnaHilang.Blush, BentukHilang.Kartu)
        )
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(width = 72.dp, height = 16.dp).background(WarnaHilang.Blush, CircleShape))
            Box(Modifier.fillMaxWidth(0.8f).height(14.dp).background(WarnaHilang.Blush, CircleShape))
            Box(Modifier.fillMaxWidth(0.6f).height(12.dp).background(WarnaHilang.Blush, CircleShape))
        }
    }
}

// ---------------------------------------------------------------------
// Bottom sheet filter kategori + status
// ---------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SheetFilter(
    kategoriAwal: String?,
    statusAwal: Set<String>,
    onTutup: () -> Unit,
    onTerapkan: (String?, Set<String>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var kategori by remember { mutableStateOf(kategoriAwal) }
    var status by remember { mutableStateOf(statusAwal) }

    val keterangan = mapOf(
        StatusLaporan.DILAPORKAN to "Baru diterbitkan, belum ditangani.",
        StatusLaporan.DALAM_PROSES to "Sedang dicek atau menunggu serah terima.",
        StatusLaporan.SELESAI to "Barang sudah kembali ke pemiliknya."
    )

    ModalBottomSheet(
        onDismissRequest = onTutup,
        sheetState = sheetState,
        containerColor = WarnaHilang.Latar
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Filter laporan", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WarnaHilang.Teks)

            Text("Kategori", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WarnaHilang.Teks)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChipKategori("Semua", kategori == null) { kategori = null }
                kategoriBarang.forEach { (kode, label) ->
                    ChipKategori(label, kategori == kode) { kategori = kode }
                }
            }

            Text("Status", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = WarnaHilang.Teks)
            StatusLaporan.semua.forEach { kode ->
                val dipilih = kode in status
                val ubah = { status = if (dipilih) status - kode else status + kode }
                Surface(
                    onClick = ubah,
                    shape = BentukHilang.Isian,
                    color = Color.White,
                    border = BorderStroke(1.dp, if (dipilih) WarnaHilang.Rose else WarnaHilang.GarisForm)
                ) {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = dipilih,
                            onCheckedChange = { ubah() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = WarnaHilang.Rose,
                                uncheckedColor = WarnaHilang.TeksSekunder,
                                checkmarkColor = Color.White
                            )
                        )
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (kode == StatusLaporan.SELESAI) "Selesai" else gayaStatus(kode).label,
                                color = WarnaHilang.Teks,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(keterangan[kode].orEmpty(), color = WarnaHilang.TeksSekunder, fontSize = 12.sp)
                        }
                        Spacer(Modifier.size(8.dp))
                        LabelStatus(kode)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        kategori = null
                        status = emptySet()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("Reset", color = WarnaHilang.TeksSekunder)
                }
                Button(
                    onClick = { onTerapkan(kategori, status) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = BentukHilang.Isian,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarnaHilang.Rose,
                        contentColor = Color.White
                    )
                ) {
                    Text("Terapkan")
                }
            }
        }
    }
}
