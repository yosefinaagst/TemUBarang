
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.temu.ui.screens.auth.LoginScreen
import com.example.temu.ui.screens.auth.OnboardingScreen
import com.example.temu.ui.screens.auth.RegisterScreen
import com.example.temu.ui.screens.auth.SplashScreen
import com.example.temu.ui.screens.temuan.FormTemuanRoute
import com.example.temu.ui.screens.temuan.HomeRoute
import com.example.temu.ui.theme.TextPrimary
import com.example.temu.ui.theme.WarmWhite
import com.example.temu.viewmodel.AuthViewModel
import com.example.temu.viewmodel.TemuanViewModel
import kotlinx.coroutines.delay
import androidx.navigation.navArgument
import com.example.temu.ui.screens.temuan.DetailTemuanRoute
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.temu.ui.screens.temuan.DetailTemuanRoute
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
                }
            )
        }

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
                }
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
    }
}
