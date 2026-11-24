package com.example.temu.ui.screens.claim

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ksBg = Color(0xFFFCF8F7)
private val ksRose = Color(0xFF89465F)
private val ksText = Color(0xFF30282E)
private val ksMuted = Color(0xFF72666D)
private val ksLine = Color(0xFFE9DDE2)
private val ksField = Color(0xFFF3EFF0)

/**
 * Tab "Klaim Saya" (F2).
 * [bottomBar] diisi dengan bottom navigation yang sama seperti di Home,
 * supaya navbar tetap tampil di tab ini.
 */
@Composable
fun KlaimSayaScreen(
    onItemClick: (String) -> Unit,
    bottomBar: @Composable () -> Unit = {},
    viewModel: KlaimSayaViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    KlaimSayaContent(
        state = state,
        onItemClick = onItemClick,
        onCobaLagi = viewModel::muat,
        bottomBar = bottomBar
    )
}

@Composable
fun KlaimSayaContent(
    state: KlaimSayaUiState,
    onItemClick: (String) -> Unit,
    onCobaLagi: () -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    Scaffold(
        containerColor = ksBg,
        bottomBar = bottomBar
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)) {
                Text("Klaim Saya", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = ksText)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Pengajuan klaim barang yang sudah kamu kirim.",
                    fontSize = 14.sp,
                    color = ksMuted
                )
            }

            when {
                state.memuat && state.items.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = ksRose) }

                state.error != null && state.items.isEmpty() -> PesanTengah(
                    judul = "Ada kendala",
                    keterangan = state.error,
                    teksTombol = "Coba lagi",
                    onTombol = onCobaLagi
                )

                state.items.isEmpty() -> PesanTengah(
                    judul = "Belum ada klaim",
                    keterangan = "Buka barang di tab Ditemukan, lalu tekan \"Klaim Ini Barang Saya\".",
                    teksTombol = null,
                    onTombol = {}
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.items, key = { it.id }) { item ->
                        KlaimItem(item = item, onClick = { onItemClick(item.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun KlaimItem(item: ItemKlaimSaya, onClick: () -> Unit) {
    val (labelStatus, warnaLatar, warnaTeks) = statusKlaim(item.status)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ksLine, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ksField)
            ) {
                if (item.fotoUrl != null) {
                    AsyncImage(
                        model = item.fotoUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Surface(shape = CircleShape, color = warnaLatar) {
                    Text(
                        "•  $labelStatus",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = warnaTeks,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    item.namaBarang,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ksText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Diajukan ${tanggalPendek(item.tanggalAjukan)}" +
                            if (item.lokasi.isNotBlank()) " · ${item.lokasi}" else "",
                    fontSize = 13.sp,
                    color = ksMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    item.ciriKhusus,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = ksMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PesanTengah(
    judul: String,
    keterangan: String,
    teksTombol: String?,
    onTombol: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(judul, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ksText)
        Spacer(Modifier.height(8.dp))
        Text(keterangan, fontSize = 14.sp, color = ksMuted, textAlign = TextAlign.Center)
        if (teksTombol != null) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onTombol) { Text(teksTombol, color = ksRose) }
        }
    }
}

/** Label + warna untuk status klaim. Nilai status di DB belum pasti, jadi beberapa variasi ditangani. */
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

@Preview(showBackground = true)
@Composable
private fun KlaimSayaPreview() {
    KlaimSayaContent(
        state = KlaimSayaUiState(
            memuat = false,
            items = listOf(
                ItemKlaimSaya(
                    id = "1",
                    namaBarang = "Kotak Pensil",
                    lokasi = "Gedung F",
                    ciriKhusus = "Ada gantungan kunci kapibara dan stiker nama di bagian dalam.",
                    status = null,
                    tanggalAjukan = "2026-11-24",
                    fotoUrl = null
                )
            )
        ),
        onItemClick = {},
        onCobaLagi = {}
    )
}