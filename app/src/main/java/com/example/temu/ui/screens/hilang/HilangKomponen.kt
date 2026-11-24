package com.example.temu.ui.screens.hilang

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.temu.R
import com.example.temu.data.model.StatusLaporan
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

// =====================================================================
// Warna & bentuk — disamakan dengan layar Barang Ditemukan (F3, Divana)
// =====================================================================

internal object WarnaHilang {
    val Latar = Color(0xFFFCF8F7)       // paper
    val Putih = Color.White
    val Blush = Color(0xFFF2E6EB)       // blush
    val BlushMuda = Color(0xFFF8F2F4)   // area unggah foto
    val GarisPutus = Color(0xFFD8BEC9)  // garis putus-putus area unggah
    val Rose = Color(0xFF89465F)        // berry
    val Teks = Color(0xFF30282E)        // ink
    val TeksSekunder = Color(0xFF72666D) // muted
    val Garis = Color(0xFFE9DDE2)       // line
    val GarisForm = Color(0xFFE7DCE1)   // outline form
    val Merah = Color(0xFFB94D4D)

    val DilaporkanBg = Color(0xFFFAF0E3)
    val DilaporkanTeks = Color(0xFF814A17)
    val ProsesBg = Color(0xFFE6EEF8)
    val ProsesTeks = Color(0xFF2E5283)
    val SelesaiBg = Color(0xFFE3F1EA)
    val SelesaiTeks = Color(0xFF2D6A4B)
}

internal object BentukHilang {
    val Kartu = RoundedCornerShape(17.dp)
    val Isian = RoundedCornerShape(14.dp)
    val Tombol = RoundedCornerShape(12.dp)
    val FotoDetail = RoundedCornerShape(22.dp)
    val AreaUnggah = RoundedCornerShape(20.dp)
}

// =====================================================================
// Format tanggal & label status
// =====================================================================

private val lokalIndonesia: Locale = Locale.forLanguageTag("id-ID")
private val formatPanjang = DateTimeFormatter.ofPattern("d MMMM yyyy", lokalIndonesia)

internal fun tanggalPanjang(tanggal: LocalDate): String = tanggal.format(formatPanjang)

internal fun tanggalPanjang(iso: String): String = try {
    tanggalPanjang(LocalDate.parse(iso))
} catch (_: DateTimeParseException) {
    iso
}

internal data class GayaStatus(val label: String, val latar: Color, val teks: Color)

internal fun gayaStatus(status: String): GayaStatus = when (status) {
    StatusLaporan.DALAM_PROSES -> GayaStatus("Dalam proses", WarnaHilang.ProsesBg, WarnaHilang.ProsesTeks)
    StatusLaporan.SELESAI -> GayaStatus("Selesai · Ditemukan", WarnaHilang.SelesaiBg, WarnaHilang.SelesaiTeks)
    else -> GayaStatus("Dilaporkan", WarnaHilang.DilaporkanBg, WarnaHilang.DilaporkanTeks)
}

// =====================================================================
// Komponen kecil
// =====================================================================

@Composable
internal fun Glyph(
    @DrawableRes id: Int,
    label: String? = null,
    color: Color = WarnaHilang.TeksSekunder,
    size: Int = 18
) {
    Icon(
        painter = painterResource(id),
        contentDescription = label,
        modifier = Modifier.size(size.dp),
        tint = color
    )
}

/** Label status "•  Dilaporkan" seperti di kartu temuan. */
@Composable
internal fun LabelStatus(status: String, besar: Boolean = false) {
    val gaya = gayaStatus(status)
    Text(
        "•  ${gaya.label}",
        modifier = Modifier
            .background(gaya.latar, CircleShape)
            .padding(
                horizontal = if (besar) 12.dp else 8.dp,
                vertical = if (besar) 7.dp else 3.dp
            ),
        fontSize = if (besar) 12.sp else 11.sp,
        fontWeight = if (besar) FontWeight.SemiBold else FontWeight.Normal,
        color = gaya.teks
    )
}

/**
 * Menampilkan foto laporan dari [url] (signed URL yang sudah disiapkan ViewModel),
 * atau teks pengganti bila laporan tidak punya foto / foto gagal dimuat.
 */
@Composable
internal fun FotoLaporan(
    adaFoto: Boolean,
    url: String?,
    deskripsi: String,
    teksTanpaFoto: String,
    modifier: Modifier = Modifier
) {
    var gagal by remember(url) { mutableStateOf(false) }

    Box(modifier.background(WarnaHilang.Blush), contentAlignment = Alignment.Center) {
        when {
            !adaFoto -> TeksFoto(teksTanpaFoto)
            url == null || gagal -> TeksFoto("Foto tidak dapat dimuat")
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
private fun TeksFoto(teks: String) {
    Text(
        teks,
        modifier = Modifier.padding(12.dp),
        color = WarnaHilang.TeksSekunder,
        fontSize = 13.sp,
        textAlign = TextAlign.Center
    )
}

/** Pesan keadaan kosong / error sederhana dengan satu tombol teks. */
@Composable
internal fun PesanKeadaan(
    judul: String,
    keterangan: String,
    teksTombol: String?,
    onTombol: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            judul,
            color = WarnaHilang.Teks,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            keterangan,
            color = WarnaHilang.TeksSekunder,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
        if (teksTombol != null) {
            TextButton(onClick = onTombol) {
                Text(teksTombol, color = WarnaHilang.Rose, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/**
 * Navigasi bawah, sama persis dengan layar Barang Ditemukan.
 * [aktif] = indeks menu yang sedang dibuka (0 Jelajah, 3 Laporan Saya / F1 Halaman 2).
 */
@Composable
internal fun NavigasiBawahJelajah(onPilih: (Int) -> Unit = {}, aktif: Int = 0) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 8.dp)
    ) {
        listOf(
            R.drawable.home_jelajah to "Jelajah",
            R.drawable.home_klaim to "Klaim Saya",
            R.drawable.home_tersimpan to "Tersimpan",
            R.drawable.home_status to "Laporan Saya",
            R.drawable.home_profil to "Profil"
        ).forEachIndexed { index, (ikon, label) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onPilih(index) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (index == aktif) WarnaHilang.Blush else Color.Transparent,
                            CircleShape
                        )
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Glyph(ikon, color = if (index == aktif) WarnaHilang.Rose else WarnaHilang.TeksSekunder)
                }
                Text(
                    label,
                    fontSize = 10.sp,
                    color = if (index == aktif) WarnaHilang.Rose else WarnaHilang.TeksSekunder
                )
            }
        }
    }
}
