package com.example.temubarang.ui.ditemukan

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.example.temu.R
import com.example.temubarang.data.model.Temuan
import com.example.temubarang.data.model.kategoriBarang
import kotlinx.coroutines.CancellationException

private val paper = Color(0xFFFCF8F7)
private val berry = Color(0xFF89465F)
private val blush = Color(0xFFF2E6EB)
private val ink = Color(0xFF30282E)
private val muted = Color(0xFF72666D)
private val line = Color(0xFFE9DDE2)
private val rounded = RoundedCornerShape(17.dp)

@Composable
private fun Glyph(id: Int, label: String? = null, color: Color = muted, size: Int = 18) {
    Icon(painterResource(id), label, Modifier.size(size.dp), tint = color)
}

@Composable
internal fun HomePink(vm: TemuanViewModel) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var detail by remember { mutableStateOf<Temuan?>(null) }
    val rows = vm.daftar.filter {
        (category.isEmpty() || it.kategori == category) && it.nama.contains(query.trim(), ignoreCase = true)
    }
    Surface(color = paper, contentColor = ink) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            Box(Modifier.fillMaxWidth().weight(1f)) {
                LazyVerticalGrid(GridCells.Fixed(2), Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 92.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Halo! User", color = muted)
                                Box(Modifier.size(40.dp).background(blush, CircleShape), contentAlignment = Alignment.Center) {
                                    Glyph(R.drawable.home_profil, "Pengguna", berry)
                                }
                            }
                            Text("Temukan kembali\nbarangmu.", fontSize = 32.sp, lineHeight = 35.sp,
                                fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.weight(1f).height(48.dp).background(Color.White, rounded)
                                    .border(1.dp, line, rounded).padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                    Glyph(R.drawable.home_cari)
                                    BasicTextField(query, { query = it }, Modifier.weight(1f), singleLine = true,
                                        textStyle = TextStyle(color = ink, fontSize = 15.sp),
                                        decorationBox = { field -> Box {
                                            if (query.isEmpty()) Text("Cari nama barang…", color = muted, fontSize = 15.sp)
                                            field()
                                        } })
                                }
                                OutlinedIconButton(onClick = vm::muat, enabled = !vm.sibuk, modifier = Modifier.size(48.dp),
                                    shape = rounded, border = BorderStroke(1.dp, line)) {
                                    Glyph(R.drawable.home_ulang, "Muat ulang laporan", berry)
                                }
                            }
                            Row(Modifier.fillMaxWidth().background(blush, rounded).padding(4.dp)) {
                                listOf("Hilang", "Ditemukan").forEachIndexed { idx, label ->
                                    Box(Modifier.weight(1f).background(if (idx == 1) Color.White else blush, rounded)
                                        .padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                        Text(label, color = if (idx == 1) berry else muted, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (mapOf("" to "Semua") + kategoriBarang).forEach { (key, label) ->
                                    Surface(onClick = { category = key }, shape = CircleShape,
                                        color = if (category == key) berry else Color.White,
                                        contentColor = if (category == key) Color.White else ink,
                                        border = BorderStroke(1.dp, if (category == key) berry else line)) {
                                        Text(label, Modifier.padding(horizontal = 13.dp, vertical = 8.dp), fontSize = 13.sp)
                                    }
                                }
                            }
                            if (vm.sibuk) LinearProgressIndicator(Modifier.fillMaxWidth(), color = berry)
                            if (vm.pesan.isNotEmpty()) Text(vm.pesan, color = muted)
                            if (rows.isEmpty() && !vm.sibuk && vm.pesan.isEmpty()) Text(
                                if (vm.daftar.isEmpty()) "Belum ada laporan temuan." else "Tidak ada barang yang cocok.", color = muted)
                        }
                    }
                    items(rows, key = { it.id }) { barang -> KartuTemuan(barang, vm) { detail = barang } }
                }
                ExtendedFloatingActionButton(onClick = { if (!vm.sibuk) vm.bukaForm() },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(14.dp),
                    containerColor = berry, contentColor = Color.White) { Text("+   Laporkan temuan") }
            }
            Row(Modifier.fillMaxWidth().background(Color.White).padding(vertical = 8.dp)) {
                listOf(R.drawable.home_jelajah to "Jelajah", R.drawable.home_klaim to "Klaim Saya",
                    R.drawable.home_tersimpan to "Tersimpan", R.drawable.home_status to "Status Saya",
                    R.drawable.home_profil to "Profil").forEachIndexed { idx, (icon, label) ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.background(if (idx == 0) blush else Color.Transparent, CircleShape)
                            .padding(horizontal = 14.dp, vertical = 4.dp)) { Glyph(icon, color = if (idx == 0) berry else muted) }
                        Text(label, fontSize = 10.sp, color = if (idx == 0) berry else muted)
                    }
                }
            }
        }
    }
    detail?.let { barang ->
        AlertDialog(onDismissRequest = { detail = null }, title = { Text(barang.nama) },
            text = { Text(listOf(barang.lokasi, barang.tanggal, barang.deskripsi).filter { it.isNotBlank() }.joinToString("\n\n")) },
            confirmButton = { TextButton(onClick = { detail = null }) { Text("Tutup", color = berry) } })
    }
}

@Composable
private fun KartuTemuan(barang: Temuan, vm: TemuanViewModel, onClick: () -> Unit) {
    var url by remember(barang.foto) { mutableStateOf<String?>(null) }
    var gagal by remember(barang.foto) { mutableStateOf(false) }
    var attempt by remember(barang.foto) { mutableIntStateOf(0) }
    LaunchedEffect(barang.foto, attempt) {
        if (barang.foto.isBlank()) return@LaunchedEffect
        gagal = false
        url = null
        try { url = vm.urlFoto(barang.foto) }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { gagal = true }
    }
    Surface(onClick = onClick, shape = rounded, color = Color.White, contentColor = ink,
        border = BorderStroke(1.dp, line), shadowElevation = 1.dp) {
        Column {
            Box(Modifier.fillMaxWidth().aspectRatio(1f).background(blush), contentAlignment = Alignment.Center) {
                when {
                    gagal -> TextButton(onClick = { attempt++ }) { Text("Coba lagi", color = berry) }
                    barang.foto.isBlank() -> Text("Tanpa foto", color = muted)
                    url != null -> AsyncImage(model = url, contentDescription = barang.nama,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, onError = { gagal = true })
                }
            }
            Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                val status = when (barang.status) {
                    "in_progress" -> "Dalam proses"
                    "completed" -> "Dikembalikan"
                    else -> "Dilaporkan"
                }
                Text("•  $status", Modifier.background(Color(0xFFFAF0E3), CircleShape).padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize = 11.sp, color = Color(0xFF814A17))
                Text(barang.nama, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Meta(R.drawable.home_kategori, kategoriBarang[barang.kategori] ?: "Lainnya")
                Meta(R.drawable.home_lokasi, barang.lokasi)
            }
        }
    }
}

@Composable
private fun Meta(icon: Int, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Glyph(icon, size = 13)
        Text(value, color = muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
