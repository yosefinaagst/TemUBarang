package com.example.temu.ui.screens.profil

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.temu.data.model.Profil
import com.example.temu.viewmodel.profil.ProfilUiState
import com.example.temu.viewmodel.profil.ProfilViewModel

private val pfBg = Color(0xFFFCF8F7)
private val pfRose = Color(0xFF89465F)
private val pfText = Color(0xFF30282E)
private val pfMuted = Color(0xFF72666D)
private val pfLine = Color(0xFFE9DDE2)
private val pfField = Color(0xFFF3EFF0)
private val pfBlush = Color(0xFFF7E3E8)
private val pfRed = Color(0xFFB3261E)
private val pfErrorBg = Color(0xFFFBE9EC)
private val pfErrorText = Color(0xFF8C1D3A)

/**
 * Tab "Profil" (PR-01 s.d. PR-06).
 * [bottomBar] diisi NavigasiBawah dengan TAB_PROFIL terpilih.
 * [onKeluar] dipanggil setelah sign out berhasil -> pindah ke Login.
 */
@Composable
fun ProfilScreen(
    onKeluar: () -> Unit,
    bottomBar: @Composable () -> Unit = {},
    viewModel: ProfilViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.keluarBerhasil) {
        if (state.keluarBerhasil) onKeluar()
    }

    // Tombol Back HP saat mode edit -> batal edit, bukan keluar dari layar
    BackHandler(enabled = state.sedangEdit) { viewModel.batalEdit() }

    // Photo Picker Android: tidak perlu izin akses penyimpanan
    val pemilihFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) viewModel.pilihFoto(uri) }

    ProfilContent(
        state = state,
        namaEdit = viewModel.namaEdit,
        kontakEdit = viewModel.kontakEdit,
        fotoBaru = viewModel.fotoBaru,
        hapusFoto = viewModel.hapusFoto,
        onPilihFoto = {
            pemilihFoto.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onHapusFoto = viewModel::hapusFotoProfil,
        onCobaLagi = viewModel::muat,
        onMulaiEdit = viewModel::mulaiEdit,
        onBatalEdit = viewModel::batalEdit,
        onNamaChange = viewModel::ubahNama,
        onKontakChange = viewModel::ubahKontak,
        onSimpan = viewModel::simpan,
        onKeluar = viewModel::keluar,
        onPesanTampil = viewModel::pesanSudahTampil,
        bottomBar = bottomBar
    )
}

@Composable
fun ProfilContent(
    state: ProfilUiState,
    namaEdit: String,
    kontakEdit: String,
    fotoBaru: Uri? = null,
    hapusFoto: Boolean = false,
    onPilihFoto: () -> Unit = {},
    onHapusFoto: () -> Unit = {},
    onCobaLagi: () -> Unit,
    onMulaiEdit: () -> Unit,
    onBatalEdit: () -> Unit,
    onNamaChange: (String) -> Unit,
    onKontakChange: (String) -> Unit,
    onSimpan: () -> Unit,
    onKeluar: () -> Unit,
    onPesanTampil: () -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.pesan) {
        val pesan = state.pesan
        if (pesan != null) {
            snackbar.showSnackbar(pesan)
            onPesanTampil()
        }
    }

    var konfirmasiKeluar by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = pfBg,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            if (state.sedangEdit) {
                Column(Modifier.statusBarsPadding()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBatalEdit, enabled = !state.menyimpan) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Batal edit",
                                tint = pfText
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Edit profil", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = pfText)
                    }
                    HorizontalDivider(color = pfLine)
                }
            }
        },
        bottomBar = {
            if (state.sedangEdit) {
                Column(
                    Modifier.windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                ) {
                    HorizontalDivider(color = pfLine)
                    TombolSimpan(
                        menyimpan = state.menyimpan,
                        aktif = namaEdit.isNotBlank(),
                        onSimpan = onSimpan
                    )
                }
            } else {
                bottomBar()
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val profil = state.profil
            when {
                state.memuat && profil == null -> CircularProgressIndicator(
                    color = pfRose,
                    modifier = Modifier.align(Alignment.Center)
                )

                profil == null -> Column(
                    Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        state.error ?: "Profil tidak ditemukan.",
                        color = pfMuted,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onCobaLagi) { Text("Coba lagi", color = pfRose) }
                }

                state.sedangEdit -> BagianEdit(
                    profil = profil,
                    nama = namaEdit,
                    kontak = kontakEdit,
                    // Foto yang tampil: pilihan baru > foto lama (kalau tidak dihapus) > inisial
                    foto = fotoBaru ?: profil.fotoUrl.takeUnless { hapusFoto },
                    gagalSimpan = state.gagalSimpan,
                    bisaDiketik = !state.menyimpan,
                    onNamaChange = onNamaChange,
                    onKontakChange = onKontakChange,
                    onPilihFoto = onPilihFoto,
                    onHapusFoto = onHapusFoto
                )

                else -> BagianProfil(
                    profil = profil,
                    onEdit = onMulaiEdit,
                    onKeluar = { konfirmasiKeluar = true }
                )
            }
        }
    }

    // PR-06: dialog konfirmasi keluar
    if (konfirmasiKeluar) {
        DialogKeluar(
            sibuk = state.sedangKeluar,
            onKeluar = onKeluar,
            onBatal = { if (!state.sedangKeluar) konfirmasiKeluar = false }
        )
    }
}

