package com.example.temu.ui.screens.hilang

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.temu.data.model.BatasFormHilang
import com.example.temu.data.model.FormHilang
import com.example.temu.data.model.KolomFormHilang
import com.example.temu.data.model.kategoriBarang
import com.example.temu.data.model.ZONA_WAKTU_KAMPUS
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * F1 · Form tambah & edit laporan hilang (tampilan disamakan dengan form temuan F3)
 * Foto barang opsional (JPG/PNG, maks. 5 MB) → Supabase Storage bucket "foto-hilang".
 * Screen ini TIDAK memakai ViewModel: hanya menerima [state] dan aksi (lambda).
 */
@Composable
fun FormHilangScreen(
    state: FormHilangUiState,
    onUbah: (FormHilang) -> Unit,
    onPilihFoto: (String) -> Unit,
    onHapusFoto: () -> Unit,
    onKirim: () -> Unit,
    onMuatUlang: () -> Unit,
    onKembali: () -> Unit
) {
    val modeEdit = state.modeEdit
    val form = state.form
    val errorKolom = state.errorKolom
    val errorForm = state.errorForm
    val mengirim = state.mengirim
    val memuat = state.memuat

    var menuKategori by remember { mutableStateOf(false) }
    var tampilTanggal by rememberSaveable { mutableStateOf(false) }
    var konfirmasiKeluar by rememberSaveable { mutableStateOf(false) }
    val sibuk = mengirim || memuat

    val pilihFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) onPilihFoto(uri.toString())
    }

    val cobaKembali: () -> Unit = {
        if (mengirim) {
            // Tunggu proses kirim selesai.
        } else if (state.formBerubah) {
            konfirmasiKeluar = true
        } else {
            onKembali()
        }
    }
    BackHandler(onBack = cobaKembali)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WarnaHilang.Latar,
        contentColor = WarnaHilang.Teks
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = cobaKembali,
                    enabled = !mengirim,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, WarnaHilang.GarisForm, CircleShape)
                        .semantics { contentDescription = "Kembali" }
                ) {
                    PanahKembali()
                }
                Text(
                    if (modeEdit) "Edit laporan hilang" else "Laporkan kehilangan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = WarnaHilang.GarisForm)

            if (memuat) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WarnaHilang.Rose)
                }
            } else {
                // Isi form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Kolom bertanda * wajib diisi.", color = WarnaHilang.TeksSekunder, fontSize = 14.sp)

                    if (modeEdit) {
                        Text(
                            "Status laporan tidak diubah di sini. Ubah melalui menu Status Saya.",
                            color = WarnaHilang.TeksSekunder,
                            fontSize = 13.sp
                        )
                    }

                    if (errorForm != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFCEDEC), BentukHilang.Isian)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                errorForm,
                                modifier = Modifier.weight(1f),
                                color = WarnaHilang.Merah,
                                fontSize = 13.sp
                            )
                            if (state.konflikVersi) {
                                TextButton(onClick = onMuatUlang) {
                                    Text("Muat ulang", color = WarnaHilang.Rose, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    // Foto barang (opsional)
                    KelompokForm("Foto barang", wajib = false) {
                        AreaFoto(
                            fotoBaru = form.fotoBaru,
                            adaFotoLama = form.fotoLama.isNotBlank(),
                            urlFotoLama = state.urlFotoLama,
                            aktif = !sibuk,
                            onPilih = { pilihFoto.launch(arrayOf("image/jpeg", "image/png")) }
                        )
                        if (form.fotoBaru != null || form.fotoLama.isNotBlank()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = { pilihFoto.launch(arrayOf("image/jpeg", "image/png")) },
                                    enabled = !sibuk
                                ) {
                                    Text("Ganti foto", color = WarnaHilang.Rose)
                                }
                                TextButton(onClick = onHapusFoto, enabled = !sibuk) {
                                    Text("Hapus foto", color = WarnaHilang.Merah)
                                }
                            }
                        } else {
                            Text(
                                "Foto tidak wajib, tetapi membantu orang lain mengenali barangmu.",
                                color = WarnaHilang.TeksSekunder,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Nama barang
                    KelompokForm("Nama barang") {
                        IsianForm(
                            nilai = form.nama,
                            ubah = { onUbah(form.copy(nama = it.take(BatasFormHilang.NAMA))) },
                            placeholder = "Contoh: Dompet hitam",
                            aktif = !sibuk,
                            error = errorKolom[KolomFormHilang.NAMA]
                        )
                    }

                    // Kategori
                    KelompokForm("Kategori") {
                        Box {
                            TombolPilihan(
                                teks = kategoriBarang[form.kategori] ?: "Pilih kategori",
                                terisi = form.kategori.isNotBlank(),
                                aktif = !sibuk,
                                error = errorKolom[KolomFormHilang.KATEGORI] != null,
                                onClick = { menuKategori = true }
                            )
                            DropdownMenu(
                                expanded = menuKategori,
                                onDismissRequest = { menuKategori = false }
                            ) {
                                kategoriBarang.forEach { (kode, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            menuKategori = false
                                            onUbah(form.copy(kategori = kode))
                                        }
                                    )
                                }
                            }
                        }
                        PesanError(errorKolom[KolomFormHilang.KATEGORI])
                    }

                    // Deskripsi
                    KelompokForm("Deskripsi") {
                        IsianForm(
                            nilai = form.deskripsi,
                            ubah = { onUbah(form.copy(deskripsi = it.take(BatasFormHilang.DESKRIPSI))) },
                            placeholder = "Ceritakan bentuk, warna, dan kapan terakhir kamu melihatnya.",
                            aktif = !sibuk,
                            error = errorKolom[KolomFormHilang.DESKRIPSI],
                            banyakBaris = true
                        )
                        Bantuan(
                            "Jangan cantumkan data sensitif seperti nomor kartu atau PIN.",
                            "${form.deskripsi.length}/${BatasFormHilang.DESKRIPSI}"
                        )
                    }

                    // Lokasi
                    KelompokForm("Lokasi terakhir dilihat") {
                        IsianForm(
                            nilai = form.lokasi,
                            ubah = { onUbah(form.copy(lokasi = it.take(120))) },
                            placeholder = "Contoh: Gedung F, lantai 1",
                            aktif = !sibuk,
                            error = errorKolom[KolomFormHilang.LOKASI]
                        )
                    }

                    // Tanggal
                    KelompokForm("Tanggal kehilangan") {
                        TombolPilihan(
                            teks = form.tanggal?.let { tanggalPanjang(it) } ?: "Pilih tanggal",
                            terisi = form.tanggal != null,
                            aktif = !sibuk,
                            error = errorKolom[KolomFormHilang.TANGGAL] != null,
                            onClick = { tampilTanggal = true }
                        )
                        PesanError(errorKolom[KolomFormHilang.TANGGAL])
                    }

                    // Ciri khusus (opsional)
                    KelompokForm("Ciri khusus", wajib = false) {
                        IsianForm(
                            nilai = form.ciriPublik,
                            ubah = { onUbah(form.copy(ciriPublik = it.take(BatasFormHilang.CIRI))) },
                            placeholder = "Contoh: Ada gantungan kecil berbentuk bintang.",
                            aktif = !sibuk,
                            error = null,
                            banyakBaris = true
                        )
                        Bantuan(
                            "Tulis ciri yang aman dilihat semua orang. Simpan ciri paling khas untuk memeriksa klaim.",
                            "${form.ciriPublik.length}/${BatasFormHilang.CIRI}"
                        )
                    }
                }
            }

            HorizontalDivider(color = WarnaHilang.GarisForm)

            // Tombol kirim
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onKirim,
                    enabled = !sibuk,
                    shape = BentukHilang.Isian,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarnaHilang.Rose,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        when {
                            mengirim && modeEdit -> "Menyimpan..."
                            mengirim -> "Menerbitkan..."
                            modeEdit -> "Simpan perubahan"
                            else -> "Terbitkan laporan"
                        }
                    )
                }
            }
        }
    }

    if (tampilTanggal) {
        DialogTanggal(
            awal = form.tanggal,
            onPilih = {
                tampilTanggal = false
                onUbah(form.copy(tanggal = it))
            },
            onTutup = { tampilTanggal = false }
        )
    }

    if (konfirmasiKeluar) {
        AlertDialog(
            onDismissRequest = { konfirmasiKeluar = false },
            title = { Text("Buang perubahan?") },
            text = { Text("Isian yang belum disimpan akan hilang.") },
            confirmButton = {
                TextButton(onClick = {
                    konfirmasiKeluar = false
                    onKembali()
                }) {
                    Text("Buang", color = WarnaHilang.Merah)
                }
            },
            dismissButton = {
                TextButton(onClick = { konfirmasiKeluar = false }) {
                    Text("Lanjut mengisi", color = WarnaHilang.TeksSekunder)
                }
            }
        )
    }
}

