package com.example.temu.ui.screens.tersimpan

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.temu.viewmodel.tersimpan.DaftarTersimpanViewModel
import com.example.temu.viewmodel.tersimpan.DetailTersimpanViewModel
import com.example.temu.viewmodel.tersimpan.PilihLaporanViewModel

/**
 * F4 · Route Laporan Tersimpan
 * Alur: Daftar Tersimpan → Pilih Laporan (Tambah) / Detail Tersimpan
 */
object RuteTersimpan {
    const val DAFTAR = "tersimpan"
    const val PILIH = "tersimpan/pilih"
    const val DETAIL = "tersimpan/detail/{id}?simpan={simpan}"

    /** simpan = true → laporan langsung disimpan dulu (dipakai tombol Simpan di F1). */
    fun detail(id: String, simpan: Boolean = false) = "tersimpan/detail/$id?simpan=$simpan"
}

/**
 * Navigasi F4. 1 layar = 1 ViewModel (dibuat per halaman dengan viewModel()).
 *
 * Dipasang di AppNavigation dengan:
 *     laporanTersimpanGraph(navController, onNavigasiBawah = pindahTab)
 *
 * @param onNavigasiBawah dipanggil saat tab navigasi bawah diketuk (index tab).
 */
fun NavGraphBuilder.laporanTersimpanGraph(
    navController: NavHostController,
    onNavigasiBawah: (Int) -> Unit
) {
    // ---------------- Daftar Tersimpan ----------------
    composable(RuteTersimpan.DAFTAR) {
        val viewModel: DaftarTersimpanViewModel = viewModel()
        val state by viewModel.uiState.collectAsState()

        DaftarTersimpanScreen(
            state = state,
            onMuatUlang = viewModel::muat,
            onTambah = { navController.navigate(RuteTersimpan.PILIH) },
            onDetail = { id -> navController.navigate(RuteTersimpan.detail(id)) },
            onPesanTampil = viewModel::pesanSudahTampil,
            onNavigasiBawah = onNavigasiBawah
        )
    }

    // ---------------- Pilih Laporan (Tambah ke Tersimpan) ----------------
    composable(RuteTersimpan.PILIH) {
        val viewModel: PilihLaporanViewModel = viewModel()
        val state by viewModel.uiState.collectAsState()

        PilihLaporanScreen(
            state = state,
            onKembali = { navController.popBackStack() },
            onPilihJenis = viewModel::pilihJenis,
            onSimpan = viewModel::simpan,
            onMuatUlang = viewModel::muat,
            onPesanTampil = viewModel::pesanSudahTampil
        )
    }

    // ---------------- Detail Tersimpan ----------------
    composable(
        route = RuteTersimpan.DETAIL,
        arguments = listOf(
            navArgument("id") { type = NavType.StringType },
            navArgument("simpan") {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) {
        val viewModel: DetailTersimpanViewModel = viewModel()
        val state by viewModel.uiState.collectAsState()

        // Setelah dihapus dari Tersimpan: kembali ke layar sebelumnya.
        LaunchedEffect(state.hapusSelesai) {
            if (state.hapusSelesai) {
                viewModel.hapusSelesaiDitangani()
                navController.popBackStack()
            }
        }

        DetailTersimpanScreen(
            state = state,
            onKembali = { navController.popBackStack() },
            onCobaLagi = viewModel::muat,
            onHapus = viewModel::hapus,
            onPesanTampil = viewModel::pesanSudahTampil
        )
    }
}
