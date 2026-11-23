
package com.example.temu.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temu.ui.theme.*

private data class OnboardingPage(
    val title: String,
    val description: String,
    val illustration: Int
)

private val onboardingPages = listOf(
    OnboardingPage(
        title = "Barang hilang?\nMulai cari di sini.",
        description = "Jelajahi laporan barang di sekitar kampus dan temukan yang mungkin milikmu.",
        illustration = 1
    ),
    OnboardingPage(
        title = "Temuan kecil,\nbantuan berarti.",
        description = "Laporkan barang yang kamu temukan agar pemiliknya bisa menemukannya kembali.",
        illustration = 2
    ),
    OnboardingPage(
        title = "Kenali barangmu,\najukan klaim.",
        description = "Kenali ciri khusus barangmu agar penemu dapat memeriksa kepemilikannya.",
        illustration = 3
    )
)

@Composable
fun OnboardingScreen(
    onLoginClick: () -> Unit,
    onFinish: () -> Unit
) {
    var halamanAktif by remember { mutableIntStateOf(0) }

    val halaman = onboardingPages[halamanAktif]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(LightGray)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "TemUBarang",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = onFinish
            ) {
                Text(
                    text = "Lewati",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Ilustrasi
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp),
            contentAlignment = Alignment.Center
        ) {
            OnboardingIllustration(
                type = halaman.illustration
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Indikator halaman
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .width(if (index == halamanAktif) 22.dp else 7.dp)
                        .height(6.dp)
                        .background(
                            color = if (index == halamanAktif) {
                                Maroon
                            } else {
                                DustyPink
                            },
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Judul
        Text(
            text = halaman.title,
            fontSize = 27.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Deskripsi
        Text(
            text = halaman.description,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.weight(1f))

        // Tombol utama
        Button(
            onClick = {
                if (halamanAktif < onboardingPages.lastIndex) {
                    halamanAktif++
                } else {
                    onFinish()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Maroon,
                contentColor = TextWhite
            )
        ) {
            Text(
                text = if (halamanAktif == 2) "Mulai" else "Lanjut",
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigasi login
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sudah punya akun?",
                color = TextSecondary,
                fontSize = 12.sp
            )

            TextButton(onClick = onLoginClick) {
                Text(
                    text = "Masuk",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun OnboardingIllustration(type: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Kartu utama
        Box(
            modifier = Modifier
                .size(width = 165.dp, height = 175.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(24.dp)
                )
                .background(
                    LightGray,
                    RoundedCornerShape(24.dp)
                )
        )

        when (type) {
            1 -> {
                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(y = 45.dp)
                        .size(90.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = LightGray,
                    shadowElevation = 8.dp
                ) {}

                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = 55.dp)
                        .size(90.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = LightGray,
                    shadowElevation = 8.dp
                ) {}

                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = (-65).dp, y = (-20).dp),
                    shape = RoundedCornerShape(12.dp),
                    color = WarmWhite,
                    shadowElevation = 5.dp
                ) {
                    Text(
                        text = "Botol minum...",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 10.sp,
                        color = TextPrimary
                    )
                }
            }

            2 -> {
                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(y = 50.dp)
                        .size(90.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = LightGray,
                    shadowElevation = 8.dp
                ) {}

                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = 50.dp)
                        .size(90.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = LightGray,
                    shadowElevation = 8.dp
                ) {}

                Surface(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = (-5).dp),
                    shape = RoundedCornerShape(12.dp),
                    color = WarmWhite,
                    shadowElevation = 5.dp
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(
                            text = "Ditemukan",
                            color = SuccessColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tas tote beige",
                            color = TextPrimary,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            3 -> {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(0.92f)
                        .offset(y = 50.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = WarmWhite,
                    shadowElevation = 7.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "CIRI KHUSUS · PRIVAT",
                            color = Maroon,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "\"Ada inisial AP di bagian bawah botol...\"",
                            color = TextPrimary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
