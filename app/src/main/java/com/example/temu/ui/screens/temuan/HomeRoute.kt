
package com.example.temu.ui.screens.temuan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.temu.viewmodel.TemuanViewModel

@Composable
fun HomeRoute(
    viewModel: TemuanViewModel,
    onAddClick: () -> Unit,
    onDetailClick: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        if (
            viewModel.daftar.isEmpty() ||
            viewModel.penggunaId == null
        ) {
            viewModel.muat()
        }
    }

    HomeScreen(
        daftar = viewModel.daftar,
        sibuk = viewModel.sibuk,
        pesan = viewModel.pesan,

        onRefresh = {
            viewModel.muat()
        },

        onAddClick = onAddClick,

        onItemClick = { barang ->
            onDetailClick(barang.id)
        },

        getPhotoUrl = { path ->
            viewModel.urlFoto(path)
        }
    )
}
