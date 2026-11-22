
package com.example.temu.ui.screens.temuan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.temu.data.model.Temuan
import com.example.temu.data.model.kategoriBarang

private val detailBg = Color(0xFFFCF8F7)
private val detailRose = Color(0xFF89465F)
private val detailText = Color(0xFF30282E)
private val detailMuted = Color(0xFF72666D)
private val detailLine = Color(0xFFE9DDE2)
private val detailRed = Color(0xFFB94D4D)

@Composable
fun DetailTemuanScreen(
    barang: Temuan,
    fotoUrl: String?,
    fotoLoading: Boolean,
    fotoError: Boolean,
    isOwner: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    deleting: Boolean = false
) {
    var konfirmasiHapus by remember {
        mutableStateOf(false)
    }

    val statusLabel = when (barang.status) {
        "in_progress" -> "Dalam proses"
        "completed" -> "Dikembalikan"
        else -> "Dilaporkan"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(detailBg)
            .safeDrawingPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White, CircleShape)
            ) {
                Text(
                    "‹",
                    fontSize = 32.sp,
                    color = detailRose
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                "Detail temuan",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = detailText
            )
        }

        HorizontalDivider(color = detailLine)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFFF2E6EB)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    barang.foto.isBlank() -> {
                        Text("Tidak ada foto barang", color = detailMuted)
                    }

                    fotoLoading -> {
                        CircularProgressIndicator(color = detailRose)
                    }

                    fotoError -> {
                        Text("Foto tidak dapat dimuat", color = detailMuted)
                    }

                    fotoUrl != null -> {
                        AsyncImage(
                            model = fotoUrl,
                            contentDescription = "Foto ${barang.nama}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Surface(
                shape = CircleShape,
                color = Color(0xFFFAF0E3)
            ) {
                Text(
                    "•  $statusLabel",
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 7.dp
                    ),
                    color = Color(0xFF814A17),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                barang.nama,
                fontSize = 27.sp,
                lineHeight = 33.sp,
                fontWeight = FontWeight.Bold,
                color = detailText
            )

            HorizontalDivider(color = detailLine)

            DetailInfo(
                "Kategori barang",
                kategoriBarang[barang.kategori] ?: "Lainnya"
            )

            DetailInfo("Lokasi ditemukan", barang.lokasi)
            DetailInfo("Tanggal ditemukan", barang.tanggal)

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Deskripsi barang",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = detailText
                )

                Text(
                    barang.deskripsi.ifBlank {
                        "Tidak ada deskripsi."
                    },
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = detailMuted
                )
            }

            // Hanya pemilik yang melihat tombol ini
            if (isOwner) {
                HorizontalDivider(color = detailLine)

                Text(
                    "Kelola laporan",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = detailText
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        enabled = !deleting,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Edit laporan",
                            color = detailRose
                        )
                    }

                    Button(
                        onClick = {
                            konfirmasiHapus = true
                        },
                        enabled = !deleting,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = detailRed
                        )
                    ) {
                        Text("Hapus laporan")
                    }
                }
            }
        }

        HorizontalDivider(color = detailLine)

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = detailRose,
                contentColor = Color.White
            )
        ) {
            Text("Kembali ke Jelajah")
        }
    }

    // Dialog konfirmasi penghapusan
    if (konfirmasiHapus) {
        AlertDialog(
            onDismissRequest = {
                if (!deleting) konfirmasiHapus = false
            },
            title = {
                Text("Hapus laporan?")
            },
            text = {
                Text(
                    "Laporan ${barang.nama} akan disembunyikan dari daftar temuan. Data riwayat tidak dihapus permanen."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !deleting,
                    onClick = onDelete
                ) {
                    if (deleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Ya, hapus", color = detailRed)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !deleting,
                    onClick = {
                        konfirmasiHapus = false
                    }
                ) {
                    Text("Batal", color = detailMuted)
                }
            }
        )
    }
}

@Composable
private fun DetailInfo(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = detailMuted
        )

        Text(
            value.ifBlank { "-" },
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = detailText
        )
    }
}