// ---------------- PR-01 / PR-05: tampilan profil ----------------

@Composable
private fun BagianProfil(
    profil: Profil,
    onEdit: () -> Unit,
    onKeluar: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            "Profil",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = pfText
        )

        Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Avatar(profil.inisial, foto = profil.fotoUrl)
            Spacer(Modifier.height(14.dp))
            Text(
                profil.namaLengkap.ifBlank { "Tanpa nama" },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = pfText,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(profil.email, fontSize = 14.sp, color = pfMuted)
        }

        Spacer(Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, pfLine, RoundedCornerShape(20.dp))
        ) {
            Column {
                BarisInfo("Nama", profil.namaLengkap.ifBlank { "-" })
                HorizontalDivider(color = pfLine)
                BarisInfo("Email", profil.email.ifBlank { "-" })
                HorizontalDivider(color = pfLine)
                BarisInfo(
                    "Kontak (opsional)",
                    profil.kontak ?: "Belum diisi",
                    redup = profil.kontak == null
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        TombolPutih("Edit profil", onClick = onEdit)
        Spacer(Modifier.height(12.dp))
        TombolPutih("Keluar", onClick = onKeluar)
    }
}

@Composable
private fun BarisInfo(label: String, nilai: String, redup: Boolean = false) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(label, fontSize = 12.sp, color = pfMuted)
        Spacer(Modifier.height(4.dp))
        Text(
            nilai,
            fontSize = 15.sp,
            fontWeight = if (redup) FontWeight.Normal else FontWeight.SemiBold,
            color = if (redup) pfMuted else pfText
        )
    }
}

@Composable
private fun TombolPutih(teks: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, pfLine),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(teks, fontSize = 16.sp, color = pfText)
        }
    }
}

// ---------------- PR-02 / PR-03 / PR-04: edit profil ----------------

