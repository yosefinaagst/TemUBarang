package com.example.temu.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Claim(
    val id: String? = null,
    @SerialName("report_id") val reportId: String,
    @SerialName("claimant_id") val claimantId: String? = null,
    @SerialName("special_characteristics") val ciriKhusus: String,
    val description: String? = null,
    val status: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val reports: LaporanDiklaim? = null      // terisi kalau di-join ke tabel reports
)

@Serializable
data class LaporanDiklaim(
    @SerialName("item_name") val namaBarang: String? = null,
    val location: String? = null,
    @SerialName("event_date") val tanggal: String? = null,
    @SerialName("photo_path") val fotoPath: String? = null,
    val status: String? = null
)