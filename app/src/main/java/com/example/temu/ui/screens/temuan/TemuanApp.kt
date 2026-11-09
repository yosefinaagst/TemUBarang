package com.example.temu.ui.screens.temuan

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.temubarang.data.model.*
import java.time.LocalDate
import java.util.UUID
import com.example.temu.viewmodel.TemuanViewModel
import com.example.temu.viewmodel.Halaman



@Composable
fun TemuanApp(vm: TemuanViewModel = viewModel()) {
    when (vm.halaman) {
        Halaman.HOME -> HomePink(vm)
        Halaman.FORM -> FormTemuanUI(vm)

        Halaman.MEMUAT, Halaman.MASUK -> {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding()
                        .imePadding()
                        .padding(16.dp)
                ) {
                    if (vm.pesan.isNotEmpty()) {
                        Text(
                            text = vm.pesan,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (vm.halaman == Halaman.MASUK) {
                        MasukScreen(vm)
                    } else if (vm.sibuk) {
                        CircularProgressIndicator()
                    } else {
                        Button(onClick = vm::periksaSesi) {
                            Text("Coba lagi")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MasukScreen(vm: TemuanViewModel) {
    var email by rememberSaveable { mutableStateOf("") }
    var sandi by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Masuk ke TemUBarang", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        OutlinedTextField(sandi, { sandi = it }, label = { Text("Kata sandi") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
        Button(onClick = { vm.masuk(email, sandi) },
            enabled = !vm.sibuk && email.isNotBlank() && sandi.isNotEmpty()) {
            Text(if (vm.sibuk) "Sedang masuk…" else "Masuk")
        }
    }
}

@Composable
private fun FormScreen(vm: TemuanViewModel) {
    val id = rememberSaveable { UUID.randomUUID().toString() }
    var nama by rememberSaveable { mutableStateOf("") }
    var lokasi by rememberSaveable { mutableStateOf("") }
    var tanggal by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var deskripsi by rememberSaveable { mutableStateOf("") }
    var kategori by rememberSaveable { mutableStateOf("personal_items") }
    var gambar by rememberSaveable { mutableStateOf<String?>(null) }
    var terbuka by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) gambar = it.toString()
    }
    BackHandler { vm.kembali() }
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text("Lapor barang ditemukan", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(nama, { nama = it }, label = { Text("Nama barang") }, singleLine = true)
        Box {
            OutlinedButton(onClick = { terbuka = true }) { Text("Kategori: ${kategoriBarang[kategori]} ▾") }
            DropdownMenu(expanded = terbuka, onDismissRequest = { terbuka = false }) {
                kategoriBarang.forEach { (kode, namaKategori) ->
                    DropdownMenuItem(text = { Text(namaKategori) },
                        onClick = { kategori = kode; terbuka = false })
                }
            }
        }
        OutlinedTextField(lokasi, { lokasi = it }, label = { Text("Lokasi ditemukan") })
        OutlinedTextField(tanggal, { tanggal = it }, label = { Text("Tanggal (YYYY-MM-DD)") }, singleLine = true)
        OutlinedTextField(deskripsi, { deskripsi = it }, label = { Text("Deskripsi umum") })
        gambar?.let { AsyncImage(model = Uri.parse(it), contentDescription = "Foto pilihan",
            modifier = Modifier.fillMaxWidth().height(135.dp)) }
        OutlinedButton(onClick = { picker.launch("image/*") }, enabled = !vm.sibuk) {
            Text(if (gambar == null) "Pilih foto (JPG/PNG)" else "Ganti foto")
        }
        Button(onClick = {
            vm.simpan(FormTemuan(id, nama, lokasi, tanggal, kategori, deskripsi),
                gambar?.let { Uri.parse(it) })
        }, enabled = !vm.sibuk) { Text(if (vm.sibuk) "Menyimpan…" else "Simpan laporan") }
        TextButton(onClick = vm::kembali, enabled = !vm.sibuk) { Text("Kembali") }
    }
}
