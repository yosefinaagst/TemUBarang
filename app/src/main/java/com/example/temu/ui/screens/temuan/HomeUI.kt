
package com.example.temu.ui.screens.temuan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.temu.R
import com.example.temu.data.model.Temuan
import com.example.temu.data.model.kategoriBarang
import kotlinx.coroutines.CancellationException

private val paper = Color(0xFFFCF8F7)
private val berry = Color(0xFF89465F)
private val blush = Color(0xFFF2E6EB)
private val ink = Color(0xFF30282E)
private val muted = Color(0xFF72666D)
private val line = Color(0xFFE9DDE2)
private val rounded = RoundedCornerShape(17.dp)

@Composable
private fun Glyph(
    id: Int,
    label: String? = null,
    color: Color = muted,
    size: Int = 18
) {
    Icon(
        painter = painterResource(id),
        contentDescription = label,
        modifier = Modifier.size(size.dp),
        tint = color
    )
}

@Composable
fun HomeScreen(
    daftar: List<Temuan>,
    sibuk: Boolean,
    pesan: String,
    onRefresh: () -> Unit,
    onAddClick: () -> Unit,
    onItemClick: (Temuan) -> Unit,
    getPhotoUrl: suspend (String) -> String
) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
//    var detail by remember { mutableStateOf<Temuan?>(null) }

    val rows = daftar.filter { barang ->
        (category.isEmpty() || barang.kategori == category) &&
                barang.nama.contains(query.trim(), ignoreCase = true)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = paper,
        contentColor = ink
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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 20.dp,
                        bottom = 92.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Halo!", color = muted)

                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(blush, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Glyph(
                                        R.drawable.home_profil,
                                        label = "Profil",
                                        color = berry
                                    )
                                }
                            }

                            Text(
                                text = "Temukan kembali\nbarangmu.",
                                fontSize = 32.sp,
                                lineHeight = 35.sp,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .background(Color.White, rounded)
                                        .border(1.dp, line, rounded)
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                                ) {
                                    BasicTextField(
                                        value = query,
                                        onValueChange = { query = it },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = ink,
                                            fontSize = 15.sp
                                        ),
                                        decorationBox = { field ->
                                            Box {
                                                if (query.isEmpty()) {
                                                    Text(
                                                        "Cari nama barang…",
                                                        color = muted,
                                                        fontSize = 15.sp
                                                    )
                                                }
                                                field()
                                            }
                                        }
                                    )
                                }

                                OutlinedIconButton(
                                    onClick = onRefresh,
                                    enabled = !sibuk,
                                    modifier = Modifier.size(48.dp),
                                    shape = rounded,
                                    border = BorderStroke(1.dp, line)
                                ) {
                                    Glyph(
                                        R.drawable.home_ulang,
                                        label = "Muat ulang laporan",
                                        color = berry
                                    )
                                }
                            }

                            // Jenis laporan: saat ini hanya Ditemukan yang aktif
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(blush, rounded)
                                    .padding(4.dp)
                            ) {
                                listOf("Hilang", "Ditemukan").forEachIndexed { index, label ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (index == 1) Color.White else blush,
                                                rounded
                                            )
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            label,
                                            color = if (index == 1) berry else muted,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            // Filter kategori
                            Row(
                                modifier = Modifier.horizontalScroll(
                                    rememberScrollState()
                                ),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                (mapOf("" to "Semua") + kategoriBarang)
                                    .forEach { (key, label) ->
                                        Surface(
                                            onClick = { category = key },
                                            shape = CircleShape,
                                            color = if (category == key) berry else Color.White,
                                            contentColor = if (category == key) Color.White else ink,
                                            border = BorderStroke(
                                                1.dp,
                                                if (category == key) berry else line
                                            )
                                        ) {
                                            Text(
                                                label,
                                                modifier = Modifier.padding(
                                                    horizontal = 13.dp,
                                                    vertical = 8.dp
                                                ),
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                            }

                            if (sibuk) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = berry
                                )
                            }

                            if (pesan.isNotBlank()) {
                                Text(pesan, color = muted, fontSize = 12.sp)
                            }

                            if (rows.isEmpty() && !sibuk) {
                                Text(
                                    text = if (daftar.isEmpty()) {
                                        "Belum ada laporan temuan."
                                    } else {
                                        "Tidak ada barang yang cocok."
                                    },
                                    color = muted
                                )
                            }
                        }
                    }

                    items(
                        items = rows,
                        key = { it.id }
                    ) { barang ->
                        KartuTemuan(
                            barang = barang,
                            getPhotoUrl = getPhotoUrl,
                            onClick = {
                                onItemClick(barang)
                            }
                        )
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(14.dp),
                    containerColor = berry,
                    contentColor = Color.White
                ) {
                    Text("+   Laporkan temuan")
                }
            }

            // Navigasi bawah sementara; fitur lain belum diaktifkan
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
                    R.drawable.home_status to "Status Saya",
                    R.drawable.home_profil to "Profil"
                ).forEachIndexed { index, (icon, label) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (index == 0) blush else Color.Transparent,
                                    CircleShape
                                )
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Glyph(
                                icon,
                                color = if (index == 0) berry else muted
                            )
                        }

                        Text(
                            label,
                            fontSize = 10.sp,
                            color = if (index == 0) berry else muted
                        )
                    }
                }
            }
        }
    }


}

@Composable
private fun KartuTemuan(
    barang: Temuan,
    getPhotoUrl: suspend (String) -> String,
    onClick: () -> Unit
) {
    var url by remember(barang.foto) { mutableStateOf<String?>(null) }
    var gagal by remember(barang.foto) { mutableStateOf(false) }
    var attempt by remember(barang.foto) { mutableIntStateOf(0) }

    LaunchedEffect(barang.foto, attempt) {
        if (barang.foto.isBlank()) return@LaunchedEffect

        gagal = false
        url = null

        try {
            url = getPhotoUrl(barang.foto)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            gagal = true
        }
    }

    Surface(
        onClick = onClick,
        shape = rounded,
        color = Color.White,
        contentColor = ink,
        border = BorderStroke(1.dp, line),
        shadowElevation = 1.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(blush),
                contentAlignment = Alignment.Center
            ) {
                when {
                    gagal -> {
                        TextButton(onClick = { attempt++ }) {
                            Text("Coba lagi", color = berry)
                        }
                    }

                    barang.foto.isBlank() -> {
                        Text("Tanpa foto", color = muted)
                    }

                    url != null -> {
                        AsyncImage(
                            model = url,
                            contentDescription = barang.nama,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onError = { gagal = true }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(11.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val status = when (barang.status) {
                    "in_progress" -> "Dalam proses"
                    "completed" -> "Dikembalikan"
                    else -> "Dilaporkan"
                }

                Text(
                    "•  $status",
                    modifier = Modifier
                        .background(Color(0xFFFAF0E3), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 11.sp,
                    color = Color(0xFF814A17)
                )

                Text(
                    barang.nama,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Meta(
                    R.drawable.home_kategori,
                    kategoriBarang[barang.kategori] ?: "Lainnya"
                )

                Meta(R.drawable.home_lokasi, barang.lokasi)
            }
        }
    }
}

@Composable
private fun Meta(icon: Int, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Glyph(icon, size = 13)

        Text(
            value,
            color = muted,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
