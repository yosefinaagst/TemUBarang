package com.example.temubarang.data.model

data class Temuan(
    val id: String,
    val nama: String,
    val lokasi: String,
    val tanggal: String,
    val foto: String,
    val deskripsi: String,
    val status: String,
    val kategori: String
)

data class FormTemuan(
    val id: String,
    val nama: String,
    val lokasi: String,
    val tanggal: String,
    val kategori: String = "",
    val deskripsi: String
)

val kategoriBarang = mapOf(
    "electronics" to "Elektronik", "wallet_bag" to "Dompet dan Tas",
    "keys" to "Kunci", "cards_documents" to "Kartu dan Dokumen",
    "accessories" to "Aksesori", "books_stationery" to "Buku dan Alat Tulis",
    "personal_items" to "Perlengkapan Pribadi", "other" to "Lainnya"
)