@Composable
private fun BagianEdit(
    profil: Profil,
    nama: String,
    kontak: String,
    foto: Any?,
    gagalSimpan: Boolean,
    bisaDiketik: Boolean,
    onNamaChange: (String) -> Unit,
    onKontakChange: (String) -> Unit,
    onPilihFoto: () -> Unit,
    onHapusFoto: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            // Avatar bisa ditekan untuk memilih foto; inisial ikut berubah saat nama diketik
            Box(
                Modifier.clickable(enabled = bisaDiketik, onClick = onPilihFoto)
            ) {
                Avatar(profil.copy(namaLengkap = nama).inisial, foto = foto)
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(pfRose)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Ganti foto",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onPilihFoto, enabled = bisaDiketik) {
                    Text(if (foto != null) "Ganti foto" else "Tambah foto", color = pfRose)
                }
                if (foto != null) {
                    Text("·", color = pfMuted)
                    TextButton(onClick = onHapusFoto, enabled = bisaDiketik) {
                        Text("Hapus foto", color = pfMuted)
                    }
                }
            }
            Text("Foto profil tidak wajib.", fontSize = 12.sp, color = pfMuted)
        }

        if (gagalSimpan) {
            Spacer(Modifier.height(20.dp))
            Text(
                "Profil belum tersimpan. Perubahanmu masih ada — coba simpan lagi.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = pfErrorText,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(pfErrorBg)
                    .padding(16.dp)
            )
        }

        // Nama lengkap (wajib)
        Spacer(Modifier.height(24.dp))
        LabelField("Nama lengkap", wajib = true)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = nama,
            onValueChange = onNamaChange,
            readOnly = !bisaDiketik,
            singleLine = true,
            isError = nama.isBlank(),
            shape = RoundedCornerShape(16.dp),
            colors = warnaKolom(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (nama.isBlank()) {
            Spacer(Modifier.height(6.dp))
            Text("Nama lengkap wajib diisi.", fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
        }

        // Email (read-only)
        Spacer(Modifier.height(20.dp))
        LabelField("Email")
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = profil.email,
            onValueChange = {},
            enabled = false,
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = warnaKolom(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Petunjuk("Email tidak dapat diubah.")

        // Kontak (opsional)
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            LabelField("Kontak")
            Spacer(Modifier.width(6.dp))
            Text("(opsional)", fontSize = 14.sp, color = pfMuted)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = kontak,
            onValueChange = onKontakChange,
            readOnly = !bisaDiketik,
            singleLine = true,
            placeholder = { Text("Contoh: WA 0812-3456-7801", color = pfMuted.copy(alpha = 0.7f)) },
            shape = RoundedCornerShape(16.dp),
            colors = warnaKolom(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Petunjuk("Bisa kamu cantumkan saat membuat laporan. Kontak tidak otomatis tampil publik.")
    }
}

@Composable
private fun TombolSimpan(menyimpan: Boolean, aktif: Boolean, onSimpan: () -> Unit) {
    Button(
        onClick = onSimpan,
        enabled = aktif && !menyimpan,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = pfRose,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFEDE3E7),
            disabledContentColor = pfMuted
        )
    ) {
        if (menyimpan) {
            CircularProgressIndicator(Modifier.size(18.dp), color = pfMuted, strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
            Text("Menyimpan…", fontSize = 16.sp)
        } else {
            Text("Simpan perubahan", fontSize = 16.sp)
        }
    }
}

// ---------------- PR-06: dialog keluar ----------------

@Composable
private fun DialogKeluar(sibuk: Boolean, onKeluar: () -> Unit, onBatal: () -> Unit) {
    Dialog(onDismissRequest = onBatal) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
            Column(Modifier.padding(24.dp)) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(pfBlush),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ExitToApp,
                        contentDescription = null,
                        tint = pfRose,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text("Keluar dari akun?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = pfText)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Kamu perlu masuk kembali untuk membuat laporan, menyimpan, atau mengajukan klaim.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = pfMuted
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onKeluar,
                    enabled = !sibuk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pfRed,
                        contentColor = Color.White,
                        disabledContainerColor = pfRed.copy(alpha = 0.5f),
                        disabledContentColor = Color.White
                    )
                ) {
                    if (sibuk) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Keluar", fontSize = 16.sp)
                    }
                }
                TextButton(
                    onClick = onBatal,
                    enabled = !sibuk,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Batal", color = pfText, fontSize = 16.sp)
                }
            }
        }
    }
}

// ---------------- Komponen kecil ----------------

/**
 * Avatar bulat: tampilkan [foto] (URL / Uri) kalau ada, kalau tidak tampilkan [inisial].
 */
@Composable
private fun Avatar(inisial: String, foto: Any? = null, ukuran: Dp = 84.dp) {
    Box(
        Modifier
            .size(ukuran)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFFF9E6EC), Color(0xFFEBC5D2)))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            inisial,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = pfRose
        )
        // Foto menutupi inisial; kalau gagal dimuat, inisial tetap terlihat
        if (foto != null) {
            AsyncImage(
                model = foto,
                contentDescription = "Foto profil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun LabelField(teks: String, wajib: Boolean = false) {
    Row {
        Text(teks, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = pfText)
        if (wajib) Text("  *", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = pfRose)
    }
}

@Composable
private fun Petunjuk(teks: String) {
    Text(teks, fontSize = 13.sp, lineHeight = 18.sp, color = pfMuted)
}

@Composable
private fun warnaKolom() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = pfMuted,
    unfocusedBorderColor = pfLine,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = pfField,
    disabledBorderColor = pfLine,
    disabledTextColor = pfMuted,
    cursorColor = pfRose
)