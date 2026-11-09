package com.example.temu.data.repository.report

import com.example.temu.data.remote.TemuanRemoteDataSource
import com.example.temubarang.data.model.FormTemuan

class TemuanRepository(
    private val remote: TemuanRemoteDataSource = TemuanRemoteDataSource()
) {

    suspend fun daftar() = remote.daftar()

    suspend fun fotoUrl(path: String) =
        remote.fotoUrl(path)

    suspend fun simpan(
        data: FormTemuan,
        foto: ByteArray,
        mime: String
    ) {
        remote.simpan(data, foto, mime)
    }
}