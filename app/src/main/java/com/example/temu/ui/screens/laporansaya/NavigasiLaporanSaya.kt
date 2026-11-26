package com.example.temu.ui.screens.laporansaya

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.temu.ui.screens.hilang.RuteHilang
import com.example.temu.viewmodel.laporansaya.DetailLaporanSayaViewModel
import com.example.temu.viewmodel.laporansaya.LaporanSayaViewModel
import com.example.temu.viewmodel.laporansaya.UbahStatusViewModel

/**
 * F1 Halaman 2 · Route Laporan Saya
 * Alur: Laporan Saya (tab) → Detail + riwayat → Ubah status / Edit / Hapus
 * Edit memakai form Halaman 1 (RuteHilang.EDIT), sesuai PRD.
 */
object RuteLaporanSaya {
    const val DAFTAR = "laporan_saya?tab={tab}"
    const val DETAIL = "laporan_saya/detail/{id}"
    const val UBAH_STATUS = "laporan_saya/status/{id}"

    /** Kunci savedStateHandle untuk meminta daftar membuka tab tertentu. */
    const val KUNCI_TAB = "buka_tab"

    fun daftar(tab: Int = 0) = "laporan_saya?tab=$tab"
    fun detail(id: String) = "laporan_saya/detail/$id"
    fun ubahStatus(id: String) = "laporan_saya/status/$id"
}

/**
 * Navigasi F1 Halaman 2 (tab "Status Saya" di navigasi bawah). 1 layar = 1 ViewModel.
 * Dipasang di AppNavigation dengan: laporanSayaGraph(navController, onNavigasiBawah = pindahTab)
 *
 * @param onNavigasiBawah pindah tab navigasi bawah (fungsi pindahTab milik AppNavigation)
 */
fun NavGraphBuilder.laporanSayaGraph(
    navController: NavHostController,
    onNavigasiBawah: (Int) -> Unit
) {

    // ---------------- Laporan Saya (ViewPager) ----------------
    composable(
        route = RuteLaporanSaya.DAFTAR,
        arguments = listOf(
            navArgument("tab") {
                type = NavType.IntType
                defaultValue = 0
            }
        )
    ) { entry ->
        val viewModel: LaporanSayaViewModel = viewModel()
        val state by viewModel.uiState.collectAsState()
        val tabDiminta by entry.savedStateHandle
            .getStateFlow<Int?>(RuteLaporanSaya.KUNCI_TAB, null)
            .collectAsState()

        LaporanSayaScreen(
            state = state,
            tabAwal = entry.arguments?.getInt("tab") ?: 0,
            tabDiminta = tabDiminta,
            onTabDibuka = { entry.savedStateHandle[RuteLaporanSaya.KUNCI_TAB] = null },
            onMuatUlang = viewModel::muatTab,
            onMuatLagi = viewModel::muatBerikutnya,
            onPesanTampil = viewModel::pesanSudahTampil,
            onDetail = { id -> navController.navigate(RuteLaporanSaya.detail(id)) },
            onBukaJelajah = { bukaTab(navController, RuteHilang.JELAJAH) },
            onNavigasiBawah = onNavigasiBawah
        )
    }

    // ---------------- Detail + riwayat ----------------
    composable(
        route = RuteLaporanSaya.DETAIL,
        arguments = listOf(navArgument("id") { type = NavType.StringType })
    ) { entry ->
        val id = entry.arguments?.getString("id").orEmpty()
        val viewModel: DetailLaporanSayaViewModel = viewModel()
        val state by viewModel.uiState.collectAsState()

        // Setelah dihapus: kembali ke daftar Laporan Saya.
        LaunchedEffect(state.hapusSelesai) {
            if (state.hapusSelesai) {
                viewModel.hapusSelesaiDitangani()
                kembaliKeDaftar(navController, tab = null)
            }
        }

        DetailLaporanSayaScreen(
            state = state,
            onKembali = { navController.popBackStack() },
            onCobaLagi = viewModel::muat,
            onUbahStatus = { navController.navigate(RuteLaporanSaya.ubahStatus(id)) },
            onEdit = { navController.navigate(RuteHilang.edit(id)) },
            onHapus = viewModel::hapus,
            onPesanTampil = viewModel::pesanSudahTampil
        )
    }

    // ---------------- Ubah status ----------------
    composable(
        route = RuteLaporanSaya.UBAH_STATUS,
        arguments = listOf(navArgument("id") { type = NavType.StringType })
    ) {
        val viewModel: UbahStatusViewModel = viewModel()
        val state by viewModel.uiState.collectAsState()

        // Sukses: kembali ke Laporan Saya pada tab status terbaru (PRD aturan back).
        LaunchedEffect(state.statusTersimpan) {
            val status = state.statusTersimpan
            if (status != null) {
                viewModel.navigasiSelesai()
                kembaliKeDaftar(navController, tab = UbahStatusViewModel.indeksTab(status))
            }
        }

        UbahStatusScreen(
            state = state,
            onPilihStatus = viewModel::pilihStatus,
            onCatatan = viewModel::ubahCatatan,
            onValidasi = viewModel::validasi,
            onSimpan = viewModel::simpan,
            onMuatUlang = viewModel::muat,
            onKembali = { navController.popBackStack() }
        )
    }
}

/** Kembali ke daftar Laporan Saya (dibuka baru bila belum ada di back stack). */
private fun kembaliKeDaftar(navController: NavHostController, tab: Int?) {
    val daftar = try {
        navController.getBackStackEntry(RuteLaporanSaya.DAFTAR)
    } catch (_: IllegalArgumentException) {
        null
    }
    if (daftar != null) {
        if (tab != null) daftar.savedStateHandle[RuteLaporanSaya.KUNCI_TAB] = tab
        navController.popBackStack(RuteLaporanSaya.DAFTAR, inclusive = false)
    } else {
        navController.navigate(RuteLaporanSaya.daftar(tab ?: 0)) {
            popUpTo(RuteLaporanSaya.DETAIL) { inclusive = true }
        }
    }
}

/** Buka Jelajah · Hilang (dari ajakan tab kosong); Laporan Saya ditutup dari back stack. */
private fun bukaTab(navController: NavHostController, rute: String) {
    navController.navigate(rute) {
        popUpTo(RuteLaporanSaya.DAFTAR) { inclusive = true }
        launchSingleTop = true
    }
}
