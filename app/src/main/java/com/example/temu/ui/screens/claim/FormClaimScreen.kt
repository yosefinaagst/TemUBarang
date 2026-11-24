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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.temu.data.remote.RingkasanLaporan
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val klaimBg = Color(0xFFFCF8F7)
private val klaimRose = Color(0xFF89465F)
private val klaimText = Color(0xFF30282E)
private val klaimMuted = Color(0xFF72666D)
private val klaimLine = Color(0xFFE9DDE2)
private val klaimField = Color(0xFFF3EFF0)
private val klaimBlush = Color(0xFFF7F0F2)

@Composable
fun FormKlaimScreen(
    reportId: String,
    onBack: () -> Unit,
    onLihatDetailKlaim: (String) -> Unit,
    onKembaliKeJelajah: () -> Unit,
    viewModel: FormClaimViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(reportId) { viewModel.muat(reportId) }

    val klaimId = state.klaimId
    val ringkasan = state.ringkasan
    if (klaimId != null && ringkasan != null) {
        KlaimBerhasilContent(
            ringkasan = ringkasan,
            sudahPernahKlaim = state.sudahPernahKlaim,
            onLihatDetail = { onLihatDetailKlaim(klaimId) },
            onKembaliKeJelajah = onKembaliKeJelajah
        )
    } else {
        FormKlaimContent(
            state = state,
            ciriKhusus = viewModel.ciriKhusus,
            keterangan = viewModel.keterangan,
            onCiriChange = viewModel::ubahCiri,
            onKeteranganChange = viewModel::ubahKeterangan,
            onKirim = viewModel::kirim,
            onCobaLagi = viewModel::cobaLagi,
            onBack = onBack
        )
    }
}

// ---------------- Form "Ajukan klaim" ----------------
@Composable
fun FormKlaimContent(
    state: FormClaimUiState,
    ciriKhusus: String,
    keterangan: String,
    onCiriChange: (String) -> Unit,
    onKeteranganChange: (String) -> Unit,
    onKirim: () -> Unit,
    onCobaLagi: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(klaimBg)
            .safeDrawingPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = klaimText)
            }
            Spacer(Modifier.width(8.dp))
            Text("Ajukan klaim", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = klaimText)
        }
        HorizontalDivider(color = klaimLine)

        val ringkasan = state.ringkasan
        when {
            state.memuat -> Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = klaimRose) }

            ringkasan == null -> Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    state.error ?: "Laporan tidak dapat dimuat.",
                    color = klaimMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onCobaLagi) { Text("Coba lagi", color = klaimRose) }
            }

            else -> Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                KartuBarang(ringkasan)

                // Nama barang (otomatis, terkunci)
                Spacer(Modifier.height(24.dp))
                LabelField("Nama barang")
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(klaimField)
                        .border(1.dp, klaimLine, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        ringkasan.nama,
                        color = klaimMuted,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = klaimMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Petunjuk("Terisi otomatis dari laporan yang kamu pilih.")

                // Ciri khusus (wajib)
                Spacer(Modifier.height(24.dp))
                LabelField("Ciri khusus", wajib = true)
                Spacer(Modifier.height(8.dp))
                KolomTeks(
                    value = ciriKhusus,
                    onValueChange = onCiriChange,
                    placeholder = "Contoh: Ada inisial AP di bagian bawah botol.",
                    tinggiMin = 120.dp,
                    isError = state.error != null && ciriKhusus.isBlank()
                )
                Spacer(Modifier.height(6.dp))
                Petunjuk("Sebutkan ciri yang tidak terlihat di foto atau deskripsi publik.")

                // Keterangan pendukung (opsional)
                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    LabelField("Keterangan pendukung")
                    Spacer(Modifier.width(6.dp))
                    Text("(opsional)", fontSize = 14.sp, color = klaimMuted)
                }
                Spacer(Modifier.height(8.dp))
                KolomTeks(
                    value = keterangan,
                    onValueChange = onKeteranganChange,
                    placeholder = "Contoh: Tertinggal di perpustakaan setelah belajar sore.",
                    tinggiMin = 100.dp
                )

                // Info privasi
                Spacer(Modifier.height(24.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(klaimBlush)
                        .border(1.dp, klaimLine, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = klaimRose,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Hanya kamu dan pelapor terkait yang dapat membaca bukti ini.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = klaimText
                    )
                }

                state.error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        }

        HorizontalDivider(color = klaimLine)
        Button(
            onClick = onKirim,
            enabled = ringkasan != null && ciriKhusus.isNotBlank() && !state.mengirim,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = klaimRose,
                contentColor = Color.White,
                disabledContainerColor = klaimRose.copy(alpha = 0.45f),
                disabledContentColor = Color.White.copy(alpha = 0.8f)
            )
        ) {
            if (state.mengirim) {
                CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Kirim pengajuan", fontSize = 16.sp)
            }
        }
    }
}

