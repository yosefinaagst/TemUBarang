package com.example.temu.ui.screens.temuan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.temu.viewmodel.TemuanViewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.temu.viewmodel.profil.ProfilViewModel

@Composable
fun HomeRoute(
    viewModel: TemuanViewModel,
    onAddClick: () -> Unit,
    onDetailClick: (String) -> Unit,
    onHilangClick: () -> Unit = {},
    onKlaimSayaClick: () -> Unit = {},
    onProfilClick: () -> Unit = {},
    onTersimpanClick: () -> Unit = {},

    profilViewModel: ProfilViewModel = viewModel(),
) {
    LaunchedEffect(Unit) {
        if (
            viewModel.daftar.isEmpty() ||
            viewModel.penggunaId == null
        ) {
            viewModel.muat()
        }
    }

    val profilState by profilViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (!profilState.memuat) profilViewModel.muat()
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
        },

        onHilangClick = onHilangClick,

        onKlaimSayaClick = onKlaimSayaClick,

        onProfilClick = onProfilClick,

        onTersimpanClick = onTersimpanClick,

        fotoProfil = profilState.profil?.fotoUrl,

        inisialProfil = profilState.profil?.inisial,

    )
}