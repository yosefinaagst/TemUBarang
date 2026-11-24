package com.example.temu.data.model

/**
 * Data profil pengguna.
 * - namaLengkap, kontak, fotoPath berasal dari tabel `profiles`
 *   (kolom full_name, contact, avatar_path)
 * - email berasal dari Supabase Auth (read-only)
 * - fotoUrl = signed URL dari bucket "foto-profil" (null kalau belum ada foto)
 */
data class Profil(
    val id: String,
    val namaLengkap: String,
    val email: String,
    val kontak: String? = null,
    val fotoPath: String? = null,
    val fotoUrl: String? = null
) {
    /** Inisial untuk avatar, mis. "Ayu Pratiwi" -> "AP". */
    val inisial: String
        get() = namaLengkap.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "?" }
}
