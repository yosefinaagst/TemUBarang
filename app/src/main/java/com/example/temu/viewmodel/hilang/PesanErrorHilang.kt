package com.example.temu.viewmodel.hilang

import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import java.io.IOException

/** Kode error dari database (mis. TB404 = laporan tidak tersedia, TB409 = konflik versi). */
internal fun kodeErrorHilang(e: Throwable): String? = (e as? PostgrestRestException)?.code

/** Mengubah exception menjadi pesan yang bisa dibaca pengguna. */
internal fun pesanErrorHilang(e: Throwable): String {
    val kode = kodeErrorHilang(e)
    return when {
        kode == "PGRST202" ->
            "Fungsi database F1 belum dipasang. Jalankan supabase/f1_laporan_hilang.sql."
        e is PostgrestRestException && (kode == "P0001" || kode?.startsWith("TB") == true) ->
            e.error
        kode == "42501" ->
            "Akunmu tidak memiliki izin untuk tindakan ini."
        e is IOException ->
            "Koneksi bermasalah. Periksa internetmu, lalu coba lagi."
        e is SecurityException ->
            "Foto tidak dapat dibaca. Pilih ulang fotonya."
        e is IllegalArgumentException ->
            e.message ?: "Data belum valid."
        else ->
            "Terjadi kesalahan. Silakan coba lagi."
    }
}
