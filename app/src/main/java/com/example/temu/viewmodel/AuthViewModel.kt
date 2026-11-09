
package com.example.temu.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temu.data.repository.auth.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    var sibuk by mutableStateOf(false)
        private set

    var pesan by mutableStateOf("")
        private set

    var loginBerhasil by mutableStateOf(false)
        private set

    var daftarBerhasil by mutableStateOf(false)
        private set


    private fun pesanError(error: Exception): String {
        val detail = error.message.orEmpty().lowercase()

        return when {
            "email_not_confirmed" in detail ||
                    "email not confirmed" in detail -> {
                "Email belum dikonfirmasi. Silakan periksa inbox email kamu."
            }

            "invalid login credentials" in detail -> {
                "Email atau kata sandi salah."
            }

            "user already registered" in detail -> {
                "Email sudah terdaftar. Silakan masuk."
            }

            "request timeout" in detail ||
                    "timeout" in detail -> {
                "Koneksi ke server terputus atau terlalu lama. Periksa internet dan coba lagi."
            }

            "network" in detail ||
                    "unable to resolve host" in detail -> {
                "Tidak dapat terhubung ke server. Periksa koneksi internet."
            }

            else -> {
                "Terjadi kesalahan. Silakan coba kembali."
            }
        }
    }

    fun masuk(email: String, sandi: String) {
        if (sibuk) return

        viewModelScope.launch {
            sibuk = true
            pesan = ""
            loginBerhasil = false
            daftarBerhasil = false

            try {
                require(email.isNotBlank()) {
                    "Email harus diisi."
                }

                require(sandi.isNotBlank()) {
                    "Kata sandi harus diisi."
                }

                repository.masuk(email, sandi)
                loginBerhasil = true

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                pesan = pesanError(e)
            } finally {
                sibuk = false
            }
        }
    }

    fun daftar(
        namaLengkap: String,
        email: String,
        sandi: String,
        konfirmasiSandi: String
    ) {
        if (sibuk) return

        viewModelScope.launch {
            sibuk = true
            pesan = ""
            loginBerhasil = false
            daftarBerhasil = false

            try {
                require(namaLengkap.isNotBlank()) {
                    "Nama lengkap harus diisi."
                }

                require(email.isNotBlank()) {
                    "Email harus diisi."
                }

                require(
                    android.util.Patterns.EMAIL_ADDRESS
                        .matcher(email.trim()).matches()
                ) {
                    "Format email tidak valid."
                }

                require(sandi.length >= 8) {
                    "Kata sandi minimal 8 karakter."
                }

                require(konfirmasiSandi.isNotBlank()) {
                    "Konfirmasi kata sandi harus diisi."
                }

                require(sandi == konfirmasiSandi) {
                    "Konfirmasi kata sandi tidak sama."
                }

                repository.daftar(
                    namaLengkap = namaLengkap,
                    email = email,
                    sandi = sandi
                )

                daftarBerhasil = true
                pesan = "Pendaftaran berhasil. Periksa email untuk konfirmasi jika diminta."

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {

                Log.e(
                    "TEMUBARANG_AUTH",
                    "Register gagal",
                    e
                )

                pesan = pesanError(e)

            } finally {
                sibuk = false
            }
        }
    }

    fun resetStatus() {
        pesan = ""
        loginBerhasil = false
        daftarBerhasil = false
    }
}
