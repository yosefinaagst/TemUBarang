package com.example.temu.ui.screens.tersimpan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// =====================================================================
// Warna & bentuk F4 — disamakan dengan palet layar Jelajah (F1 & F3)
// =====================================================================

internal object WarnaTersimpan {
    val Latar = Color(0xFFFCF8F7)
    val Putih = Color.White
    val Blush = Color(0xFFF2E6EB)
    val Rose = Color(0xFF89465F)
    val Teks = Color(0xFF30282E)
    val TeksSekunder = Color(0xFF72666D)
    val Garis = Color(0xFFE9DDE2)
    val Merah = Color(0xFFB94D4D)

    val HilangBg = Color(0xFFFAF0E3)
    val HilangTeks = Color(0xFF814A17)
    val DitemukanBg = Color(0xFFE3F1EA)
    val DitemukanTeks = Color(0xFF2D6A4B)
}

internal object BentukTersimpan {
    val Kartu = RoundedCornerShape(17.dp)
    val Tombol = RoundedCornerShape(12.dp)
    val Foto = RoundedCornerShape(22.dp)
    val FotoKecil = RoundedCornerShape(12.dp)
}

// =====================================================================
// Format teks
// =====================================================================

private val formatTanggal =
    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("id-ID"))

/** "2026-11-23" atau "2026-11-23T07:00:00+00:00" → "23 November 2026". */
internal fun tanggalTampil(teks: String): String {
    if (teks.isBlank()) return "-"
    return try {
        LocalDate.parse(teks).format(formatTanggal)
    } catch (_: Exception) {
        try {
            OffsetDateTime.parse(teks).toLocalDate().format(formatTanggal)
        } catch (_: Exception) {
            teks.take(10)
        }
    }
}

internal fun labelStatus(status: String): String = when (status) {
    "reported" -> "Dilaporkan"
    "in_progress" -> "Dalam proses"
    "completed" -> "Selesai"
    "" -> "-"
    else -> status.replace('_', ' ').replaceFirstChar { it.uppercase() }
}

// =====================================================================
// Komponen kecil
// =====================================================================

/** Label "Hilang" / "Ditemukan" di atas foto kartu. */
@Composable
internal fun LabelJenis(hilang: Boolean, modifier: Modifier = Modifier) {
    Text(
        if (hilang) "Hilang" else "Ditemukan",
        modifier = modifier
            .background(
                if (hilang) WarnaTersimpan.HilangBg else WarnaTersimpan.DitemukanBg,
                CircleShape
            )
            .padding(horizontal = 9.dp, vertical = 3.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (hilang) WarnaTersimpan.HilangTeks else WarnaTersimpan.DitemukanTeks
    )
}

/** Foto laporan dari signed URL, atau teks pengganti bila tidak ada / gagal dimuat. */
@Composable
internal fun FotoTersimpan(
    url: String?,
    adaFoto: Boolean,
    deskripsi: String,
    modifier: Modifier = Modifier
) {
    var gagal by remember(url) { mutableStateOf(false) }

    Box(modifier.background(WarnaTersimpan.Blush), contentAlignment = Alignment.Center) {
        when {
            !adaFoto -> TeksPengganti("Tanpa foto")
            url == null || gagal -> TeksPengganti("Foto tidak dapat dimuat")
            else -> AsyncImage(
                model = url,
                contentDescription = deskripsi,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = { gagal = true }
            )
        }
    }
}

@Composable
private fun TeksPengganti(teks: String) {
    Text(
        teks,
        modifier = Modifier.padding(8.dp),
        color = WarnaTersimpan.TeksSekunder,
        fontSize = 12.sp,
        textAlign = TextAlign.Center
    )
}

/** Pesan keadaan kosong / error dengan satu tombol teks. */
@Composable
internal fun PesanKeadaanTersimpan(
    judul: String,
    keterangan: String,
    teksTombol: String?,
    onTombol: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            judul,
            color = WarnaTersimpan.Teks,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            keterangan,
            color = WarnaTersimpan.TeksSekunder,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        if (teksTombol != null) {
            TextButton(onClick = onTombol) {
                Text(teksTombol, color = WarnaTersimpan.Rose, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** Header sederhana dengan tombol kembali (tanpa API eksperimental Material3). */
@Composable
internal fun HeaderTersimpan(judul: String, onKembali: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WarnaTersimpan.Putih)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onKembali) {
            Text("←", fontSize = 22.sp, color = WarnaTersimpan.Teks)
        }
        Text(
            judul,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = WarnaTersimpan.Teks
        )
    }
}
