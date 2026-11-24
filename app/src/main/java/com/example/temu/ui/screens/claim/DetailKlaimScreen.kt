package com.example.temu.ui.screens.claim

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.temu.data.model.Claim
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dkBg = Color(0xFFFCF8F7)
private val dkRose = Color(0xFF89465F)
private val dkText = Color(0xFF30282E)
private val dkMuted = Color(0xFF72666D)
private val dkLine = Color(0xFFE9DDE2)
private val dkField = Color(0xFFF3EFF0)
private val dkBlush = Color(0xFFF7F0F2)
private val dkRed = Color(0xFFB94D4D)

@Composable
fun DetailKlaimScreen(
    klaimId: String,
    onBack: () -> Unit,
    onDihapus: () -> Unit,
    viewModel: DetailKlaimViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(klaimId) { viewModel.muat(klaimId) }

    // Setelah dihapus -> kembali ke daftar klaim (PRD: Hapus -> konfirmasi -> daftar klaim)
    LaunchedEffect(state.dihapus) {
        if (state.dihapus) onDihapus()
    }

    DetailKlaimContent(
        state = state,
        ciriEdit = viewModel.ciriEdit,
        keteranganEdit = viewModel.keteranganEdit,
        onBack = onBack,
        onCobaLagi = viewModel::cobaLagi,
        onMulaiEdit = viewModel::mulaiEdit,
        onBatalEdit = viewModel::batalEdit,
        onCiriChange = viewModel::ubahCiri,
        onKeteranganChange = viewModel::ubahKeterangan,
        onSimpan = viewModel::simpan,
        onHapus = viewModel::hapus,
        onPesanTampil = viewModel::pesanSudahTampil
    )
}

@Composable
fun DetailKlaimContent(
    state: DetailKlaimUiState,
    ciriEdit: String,
    keteranganEdit: String,
    onBack: () -> Unit,
    onCobaLagi: () -> Unit,
    onMulaiEdit: () -> Unit,
    onBatalEdit: () -> Unit,
    onCiriChange: (String) -> Unit,
    onKeteranganChange: (String) -> Unit,
    onSimpan: () -> Unit,
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

    var konfirmasiHapus by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.menghapus) {
        if (!state.menghapus) konfirmasiHapus = false
    }

    Scaffold(
        containerColor = dkBg,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            Column(Modifier.statusBarsPadding()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = if (state.sedangEdit) onBatalEdit else onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = dkText
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (state.sedangEdit) "Edit klaim" else "Detail klaim",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = dkText
                    )
                }
                HorizontalDivider(color = dkLine)
            }
        },
        bottomBar = {
            val klaim = state.klaim
            if (klaim != null && state.bisaDiubah) {
                Column(Modifier.navigationBarsPadding()) {
                    HorizontalDivider(color = dkLine)
                    if (state.sedangEdit) {
                        TombolBawah(
                            kiri = "Batal",
                            kanan = "Simpan perubahan",
                            warnaKanan = dkRose,
                            sibuk = state.menyimpan,
                            onKiri = onBatalEdit,
                            onKanan = onSimpan,
                            kananAktif = ciriEdit.isNotBlank()
                        )
                    } else {
                        TombolBawah(
                            kiri = "Edit klaim",
                            kanan = "Hapus klaim",
                            warnaKanan = dkRed,
                            sibuk = state.menghapus,
                            onKiri = onMulaiEdit,
                            onKanan = { konfirmasiHapus = true },
                            kananAktif = true
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val klaim = state.klaim
            when {
                state.memuat && klaim == null -> CircularProgressIndicator(
                    color = dkRose,
                    modifier = Modifier.align(Alignment.Center)
                )

                klaim == null -> Column(
                    Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        state.error ?: "Klaim tidak ditemukan.",
                        color = dkMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onCobaLagi) { Text("Coba lagi", color = dkRose) }
                }

                else -> Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    KartuBarangDiklaim(klaim, state.fotoUrl)

                    if (state.sedangEdit) {
                        BagianEdit(
                            ciri = ciriEdit,
                            keterangan = keteranganEdit,
                            onCiriChange = onCiriChange,
                            onKeteranganChange = onKeteranganChange
                        )
                    } else {
                        BagianBaca(klaim, state.bisaDiubah)
                    }

                    // Info privasi
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(dkBlush)
                            .border(1.dp, dkLine, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            contentDescription = null,
                            tint = dkRose,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Hanya kamu dan pelapor terkait yang dapat membaca bukti ini.",
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = dkText
                        )
                    }
                }
            }
        }
    }

    // Dialog konfirmasi hapus
    if (konfirmasiHapus) {
        AlertDialog(
            onDismissRequest = { if (!state.menghapus) konfirmasiHapus = false },
            title = { Text("Hapus klaim?") },
            text = {
                Text(
                    "Pengajuan klaim untuk ${state.klaim?.reports?.namaBarang ?: "barang ini"} " +
                            "akan dihapus. Kamu bisa mengajukan klaim lagi selama laporan belum selesai."
                )
            },
            confirmButton = {
                TextButton(enabled = !state.menghapus, onClick = onHapus) {
                    if (state.menghapus) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Ya, hapus", color = dkRed)
                    }
                }
            },
            dismissButton = {
                TextButton(enabled = !state.menghapus, onClick = { konfirmasiHapus = false }) {
                    Text("Batal", color = dkMuted)
                }
            }
        )
    }
}