// ---------------------------------------------------------------------
// Komponen form (gaya sama dengan form temuan)
// ---------------------------------------------------------------------

@Composable
private fun PanahKembali() {
    val warna = WarnaHilang.Rose
    Canvas(Modifier.size(24.dp)) {
        val x = size.width / 24f
        val y = size.height / 24f
        val ujung = Offset(4 * x, 12 * y)
        val tebal = 2.dp.toPx()
        drawLine(warna, Offset(20 * x, 12 * y), ujung, tebal, StrokeCap.Round)
        drawLine(warna, ujung, Offset(11 * x, 5 * y), tebal, StrokeCap.Round)
        drawLine(warna, ujung, Offset(11 * x, 19 * y), tebal, StrokeCap.Round)
    }
}

@Composable
private fun KelompokForm(
    label: String,
    wajib: Boolean = true,
    isi: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(
            buildAnnotatedString {
                append(label)
                if (wajib) {
                    withStyle(SpanStyle(color = WarnaHilang.Rose)) { append("  *") }
                } else {
                    withStyle(SpanStyle(color = WarnaHilang.TeksSekunder, fontWeight = FontWeight.Normal)) {
                        append("  (opsional)")
                    }
                }
            },
            fontWeight = FontWeight.SemiBold
        )
        isi()
    }
}

