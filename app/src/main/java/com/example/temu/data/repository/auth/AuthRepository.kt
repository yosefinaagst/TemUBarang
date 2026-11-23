
package com.example.temu.data.repository.auth

import com.example.temu.data.remote.AuthRemoteDataSource

class AuthRepository(
    private val remote: AuthRemoteDataSource = AuthRemoteDataSource()
) {

    suspend fun sudahMasuk(): Boolean {
        return remote.sudahMasuk()
    }

    suspend fun masuk(email: String, sandi: String) {
        remote.masuk(email, sandi)
    }

    suspend fun daftar(
        namaLengkap: String,
        email: String,
        sandi: String
    ) {
        remote.daftar(namaLengkap, email, sandi)
    }

    suspend fun keluar() {
        remote.keluar()
    }
}
