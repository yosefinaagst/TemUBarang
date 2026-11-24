package com.example.temu.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temu.R

// Urutan tab di navigasi bawah
const val TAB_JELAJAH = 0
const val TAB_KLAIM_SAYA = 1
const val TAB_TERSIMPAN = 2
const val TAB_STATUS_SAYA = 3
const val TAB_PROFIL = 4

private val navBerry = Color(0xFF89465F)
private val navBlush = Color(0xFFF2E6EB)
private val navMuted = Color(0xFF72666D)

/**
 * Navigasi bawah yang dipakai bersama oleh Home (Jelajah) dan Klaim Saya.
 *
 * @param terpilih        tab yang sedang aktif (TAB_JELAJAH, TAB_KLAIM_SAYA, ...)
 * @param onPilih         dipanggil dengan nomor tab yang ditekan
 * @param tambahInsetBawah true kalau layar pemanggil BELUM memberi padding untuk
 *                         gesture bar (mis. di dalam Scaffold). Home sudah memakai
 *                         safeDrawingPadding, jadi biarkan false.
 */
@Composable
fun NavigasiBawah(
    terpilih: Int,
    onPilih: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tambahInsetBawah: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .then(if (tambahInsetBawah) Modifier.navigationBarsPadding() else Modifier)
            .padding(vertical = 8.dp)
    ) {
        listOf(
            R.drawable.home_jelajah to "Jelajah",
            R.drawable.home_klaim to "Klaim Saya",
            R.drawable.home_tersimpan to "Tersimpan",
            R.drawable.home_status to "Status Saya",
            R.drawable.home_profil to "Profil"
        ).forEachIndexed { index, (icon, label) ->
            val aktif = index == terpilih
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = !aktif) { onPilih(index) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .background(if (aktif) navBlush else Color.Transparent, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = label,
                        modifier = Modifier.size(18.dp),
                        tint = if (aktif) navBerry else navMuted
                    )
                }
                Text(
                    label,
                    fontSize = 10.sp,
                    color = if (aktif) navBerry else navMuted
                )
            }
        }
    }
}