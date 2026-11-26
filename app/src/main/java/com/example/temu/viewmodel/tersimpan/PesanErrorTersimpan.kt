package com.example.temu.viewmodel.tersimpan

import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import java.io.IOException

/** Mengubah exception menjadi pesan yang bisa dibaca pengguna. */
internal fun pesanErrorTersimpan(e: Throwable): String = when {
    e is PostgrestRestException && e.code == "42501" ->
        "Akses data laporan ditolak. Coba masuk ulang."
    e is IOException ->
        "Koneksi bermasalah. Periksa internetmu, lalu coba lagi."
    e is IllegalArgumentException ->
        e.message ?: "Data belum valid."
    else ->
        "Terjadi kesalahan. Silakan coba lagi."
}