@Composable
private fun IsianForm(
    nilai: String,
    ubah: (String) -> Unit,
    placeholder: String,
    aktif: Boolean,
    error: String?,
    banyakBaris: Boolean = false
) {
    OutlinedTextField(
        value = nilai,
        onValueChange = ubah,
        modifier = Modifier.fillMaxWidth(),
        enabled = aktif,
        placeholder = { Text(placeholder, color = WarnaHilang.TeksSekunder) },
        singleLine = !banyakBaris,
        minLines = if (banyakBaris) 3 else 1,
        isError = error != null,
        shape = BentukHilang.Isian,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            errorContainerColor = Color.White,
            focusedBorderColor = WarnaHilang.Rose,
            unfocusedBorderColor = WarnaHilang.GarisForm,
            errorBorderColor = WarnaHilang.Merah,
            cursorColor = WarnaHilang.Rose,
            focusedTextColor = WarnaHilang.Teks,
            unfocusedTextColor = WarnaHilang.Teks
        )
    )
    PesanError(error)
}

/** Tombol bergaya kolom isian untuk kategori & tanggal (seperti dropdown F3). */
@Composable
private fun TombolPilihan(
    teks: String,
    terisi: Boolean,
    aktif: Boolean,
    error: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = aktif,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp),
        shape = BentukHilang.Isian,
        border = BorderStroke(1.dp, if (error) WarnaHilang.Merah else WarnaHilang.GarisForm),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = WarnaHilang.Teks
        )
    ) {
        Text(
            text = teks,
            modifier = Modifier.weight(1f),
            color = if (terisi) WarnaHilang.Teks else WarnaHilang.TeksSekunder
        )
        Text("▾", color = WarnaHilang.Rose)
    }
}

@Composable
private fun PesanError(pesan: String?) {
    if (pesan != null) {
        Text(pesan, color = WarnaHilang.Merah, fontSize = 12.sp)
    }
}

