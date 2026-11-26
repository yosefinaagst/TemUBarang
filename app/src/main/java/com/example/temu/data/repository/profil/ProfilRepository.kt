package com.example.temu.data.repository.profil

import com.example.temu.data.remote.ProfilRemoteDataSource

class ProfilRepository(
    private val remote: ProfilRemoteDataSource = ProfilRemoteDataSource()
) {
    suspend fun ambil() = remote.ambil()

    suspend fun simpan(
        namaLengkap: String,
        kontak: String?,
        fotoBaru: ByteArray?,
        hapusFoto: Boolean,
        fotoLama: String?
    ) = remote.simpan(namaLengkap, kontak, fotoBaru, hapusFoto, fotoLama)

    suspend fun keluar() = remote.keluar()
}
