
package com.example.temu.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temu.ui.theme.*

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite),
        contentAlignment = Alignment.Center
    ) {
        // Lingkaran dekorasi bagian atas
        Box(
            modifier = Modifier
                .size(230.dp)
                .offset(x = 115.dp, y = (-320).dp)
                .background(SoftPink.copy(alpha = 0.7f), CircleShape)
        )

        // Lingkaran dekorasi bagian bawah
        Box(
            modifier = Modifier
                .size(190.dp)
                .offset(x = (-145).dp, y = 340.dp)
                .background(SoftPink.copy(alpha = 0.7f), CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TU",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Maroon
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TemUBarang",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Temukan kembali barangmu.",
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(90.dp))

            CircularProgressIndicator(
                modifier = Modifier
                    .size(22.dp)
                    .alpha(0.7f),
                color = Maroon,
                strokeWidth = 2.dp
            )
        }
    }
}