@Composable
private fun Bantuan(teks: String, penghitung: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(teks, modifier = Modifier.weight(1f), color = WarnaHilang.TeksSekunder, fontSize = 13.sp)
        Text(penghitung, color = WarnaHilang.TeksSekunder, fontSize = 13.sp)
    }
}

/** Area unggah foto bergaris putus-putus (sama dengan form temuan). */
@Composable
private fun AreaFoto(
    fotoBaru: String?,
    adaFotoLama: Boolean,
    urlFotoLama: String?,
    aktif: Boolean,
    onPilih: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
            .clip(BentukHilang.AreaUnggah)
            .background(WarnaHilang.BlushMuda)
            .drawBehind {
                val tebal = 2.dp.toPx()
                drawRoundRect(
                    color = WarnaHilang.GarisPutus,
                    topLeft = Offset(tebal / 2, tebal / 2),
                    size = Size(size.width - tebal, size.height - tebal),
                    cornerRadius = CornerRadius(20.dp.toPx()),
                    style = Stroke(
                        width = tebal,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 5.dp.toPx()))
                    )
                )
            }
            .clickable(enabled = aktif, onClick = onPilih),
        contentAlignment = Alignment.Center
    ) {
        when {
            fotoBaru != null -> {
                AsyncImage(
                    model = Uri.parse(fotoBaru),
                    contentDescription = "Foto barang pilihan",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                LabelFoto("Foto baru", Modifier.align(Alignment.BottomEnd))
            }

            adaFotoLama && urlFotoLama != null -> {
                AsyncImage(
                    model = urlFotoLama,
                    contentDescription = "Foto barang sebelumnya",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                LabelFoto("Foto saat ini", Modifier.align(Alignment.BottomEnd))
            }

            adaFotoLama -> Text("Foto saat ini tidak dapat dimuat", color = WarnaHilang.TeksSekunder, fontSize = 13.sp)

            else -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("+", color = WarnaHilang.Rose, fontSize = 28.sp)
                Text("Tambah foto barang", fontWeight = FontWeight.Bold)
                Text(
                    "Pilih dari galeri · JPG/PNG, maks. 5 MB",
                    color = WarnaHilang.TeksSekunder,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun LabelFoto(teks: String, modifier: Modifier) {
    Text(
        teks,
        modifier = modifier
            .padding(12.dp)
            .background(Color.White, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        color = WarnaHilang.Rose
    )
}

// ---------------------------------------------------------------------
// Date picker: tanggal setelah hari ini tidak bisa dipilih
// ---------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogTanggal(
    awal: LocalDate?,
    onPilih: (LocalDate) -> Unit,
    onTutup: () -> Unit
) {
    val hariIni = remember { LocalDate.now(ZONA_WAKTU_KAMPUS) }
    val batasMillis = remember(hariIni) {
        hariIni.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    val state = rememberDatePickerState(
        initialSelectedDateMillis = (awal ?: hariIni).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= batasMillis
            override fun isSelectableYear(year: Int): Boolean = year <= hariIni.year
        }
    )

    DatePickerDialog(
        onDismissRequest = onTutup,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onPilih(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                },
                enabled = state.selectedDateMillis != null
            ) {
                Text("Pilih", color = WarnaHilang.Rose, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onTutup) {
                Text("Batal", color = WarnaHilang.TeksSekunder)
            }
        }
    ) {
        DatePicker(
            state = state,
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = WarnaHilang.Rose,
                selectedDayContentColor = Color.White,
                todayDateBorderColor = WarnaHilang.Rose,
                todayContentColor = WarnaHilang.Rose,
                selectedYearContainerColor = WarnaHilang.Rose,
                selectedYearContentColor = Color.White
            )
        )
        Text(
            "Tanggal setelah hari ini tidak dapat dipilih.",
            modifier = Modifier.padding(horizontal = 24.dp),
            color = WarnaHilang.TeksSekunder,
            fontSize = 12.sp
        )
    }
}
