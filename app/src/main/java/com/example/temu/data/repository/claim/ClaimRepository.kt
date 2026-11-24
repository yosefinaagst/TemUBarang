package com.example.temu.data.repository.claim

import com.example.temu.data.remote.ClaimRemoteDataSource

class ClaimRepository(
    private val remote: ClaimRemoteDataSource = ClaimRemoteDataSource()
) {
    suspend fun ringkasanLaporan(reportId: String) = remote.ringkasanLaporan(reportId)
    suspend fun fotoUrl(path: String?) = remote.fotoUrl(path)
    suspend fun ajukan(reportId: String, ciri: String, keterangan: String?) =
        remote.ajukan(reportId, ciri, keterangan)
    suspend fun klaimSayaUntuk(reportId: String) = remote.klaimSayaUntuk(reportId)
    suspend fun daftarMilikSaya() = remote.daftarMilikSaya()
    suspend fun detail(id: String) = remote.detail(id)
    suspend fun ubah(id: String, ciri: String, keterangan: String?) = remote.ubah(id, ciri, keterangan)
    suspend fun hapus(id: String) = remote.hapus(id)
}