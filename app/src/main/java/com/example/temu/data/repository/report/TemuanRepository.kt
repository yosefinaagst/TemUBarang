package com.example.temubarang.data.repository.report

import com.example.temubarang.data.model.FormTemuan
import com.example.temubarang.data.remote.TemuanRemoteDataSource

class TemuanRepository(private val remote: TemuanRemoteDataSource = TemuanRemoteDataSource()) {
    suspend fun sudahMasuk() = remote.sudahMasuk()
    suspend fun masuk(email: String, sandi: String) = remote.masuk(email, sandi)
    suspend fun daftar() = remote.daftar()
    suspend fun fotoUrl(path: String) = remote.fotoUrl(path)
    suspend fun simpan(data: FormTemuan, foto: ByteArray, mime: String) = remote.simpan(data, foto, mime)
}
