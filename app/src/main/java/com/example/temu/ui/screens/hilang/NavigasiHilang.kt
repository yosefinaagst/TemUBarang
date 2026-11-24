package com.example.temu.ui.screens.hilang

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.temu.viewmodel.hilang.DetailHilangViewModel
import com.example.temu.viewmodel.hilang.FormHilangViewModel
import com.example.temu.viewmodel.hilang.JelajahHilangViewModel

/**
 * F1 · Route Laporan Barang Hilang
 * Alur: Jelajah · Hilang → Detail → Form Tambah / Edit
 */
object RuteHilang {
    const val JELAJAH = "hilang"
    const val DETAIL = "hilang/detail/{id}?baru={baru}"
    const val TAMBAH = "hilang/tambah"
    const val EDIT = "hilang/edit/{id}"

    fun detail(id: String, baru: Boolean = false) = "hilang/detail/$id?baru=$baru"
    fun edit(id: String) = "hilang/edit/$id"
}

/**
 * Navigasi F1. Di sinilah ViewModel dipakai:
 *   1 layar = 1 ViewModel (dibuat per halaman dengan viewModel()),
 *   state dari ViewModel dikirim ke Screen, aksi Screen diteruskan ke ViewModel.
 * Screen sendiri tidak mengenal ViewModel sehingga bisa dipakai ulang.
 *
 * Dipasang di AppNavigation dengan:
 *     laporanHilangGraph(navController, ruteDitemukan = Screen.HOME)
 *
 * @param ruteDitemukan   route layar Jelajah · Ditemukan (F3) untuk tombol Hilang|Ditemukan.
 * @param onUbahStatus    buka layar Status Saya untuk laporan ini (null = tombol disembunyikan).
 * @param onSimpanLaporan simpan ke Tersimpan / F4 (null = tombol disembunyikan).
 * @param onBukaLaporanSaya buka F1 Halaman 2 dari navigasi bawah (menu "Laporan Saya").
 */
fun NavGraphBuilder.laporanHilangGraph(
    navController: NavHostController,
    ruteDitemukan: String,
    onUbahStatus: ((String) -> Unit)? = null,
    onSimpanLaporan: ((String) -> Unit)? = null,
    onBukaLaporanSaya: (() -> Unit)? = null
) {
    // ---------------- Jelajah · Hilang ----------------
    composable(RuteHilang.JELAJAH) {
        val viewModel: JelajahHilangViewModel = viewModel()
        val state by viewModel.uiState.collectAsState()

        JelajahHilangScreen(
            state = state,
            onKataKunci = viewModel::ubahKataKunci,
            onKategori = viewModel::pilihKategori,
            onTerapkanFilter = viewModel::terapkanFilter,
            onReset = viewModel::resetPencarianDanFilter,
            onMuatUlang = { viewModel.muat() },
            onMuatLagi = viewModel::muatBerikutnya,
            onPesanTampil = viewModel::pesanSudahTampil,
            onBukaDitemukan = {
                navController.navigate(ruteDitemukan) {
                    popUpTo(RuteHilang.JELAJAH) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onTambah = { navController.navigate(RuteHilang.TAMBAH) },
            onDetail = { id -> navController.navigate(RuteHilang.detail(id)) },
            onNavigasiBawah = { index -> if (index == 3) onBukaLaporanSaya?.invoke() }
        )
    }

    // ---------------- Detail ----------------
    composable(
        route = RuteHilang.DETAIL,
        arguments = listOf(
            navArgument("id") { type = NavType.StringType },
            navArgument("baru") {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) { entry ->
        val id = entry.arguments?.getString("id").orEmpty()
        val viewModel: DetailHilangViewModel = viewModel()
        val state by viewModel.uiState.collectAsState()

        // Setelah dihapus: kembali ke Jelajah (buka baru bila tidak ada di back stack).
        LaunchedEffect(state.hapusSelesai) {
            if (state.hapusSelesai) {
                viewModel.hapusSelesaiDitangani()
                if (!navController.popBackStack(RuteHilang.JELAJAH, inclusive = false)) {
                    navController.navigate(RuteHilang.JELAJAH)
                }
            }
        }

        DetailHilangScreen(
            state = state,
            onKembali = { navController.popBackStack() },
            onCobaLagi = viewModel::muat,
            onEdit = { navController.navigate(RuteHilang.edit(id)) },
            onHapus = viewModel::hapus,
            onPesanTampil = viewModel::pesanSudahTampil,
            onUbahStatus = onUbahStatus?.let { buka -> { buka(id) } },
            onSimpanLaporan = onSimpanLaporan?.let { simpan -> { simpan(id) } },
        )
    }

    // ---------------- Form tambah ----------------
    composable(RuteHilang.TAMBAH) {
        val viewModel: FormHilangViewModel = viewModel()
        val state by viewModel.uiState.collectAsState()

        // Setelah terbit: buka detail laporan baru, form dikeluarkan dari back stack.
        LaunchedEffect(state.idTerbit) {
            val idBaru = state.idTerbit
            if (idBaru != null) {
                viewModel.navigasiSelesai()
                navController.navigate(RuteHilang.detail(idBaru, baru = true)) {
                    popUpTo(RuteHilang.TAMBAH) { inclusive = true }
                }
            }
        }

        FormHilangScreen(
            state = state,
            onUbah = viewModel::ubahForm,
            onPilihFoto = viewModel::pilihFoto,
            onHapusFoto = viewModel::hapusFoto,
            onKirim = viewModel::kirim,
            onMuatUlang = viewModel::muatUntukEdit,
            onKembali = { navController.popBackStack() }
        )
    }

    // ---------------- Form edit ----------------
    composable(
        route = RuteHilang.EDIT,
        arguments = listOf(navArgument("id") { type = NavType.StringType })
    ) {
        val viewModel: FormHilangViewModel = viewModel()
        val state by viewModel.uiState.collectAsState()

        // Setelah tersimpan: kembali ke Detail (Detail memuat ulang otomatis).
        LaunchedEffect(state.editSelesai) {
            if (state.editSelesai) {
                viewModel.navigasiSelesai()
                navController.popBackStack()
            }
        }

        FormHilangScreen(
            state = state,
            onUbah = viewModel::ubahForm,
            onPilihFoto = viewModel::pilihFoto,
            onHapusFoto = viewModel::hapusFoto,
            onKirim = viewModel::kirim,
            onMuatUlang = viewModel::muatUntukEdit,
            onKembali = { navController.popBackStack() }
        )
    }
}