// ---------------- Layar "Pengajuan berhasil dikirim" ----------------
@Composable
fun KlaimBerhasilContent(
    ringkasan: RingkasanLaporan,
    sudahPernahKlaim: Boolean,
    onLihatDetail: () -> Unit,
    onKembaliKeJelajah: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(klaimBg)
            .safeDrawingPadding()
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .size(96.dp)
                    .background(Color(0xFFE4EFE9), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color(0xFF3F6B4F),
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                if (sudahPernahKlaim) "Kamu sudah mengajukan klaim." else "Pengajuan berhasil dikirim.",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = klaimText,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Pelapor dapat memeriksa ciri khusus yang kamu sampaikan. " +
                        "Pengajuan belum berarti kepemilikan telah disetujui.",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = klaimMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    FotoKecil(ringkasan.fotoUrl, 64.dp)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            ringkasan.nama,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = klaimText
                        )
                        Spacer(Modifier.height(6.dp))
                        Surface(shape = CircleShape, color = Color(0xFFFAF0E3)) {
                            Text(
                                "•  Status laporan: ${labelStatus(ringkasan.status)}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color(0xFF814A17),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = klaimLine)
        Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Button(
                onClick = onLihatDetail,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = klaimRose,
                    contentColor = Color.White
                )
            ) { Text("Lihat detail klaim", fontSize = 16.sp) }
            TextButton(onClick = onKembaliKeJelajah, modifier = Modifier.fillMaxWidth()) {
                Text("Kembali ke Jelajah", color = klaimText, fontSize = 16.sp)
            }
        }
    }
}

// ---------------- Komponen kecil ----------------
@Composable
private fun KartuBarang(ringkasan: RingkasanLaporan) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, klaimLine, RoundedCornerShape(20.dp))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            FotoKecil(ringkasan.fotoUrl, 84.dp)
            Spacer(Modifier.width(16.dp))
            Column {
                Surface(shape = CircleShape, color = Color(0xFFEFE9E1)) {
                    Text(
                        "Ditemukan",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5B4A35)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    ringkasan.nama,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = klaimText
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = klaimMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${ringkasan.lokasi} · ${tanggalPendek(ringkasan.tanggal)}",
                        fontSize = 14.sp,
                        color = klaimMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun FotoKecil(url: String?, ukuran: Dp) {
    Box(
        Modifier
            .size(ukuran)
            .clip(RoundedCornerShape(14.dp))
            .background(klaimField)
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun LabelField(teks: String, wajib: Boolean = false) {
    Row {
        Text(teks, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = klaimText)
        if (wajib) Text("  *", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = klaimRose)
    }
}

@Composable
private fun Petunjuk(teks: String) {
    Text(teks, fontSize = 13.sp, color = klaimMuted)
}

@Composable
private fun KolomTeks(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    tinggiMin: Dp,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = klaimMuted.copy(alpha = 0.7f)) },
        isError = isError,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = klaimMuted,
            unfocusedBorderColor = klaimLine,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = tinggiMin)
    )
}

private fun labelStatus(status: String) = when (status) {
    "in_progress" -> "Dalam proses"
    "completed" -> "Dikembalikan"
    else -> "Dilaporkan"
}

private fun tanggalPendek(iso: String): String = runCatching {
    LocalDate.parse(iso.take(10))
        .format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID")))
}.getOrDefault(iso)