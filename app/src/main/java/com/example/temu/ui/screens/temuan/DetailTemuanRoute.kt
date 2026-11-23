
package com.example.temu.ui.screens.temuan

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.temu.viewmodel.TemuanViewModel
import kotlinx.coroutines.CancellationException

@Composable
fun DetailTemuanRoute(
    id: String,
    viewModel: TemuanViewModel,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit
) {
    val barang = viewModel.daftar.firstOrNull {
        it.id == id
    }

    var fotoUrl by remember(id) {
        mutableStateOf<String?>(null)
    }

    var fotoLoading by remember(id) {
        mutableStateOf(false)
    }

    var fotoError by remember(id) {
        mutableStateOf(false)
    }

    // Mengambil foto laporan
    LaunchedEffect(id, barang?.foto) {
        if (barang == null || barang.foto.isBlank()) {
            fotoLoading = false
            fotoUrl = null
            return@LaunchedEffect
        }

        fotoLoading = true
        fotoError = false
        fotoUrl = null

        try {
            fotoUrl = viewModel.urlFoto(barang.foto)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            fotoError = true
        } finally {
            fotoLoading = false
        }
    }

    // Kembali ke Home ketika penghapusan berhasil
    LaunchedEffect(viewModel.hapusBerhasil) {
        if (viewModel.hapusBerhasil) {
            viewModel.resetStatus()
            onDeleted()
        }
    }

    if (barang == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Laporan tidak ditemukan.",
                    color = Color(0xFF89465F)
                )
            }
        }
        return
    }

    // Membandingkan ID pemilik dengan pengguna aktif
    val isOwner = barang.ownerId.isNotBlank() &&
            barang.ownerId == viewModel.penggunaId

    DetailTemuanScreen(
        barang = barang,
        fotoUrl = fotoUrl,
        fotoLoading = fotoLoading,
        fotoError = fotoError,
        isOwner = isOwner,

        onBack = onBack,

        onEdit = {
            if (isOwner) {
                onEdit(barang.id)
            }
        },

        onDelete = {
            if (isOwner) {
                viewModel.hapus(barang.id)
            }
        },

        deleting = viewModel.sibuk
    )
}
