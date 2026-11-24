package com.example.temu.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

private val avRose = Color(0xFF89465F)

/**
 * Avatar kecil untuk header (mis. kanan atas Home).
 * Urutan tampilan: foto profil -> inisial -> ikon orang (saat profil belum dimuat).
 */
@Composable
fun AvatarProfil(
    fotoUrl: String?,
    inisial: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    ukuran: Dp = 44.dp
) {
    Box(
        modifier = modifier
            .size(ukuran)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFFF9E6EC), Color(0xFFEBC5D2))))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (inisial != null) {
            Text(
                inisial,
                fontSize = (ukuran.value * 0.36f).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = avRose
            )
        } else {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                tint = avRose,
                modifier = Modifier.size(ukuran * 0.5f)
            )
        }
        // Foto menutupi inisial; kalau gagal dimuat, inisial tetap terlihat
        if (fotoUrl != null) {
            AsyncImage(
                model = fotoUrl,
                contentDescription = "Profil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}