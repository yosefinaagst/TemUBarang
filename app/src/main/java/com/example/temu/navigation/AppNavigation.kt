package com.example.temu.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.temu.ui.screens.auth.LoginScreen
import com.example.temu.ui.screens.auth.OnboardingScreen
import com.example.temu.ui.screens.auth.RegisterScreen
import com.example.temu.ui.screens.auth.SplashScreen
import com.example.temu.ui.screens.claim.DetailKlaimScreen
import com.example.temu.ui.screens.claim.FormKlaimScreen
import com.example.temu.ui.screens.claim.KlaimSayaScreen
import com.example.temu.ui.screens.components.NavigasiBawah
import com.example.temu.ui.screens.components.TAB_JELAJAH
import com.example.temu.ui.screens.components.TAB_KLAIM_SAYA
import com.example.temu.ui.screens.hilang.RuteHilang
import com.example.temu.ui.screens.hilang.laporanHilangGraph
import com.example.temu.ui.screens.temuan.DetailTemuanRoute
import com.example.temu.ui.screens.temuan.FormTemuanRoute
import com.example.temu.ui.screens.temuan.HomeRoute
import com.example.temu.ui.theme.TextPrimary
import com.example.temu.ui.theme.WarmWhite
import com.example.temu.viewmodel.AuthViewModel
import com.example.temu.viewmodel.TemuanViewModel
import kotlinx.coroutines.delay

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // ViewModel autentikasi
    val authViewModel: AuthViewModel = viewModel()

    // ViewModel laporan temuan
    // Instance yang sama digunakan oleh Home dan Form
    val temuanViewModel: TemuanViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.SPLASH
    ) {

        // SPLASH
        composable(Screen.SPLASH) {
            SplashScreen()

            LaunchedEffect(Unit) {
                delay(1500)

                navController.navigate(Screen.ONBOARDING) {
                    popUpTo(Screen.SPLASH) {
                        inclusive = true
                    }
                }
            }
        }

        // ONBOARDING
        composable(Screen.ONBOARDING) {
            OnboardingScreen(
                onLoginClick = {
                    navController.navigate(Screen.LOGIN)
                },
                onFinish = {
                    navController.navigate(Screen.LOGIN) {
                        popUpTo(Screen.ONBOARDING) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // LOGIN
        composable(Screen.LOGIN) {
            LaunchedEffect(authViewModel.loginBerhasil) {
                if (authViewModel.loginBerhasil) {
                    navController.navigate(Screen.HOME) {
                        popUpTo(Screen.LOGIN) {
                            inclusive = true
                        }
                    }
                }
            }

            LoginScreen(
                sibuk = authViewModel.sibuk,
                pesan = authViewModel.pesan,

                onLogin = { email, sandi ->
                    authViewModel.resetStatus()
                    authViewModel.masuk(email, sandi)
                },

                onRegisterClick = {
                    authViewModel.resetStatus()
                    navController.navigate(Screen.REGISTER)
                }
            )
        }

        // REGISTER
        composable(Screen.REGISTER) {
            RegisterScreen(
                sibuk = authViewModel.sibuk,
                pesan = authViewModel.pesan,
                pendaftaranBerhasil = authViewModel.daftarBerhasil,

                onRegister = { nama, email, sandi, konfirmasi ->
                    authViewModel.resetStatus()

                    authViewModel.daftar(
                        namaLengkap = nama,
                        email = email,
                        sandi = sandi,
                        konfirmasiSandi = konfirmasi
                    )
                },

                onLoginClick = {
                    authViewModel.resetStatus()
                    navController.popBackStack()
                }
            )
        }

        // HOME F3
        composable(Screen.HOME) {
            HomeRoute(
                viewModel = temuanViewModel,

                onAddClick = {
                    navController.navigate(Screen.FORM_TEMUAN)
                },

                onDetailClick = { id ->
                    navController.navigate(
                        Screen.detailTemuan(id)
                    )
                },

                // F1: pindah ke tab Hilang
                onHilangClick = {
                    navController.navigate(RuteHilang.JELAJAH) {
                        popUpTo(Screen.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },

                // F2: pindah ke tab Klaim Saya
                onKlaimSayaClick = {
                    navController.navigate(Screen.KLAIM_SAYA) {
                        popUpTo(Screen.HOME)
                        launchSingleTop = true
                    }
                }
            )
        }

        // F1 LAPORAN BARANG HILANG (Jelajah · Hilang, detail, tambah, edit)
        // ViewModel tiap layar F1 dibuat di dalam laporanHilangGraph (1 layar = 1 ViewModel)
        laporanHilangGraph(
            navController = navController,
            ruteDitemukan = Screen.HOME
        )

        // FORM LAPORAN TEMUAN
        composable(Screen.FORM_TEMUAN) {
            FormTemuanRoute(
                viewModel = temuanViewModel,
                editId = null,
                onBack = {
                    navController.popBackStack()
                },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

        //DETAIL
        composable(
            route = Screen.DETAIL_TEMUAN,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val id = backStackEntry.arguments
                ?.getString("id")
                .orEmpty()

            DetailTemuanRoute(
                id = id,
                viewModel = temuanViewModel,

                onBack = {
                    navController.popBackStack()
                },

                onEdit = { reportId ->
                    navController.navigate(
                        Screen.editTemuan(reportId)
                    )
                },

                onDeleted = {
                    navController.popBackStack(
                        Screen.HOME,
                        inclusive = false
                    )
                },

                onKlaim = { reportId ->
                    navController.navigate(
                        Screen.formKlaim(reportId)
                    )
                },
            )
        }

        //EDIT
        composable(
            route = Screen.EDIT_TEMUAN,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val id = backStackEntry.arguments
                ?.getString("id")
                .orEmpty()

            FormTemuanRoute(
                viewModel = temuanViewModel,
                editId = id,

                onBack = {
                    navController.popBackStack()
                },

                onSaved = {
                    // Kembali ke halaman Detail setelah edit
                    navController.popBackStack()
                }
            )
        }

        // F2 · KLAIM SAYA (tab di bottom nav)
        composable(Screen.KLAIM_SAYA) {
            KlaimSayaScreen(
                onItemClick = { klaimId ->
                    navController.navigate(Screen.detailKlaim(klaimId))
                },
                bottomBar = {
                    NavigasiBawah(
                        terpilih = TAB_KLAIM_SAYA,
                        tambahInsetBawah = true,
                        onPilih = { tab ->
                            if (tab == TAB_JELAJAH) {
                                navController.navigate(Screen.HOME) {
                                    popUpTo(Screen.HOME)
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            )
        }

        // F2 · AJUKAN KLAIM (form + layar "Pengajuan berhasil dikirim")
        composable(
            route = Screen.FORM_KLAIM,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val reportId = entry.arguments?.getString("id") ?: return@composable
            FormKlaimScreen(
                reportId = reportId,
                onBack = { navController.popBackStack() },
                onLihatDetailKlaim = { klaimId ->
                    // Form klaim dikeluarkan dari back stack -> Back dari Detail ke Detail temuan
                    navController.navigate(Screen.detailKlaim(klaimId)) {
                        popUpTo(Screen.FORM_KLAIM) { inclusive = true }
                    }
                },
                onKembaliKeJelajah = {
                    navController.popBackStack(Screen.HOME, inclusive = false)
                }
            )
        }

        // F2 · DETAIL KLAIM (+ edit / hapus)
        composable(
            route = Screen.DETAIL_KLAIM,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { entry ->
            val klaimId = entry.arguments?.getString("id") ?: return@composable
            DetailKlaimScreen(
                klaimId = klaimId,
                onBack = { navController.popBackStack() },
                onDihapus = {
                    // Kembali ke daftar Klaim Saya yang baru (dimuat ulang)
                    navController.navigate(Screen.KLAIM_SAYA) {
                        popUpTo(Screen.HOME)
                    }
                }
            )
        }
    }
}