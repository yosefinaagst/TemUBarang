package com.example.temu.ui.screens.laporansaya

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temu.data.model.AturanStatus
import com.example.temu.data.model.StatusLaporan
import com.example.temu.ui.screens.hilang.BentukHilang
import com.example.temu.ui.screens.hilang.LabelStatus
import com.example.temu.ui.screens.hilang.PesanKeadaan
import com.example.temu.ui.screens.hilang.WarnaHilang

/**
 * F1 Halaman 2 · Ubah status (layar 09)
 * Pilih status baru (sesuai aturan perpindahan) + catatan wajib.
 * Memilih Selesai meminta konfirmasi "Tandai selesai?" karena tidak bisa dibuka lagi.
 *
 * @param onValidasi mengembalikan true bila isian valid (dicek sebelum dialog konfirmasi).
 */
@Composable
fun UbahStatusScreen(
    state: UbahStatusUiState,
    onPilihStatus: (String) -> Unit,
    onCatatan: (String) -> Unit,
    onValidasi: () -> Boolean,
    onSimpan: () -> Unit,
    onMuatUlang: () -> Unit,
    onKembali: () -> Unit
) {
    var konfirmasiSelesai by rememberSaveable { mutableStateOf(false) }
    var konfirmasiKeluar by rememberSaveable { mutableStateOf(false) }

    val adaIsian = state.statusBaru != null || state.catatan.isNotBlank()
    val cobaKembali: () -> Unit = {
        when {
            state.mengirim -> Unit // tunggu sampai pengiriman selesai
            adaIsian -> konfirmasiKeluar = true
            else -> onKembali()
        }
    }
    BackHandler(onBack = cobaKembali)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarnaHilang.Latar)
            .safeDrawingPadding()
    ) {
        HeaderKembali(judul = "Ubah status", onKembali = cobaKembali)
        HorizontalDivider(color = WarnaHilang.Garis)

        when {
            state.memuat && state.statusSaatIni.isEmpty() -> Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = WarnaHilang.Rose)
            }

            state.statusSaatIni.isEmpty() -> Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                PesanKeadaan(
                    judul = "Status belum bisa dimuat",
                    keterangan = state.errorForm ?: "Terjadi kesalahan.",
                    teksTombol = "Coba lagi",
                    onTombol = onMuatUlang
                )
            }

            else -> IsiForm(
                state = state,
                onPilihStatus = onPilihStatus,
                onCatatan = onCatatan,
                onMuatUlang = onMuatUlang,
                modifier = Modifier.weight(1f)
            )
        }

        HorizontalDivider(color = WarnaHilang.Garis)

        val bisaSimpan = state.statusSaatIni.isNotEmpty() &&
            AturanStatus.tujuan(state.statusSaatIni).isNotEmpty() &&
            !state.mengirim && !state.memuat
        Button(
            onClick = {
                if (onValidasi()) {
                    if (state.statusBaru == StatusLaporan.SELESAI) konfirmasiSelesai = true else onSimpan()
                }
            },
            enabled = bisaSimpan,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = WarnaHilang.Rose,
                contentColor = Color.White
            )
        ) {
            if (state.mengirim) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text("Simpan status", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (konfirmasiSelesai) {
        AlertDialog(
            onDismissRequest = { konfirmasiSelesai = false },
            title = { Text("Tandai selesai?") },
            text = { Text("Status Selesai · Ditemukan tidak bisa diubah lagi. Pastikan barang sudah kembali.") },
            confirmButton = {
                TextButton(onClick = {
                    konfirmasiSelesai = false
                    onSimpan()
                }) {
                    Text("Ya, tandai selesai", color = WarnaHilang.Rose)
                }
            },
            dismissButton = {
                TextButton(onClick = { konfirmasiSelesai = false }) {
                    Text("Batal", color = WarnaHilang.TeksSekunder)
                }
            }
        )
    }

    if (konfirmasiKeluar) {
        AlertDialog(
            onDismissRequest = { konfirmasiKeluar = false },
            title = { Text("Buang perubahan?") },
            text = { Text("Status tidak berubah dan catatan akan hilang.") },
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

@Composable
private fun IsiForm(
    state: UbahStatusUiState,
    onPilihStatus: (String) -> Unit,
    onCatatan: (String) -> Unit,
    onMuatUlang: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tujuan = AturanStatus.tujuan(state.statusSaatIni)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        LabelKecil("LAPORAN")
        Text(state.namaBarang, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WarnaHilang.Teks)

        LabelKecil("STATUS SAAT INI")
        LabelStatus(state.statusSaatIni)

        if (tujuan.isEmpty()) {
            Text(
                "Laporan sudah selesai, jadi statusnya tidak bisa diubah lagi.",
                color = WarnaHilang.TeksSekunder,
                fontSize = 13.sp
            )
            return@Column
        }

        LabelKecil("STATUS BARU")
        tujuan.forEach { status ->
            PilihanStatus(
                status = status,
                dipilih = state.statusBaru == status,
                enabled = !state.mengirim,
                onClick = { onPilihStatus(status) }
            )
        }
        state.errorStatus?.let { PesanErrorKecil(it) }

        Text(
            "Catatan perubahan",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = WarnaHilang.Teks,
            modifier = Modifier.padding(top = 6.dp)
        )
        OutlinedTextField(
            value = state.catatan,
            onValueChange = onCatatan,
            enabled = !state.mengirim,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp),
            placeholder = { Text("Contoh: jaket sudah kembali, diambil di pos keamanan.") },
            isError = state.errorCatatan != null,
            shape = BentukHilang.Isian,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = WarnaHilang.Rose,
                unfocusedBorderColor = WarnaHilang.GarisForm,
                cursorColor = WarnaHilang.Rose
            )
        )
        Row(Modifier.fillMaxWidth()) {
            Text(
                state.errorCatatan ?: "Catatan wajib dan masuk ke riwayat.",
                modifier = Modifier.weight(1f),
                color = if (state.errorCatatan != null) WarnaHilang.Merah else WarnaHilang.TeksSekunder,
                fontSize = 12.sp
            )
            Text(
                "${state.catatan.length}/${AturanStatus.CATATAN_MAKS}",
                color = WarnaHilang.TeksSekunder,
                fontSize = 12.sp
            )
        }

        // Error dari server (mis. koneksi, konflik versi). Isian tetap ada.
        state.errorForm?.let { pesan ->
            Surface(
                color = Color(0xFFFBEAEA),
                shape = BentukHilang.Isian,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Status belum tersimpan. $pesan", color = WarnaHilang.Merah, fontSize = 13.sp)
                    if (state.konflikVersi) {
                        TextButton(onClick = onMuatUlang) {
                            Text("Muat ulang", color = WarnaHilang.Rose, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PilihanStatus(status: String, dipilih: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val (judul, keterangan) = when (status) {
        StatusLaporan.DILAPORKAN -> "Dilaporkan" to "Kembali ke tahap awal, misalnya verifikasi gagal."
        StatusLaporan.DALAM_PROSES -> "Dalam Proses" to "Sedang dicek atau menunggu serah terima dengan penemu."
        else -> "Selesai · Ditemukan" to "Barang sudah kembali. Status ini tidak bisa dibuka lagi."
    }
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = BentukHilang.Isian,
        color = if (dipilih) Color(0xFFFBF1F4) else Color.White,
        border = BorderStroke(1.dp, if (dipilih) WarnaHilang.Rose else WarnaHilang.GarisForm),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = dipilih,
                onClick = onClick,
                enabled = enabled,
                colors = RadioButtonDefaults.colors(
                    selectedColor = WarnaHilang.Rose,
                    unselectedColor = WarnaHilang.TeksSekunder
                )
            )
            Column(Modifier.weight(1f)) {
                Text(judul, fontWeight = FontWeight.SemiBold, color = WarnaHilang.Teks)
                Text(keterangan, fontSize = 12.sp, color = WarnaHilang.TeksSekunder)
            }
        }
    }
}

@Composable
private fun LabelKecil(teks: String) {
    Text(
        teks,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = WarnaHilang.TeksSekunder,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun PesanErrorKecil(teks: String) {
    Text(teks, color = WarnaHilang.Merah, fontSize = 12.sp)
}
