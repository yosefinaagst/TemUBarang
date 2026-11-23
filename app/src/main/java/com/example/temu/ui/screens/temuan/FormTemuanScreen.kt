
package com.example.temu.ui.screens.temuan

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import com.example.temu.data.model.FormTemuan
import com.example.temu.data.model.kategoriBarang
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import com.example.temu.data.model.Temuan

private val formBg = Color(0xFFFCF8F7)
private val rose = Color(0xFF89465F)
private val formText = Color(0xFF30282E)
private val hint = Color(0xFF72666D)
private val outline = Color(0xFFE7DCE1)
private val fieldShape = RoundedCornerShape(14.dp)

@Composable
fun FormTemuanScreen(
    initialData: Temuan? = null,
    initialPhotoUrl: String? = null,
    sibuk: Boolean,
    pesan: String,
    onBack: () -> Unit,
    onSubmit: (FormTemuan, Uri?, Boolean) -> Unit
) {
    val id = rememberSaveable(initialData?.id) {
        initialData?.id ?: UUID.randomUUID().toString()
    }

    var nama by rememberSaveable(initialData?.id) {
        mutableStateOf(initialData?.nama.orEmpty())
    }

    var kategori by rememberSaveable(initialData?.id) {
        mutableStateOf(
            initialData?.kategori ?: "personal_items"
        )
    }

    var deskripsi by rememberSaveable(initialData?.id) {
        mutableStateOf(initialData?.deskripsi.orEmpty())
    }

    var lokasi by rememberSaveable(initialData?.id) {
        mutableStateOf(initialData?.lokasi.orEmpty())
    }

    var tanggal by rememberSaveable(initialData?.id) {
        mutableStateOf(
            initialData?.tanggal
                ?: LocalDate.now(ZoneId.of("Asia/Jakarta")).toString()
        )
    }
    var foto by rememberSaveable { mutableStateOf<String?>(null) }
    var menu by remember { mutableStateOf(false) }

    val pilihFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            foto = uri.toString()
        }
    }

    BackHandler(enabled = true) {
        if (!sibuk) onBack()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = formBg,
        contentColor = formText
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
                    onClick = onBack,
                    enabled = !sibuk,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, outline, CircleShape)
                        .semantics {
                            contentDescription = "Kembali ke Home"
                        }
                ) {
                    Canvas(Modifier.size(24.dp)) {
                        val x = size.width / 24f
                        val y = size.height / 24f
                        val tip = Offset(4 * x, 12 * y)

                        drawLine(
                            rose,
                            Offset(20 * x, 12 * y),
                            tip,
                            2.dp.toPx(),
                            StrokeCap.Round
                        )

                        drawLine(
                            rose,
                            tip,
                            Offset(11 * x, 5 * y),
                            2.dp.toPx(),
                            StrokeCap.Round
                        )

                        drawLine(
                            rose,
                            tip,
                            Offset(11 * x, 19 * y),
                            2.dp.toPx(),
                            StrokeCap.Round
                        )
                    }
                }

                Text(
                    "Laporkan temuan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            HorizontalDivider(color = outline)

            // Isi form
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Kolom bertanda * wajib diisi.",
                    color = hint,
                    fontSize = 14.sp
                )

                // Foto barang
                FormGroup("Foto barang") {
                    val photoShape = RoundedCornerShape(20.dp)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(172.dp)
                            .clip(photoShape)
                            .background(Color(0xFFF8F2F4))
                            .drawBehind {
                                val stroke = 2.dp.toPx()

                                drawRoundRect(
                                    color = Color(0xFFD8BEC9),
                                    topLeft = Offset(
                                        stroke / 2,
                                        stroke / 2
                                    ),
                                    size = Size(
                                        size.width - stroke,
                                        size.height - stroke
                                    ),
                                    cornerRadius = CornerRadius(
                                        20.dp.toPx()
                                    ),
                                    style = Stroke(
                                        width = stroke,
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(
                                                7.dp.toPx(),
                                                5.dp.toPx()
                                            )
                                        )
                                    )
                                )
                            }
                            .clickable(enabled = !sibuk) {
                                pilihFoto.launch(
                                    arrayOf("image/jpeg", "image/png")
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val selected = foto

                        when {
                            selected != null -> {
                                AsyncImage(
                                    model = Uri.parse(selected),
                                    contentDescription = "Foto barang pilihan",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                Text(
                                    "Foto baru",
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(12.dp)
                                        .background(Color.White, CircleShape)
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = rose
                                )
                            }

                            initialPhotoUrl != null -> {
                                AsyncImage(
                                    model = initialPhotoUrl,
                                    contentDescription = "Foto barang sebelumnya",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                Text(
                                    "Ganti foto",
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(12.dp)
                                        .background(Color.White, CircleShape)
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = rose
                                )
                            }

                            else -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("+", color = rose, fontSize = 28.sp)
                                    Text("Tambah foto barang", fontWeight = FontWeight.Bold)
                                    Text(
                                        "Pilih dari galeri · JPG/PNG, maks. 5 MB",
                                        color = hint,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Nama barang
                FormGroup("Nama barang") {
                    FormInput(
                        value = nama,
                        update = { nama = it },
                        placeholder = "Contoh: Botol minum krem",
                        enabled = !sibuk
                    )
                }

                // Kategori
                FormGroup("Kategori") {
                    Box {
                        OutlinedButton(
                            onClick = { menu = true },
                            enabled = !sibuk,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp),
                            shape = fieldShape,
                            border = BorderStroke(1.dp, outline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = formText
                            )
                        ) {
                            Text(
                                text = kategoriBarang[kategori].orEmpty(),
                                modifier = Modifier.weight(1f)
                            )

                            Text("▾", color = rose)
                        }

                        DropdownMenu(
                            expanded = menu,
                            onDismissRequest = { menu = false }
                        ) {
                            kategoriBarang.forEach { (kode, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        kategori = kode
                                        menu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Deskripsi
                FormGroup("Deskripsi") {
                    FormInput(
                        value = deskripsi,
                        update = {
                            if (it.length <= 500) {
                                deskripsi = it
                            }
                        },
                        placeholder = "Ceritakan bentuk, warna, dan kondisi barang saat ditemukan.",
                        enabled = !sibuk,
                        multi = true
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Sisakan ciri khusus untuk membantu pemeriksaan klaim.",
                            modifier = Modifier.weight(1f),
                            color = hint,
                            fontSize = 13.sp
                        )

                        Text(
                            "${deskripsi.length}/500",
                            color = hint,
                            fontSize = 13.sp
                        )
                    }
                }

                // Lokasi
                FormGroup("Lokasi ditemukan") {
                    FormInput(
                        value = lokasi,
                        update = { lokasi = it },
                        placeholder = "Contoh: Gedung F, lantai 1",
                        enabled = !sibuk
                    )
                }

                // Tanggal
                FormGroup("Tanggal ditemukan") {
                    FormInput(
                        value = tanggal,
                        update = { tanggal = it },
                        placeholder = "YYYY-MM-DD",
                        enabled = !sibuk
                    )
                }

                if (pesan.isNotBlank()) {
                    Text(
                        text = pesan,
                        color = rose,
                        fontSize = 13.sp
                    )
                }
            }

            HorizontalDivider(color = outline)

            // Tombol simpan
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = {
                        onSubmit(
                            FormTemuan(
                                id = id,
                                nama = nama,
                                lokasi = lokasi,
                                tanggal = tanggal,
                                kategori = kategori,
                                deskripsi = deskripsi
                            ),
                            foto?.let(Uri::parse),
                            initialData != null
                        )
                    },
                    enabled = !sibuk,
                    shape = fieldShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = rose,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        if (sibuk) {
                            "Menyimpan..."
                        } else if (initialData != null) {
                            "Simpan perubahan"
                        } else {
                            "Terbitkan laporan"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FormGroup(
    label: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            buildAnnotatedString {
                append(label)
                withStyle(SpanStyle(color = rose)) {
                    append("  *")
                }
            },
            fontWeight = FontWeight.SemiBold
        )

        content()
    }
}

@Composable
private fun FormInput(
    value: String,
    update: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    multi: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = update,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        placeholder = {
            Text(placeholder, color = hint)
        },
        singleLine = !multi,
        minLines = if (multi) 3 else 1,
        shape = fieldShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = rose,
            unfocusedBorderColor = outline,
            cursorColor = rose,
            focusedTextColor = formText,
            unfocusedTextColor = formText
        )
    )
}
