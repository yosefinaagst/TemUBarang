
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
import com.example.temu.ui.theme.TextPrimary
import com.example.temu.ui.theme.WarmWhite
import com.example.temu.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    // Satu AuthViewModel digunakan bersama oleh Login dan Register
    val authViewModel: AuthViewModel = viewModel()

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

        // HOME SEMENTARA
        composable(Screen.HOME) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(WarmWhite),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Login berhasil! Selamat datang di TemUBarang.",
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            }
        }
    }
}