// ---------------- Bagian isi ----------------

@Composable
private fun KartuBarangDiklaim(klaim: Claim, fotoUrl: String?) {
    val laporan = klaim.reports
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, dkLine, RoundedCornerShape(20.dp))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(dkField)
            ) {
                if (fotoUrl != null) {
                    AsyncImage(
                        model = fotoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Surface(shape = CircleShape, color = Color(0xFFFAF0E3)) {
                    Text(
                        "•  ${labelStatusLaporan(laporan?.status)}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF814A17)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    laporan?.namaBarang ?: "Barang",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = dkText
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = dkMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${laporan?.location.orEmpty()} · ${tanggalPendek(laporan?.tanggal)}",
                        fontSize = 14.sp,
                        color = dkMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun BagianBaca(klaim: Claim, bisaDiubah: Boolean) {
    val (label, latar, teks) = statusKlaim(klaim.status)

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Status klaim", fontSize = 14.sp, color = dkMuted, modifier = Modifier.weight(1f))
            Surface(shape = CircleShape, color = latar) {
                Text(
                    "•  $label",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = teks
                )
            }
        }

        Info("Diajukan", tanggalPendek(klaim.createdAt))
        Info("Ciri khusus", klaim.ciriKhusus)
        Info("Keterangan pendukung", klaim.description?.ifBlank { null } ?: "Tidak ada keterangan.")

        if (!bisaDiubah) {
            Text(
                "Laporan sudah selesai, klaim tidak dapat diubah atau dihapus.",
                fontSize = 13.sp,
                color = dkMuted
            )
        }
    }
}

@Composable
private fun BagianEdit(
    ciri: String,
    keterangan: String,
    onCiriChange: (String) -> Unit,
    onKeteranganChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row {
            Text("Ciri khusus", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = dkText)
            Text("  *", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = dkRose)
        }
        OutlinedTextField(
            value = ciri,
            onValueChange = onCiriChange,
            isError = ciri.isBlank(),
            shape = RoundedCornerShape(16.dp),
            colors = warnaKolom(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
        )
        Text(
            "Sebutkan ciri yang tidak terlihat di foto atau deskripsi publik.",
            fontSize = 13.sp,
            color = dkMuted
        )

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("Keterangan pendukung", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = dkText)
            Spacer(Modifier.width(6.dp))
            Text("(opsional)", fontSize = 14.sp, color = dkMuted)
        }
        OutlinedTextField(
            value = keterangan,
            onValueChange = onKeteranganChange,
            shape = RoundedCornerShape(16.dp),
            colors = warnaKolom(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
        )
    }
}

@Composable
private fun Info(label: String, nilai: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 13.sp, color = dkMuted)
        Text(nilai, fontSize = 15.sp, lineHeight = 22.sp, color = dkText)
    }
}

@Composable
private fun TombolBawah(
    kiri: String,
    kanan: String,
    warnaKanan: Color,
    sibuk: Boolean,
    onKiri: () -> Unit,
    onKanan: () -> Unit,
    kananAktif: Boolean
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onKiri,
            enabled = !sibuk,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(kiri, color = dkRose)
        }
        Button(
            onClick = onKanan,
            enabled = !sibuk && kananAktif,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = warnaKanan, contentColor = Color.White)
        ) {
            if (sibuk) {
                CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(kanan)
            }
        }
    }
}

@Composable
private fun warnaKolom() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = dkMuted,
    unfocusedBorderColor = dkLine,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = dkRose
)

private fun labelStatusLaporan(status: String?) = when (status) {
    "in_progress" -> "Dalam proses"
    "completed" -> "Dikembalikan"
    else -> "Dilaporkan"
}

private fun statusKlaim(status: String?): Triple<String, Color, Color> = when (status?.lowercase()) {
    "approved", "accepted", "disetujui" ->
        Triple("Disetujui", Color(0xFFE4EFE9), Color(0xFF3F6B4F))
    "rejected", "ditolak" ->
        Triple("Ditolak", Color(0xFFF7E3E3), Color(0xFFB94D4D))
    "cancelled", "canceled", "dibatalkan" ->
        Triple("Dibatalkan", Color(0xFFEFEFEF), Color(0xFF72666D))
    else ->
        Triple("Menunggu pemeriksaan", Color(0xFFFAF0E3), Color(0xFF814A17))
}

private fun tanggalPendek(iso: String?): String {
    if (iso.isNullOrBlank()) return "-"
    return runCatching {
        LocalDate.parse(iso.take(10))
            .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID")))
    }.getOrDefault(iso.take(10))
}