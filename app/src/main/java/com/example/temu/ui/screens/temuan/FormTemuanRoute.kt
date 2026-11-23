
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
fun FormTemuanRoute(
    viewModel: TemuanViewModel,
    editId: String? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val initialData = editId?.let { id ->
        viewModel.daftar.firstOrNull { it.id == id }
    }

    var initialPhotoUrl by remember(editId) {
        mutableStateOf<String?>(null)
    }

    // Jika data edit belum ada di daftar, coba muat ulang
    LaunchedEffect(editId) {
        if (editId != null && initialData == null) {
            viewModel.muat()
        }
    }

    // Ambil URL foto lama saat mode edit
    LaunchedEffect(editId, initialData?.foto) {
        if (initialData != null && initialData.foto.isNotBlank()) {
            try {
                initialPhotoUrl = viewModel.urlFoto(initialData.foto)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                initialPhotoUrl = null
            }
        }
    }

    // Kembali setelah simpan atau edit berhasil
    LaunchedEffect(
        viewModel.simpanBerhasil,
        viewModel.perbaruiBerhasil
    ) {
        if (
            viewModel.simpanBerhasil ||
            viewModel.perbaruiBerhasil
        ) {
            viewModel.resetStatus()
            onSaved()
        }
    }

    if (editId != null && initialData == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF89465F))
        }
    } else {
        FormTemuanScreen(
            initialData = initialData,
            initialPhotoUrl = initialPhotoUrl,
            sibuk = viewModel.sibuk,
            pesan = viewModel.pesan,
            onBack = onBack,
            onSubmit = { data, uri, isEdit ->
                if (isEdit) {
                    viewModel.perbarui(data, uri)
                } else {
                    viewModel.simpan(data, uri)
                }
            }
        )
    }
}
