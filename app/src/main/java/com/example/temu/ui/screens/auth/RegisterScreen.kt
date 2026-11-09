
package com.example.temu.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.temu.ui.theme.*

@Composable
fun RegisterScreen(
    sibuk: Boolean,
    pesan: String,
    pendaftaranBerhasil: Boolean,
    onRegister: (String, String, String, String) -> Unit,
    onLoginClick: () -> Unit
) {
    var namaLengkap by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var sandi by remember { mutableStateOf("") }
    var konfirmasiSandi by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(16.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    1.dp,
                    BorderColor,
                    RoundedCornerShape(28.dp)
                ),
            shape = RoundedCornerShape(28.dp),
            color = WarmWhite
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(64.dp))

                    Text(
                        text = "Buat akun",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = "Satu akun untuk melapor kehilangan, menerbitkan temuan, dan mengajukan klaim.",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(25.dp))

                    // Nama lengkap
                    AuthRegisterField(
                        label = "Nama lengkap",
                        value = namaLengkap,
                        onValueChange = { namaLengkap = it },
                        placeholder = "Contoh: Ayu Pratiwi"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Email
                    AuthRegisterField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "nama@email.com",
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Kata sandi
                    AuthRegisterField(
                        label = "Kata sandi",
                        value = sandi,
                        onValueChange = { sandi = it },
                        placeholder = "Buat kata sandi",
                        keyboardType = KeyboardType.Password,
                        password = true
                    )

                    Text(
                        text = "Minimal 8 karakter.",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Konfirmasi kata sandi
                    AuthRegisterField(
                        label = "Konfirmasi kata sandi",
                        value = konfirmasiSandi,
                        onValueChange = { konfirmasiSandi = it },
                        placeholder = "Ulangi kata sandi",
                        keyboardType = KeyboardType.Password,
                        password = true
                    )

                    if (pesan.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = pesan,
                            color = if (pendaftaranBerhasil) {
                                SuccessColor
                            } else {
                                ErrorColor
                            },
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            onRegister(
                                namaLengkap,
                                email.trim(),
                                sandi,
                                konfirmasiSandi
                            )
                        },
                        enabled = !sibuk && !pendaftaranBerhasil,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Maroon,
                            contentColor = TextWhite
                        )
                    ) {
                        if (sibuk) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(21.dp),
                                color = TextWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Daftar", fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Sudah punya akun?",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )

                        TextButton(onClick = onLoginClick) {
                            Text(
                                "Masuk",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthRegisterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    password: Boolean = false
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(7.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    placeholder,
                    color = TextSecondary
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(11.dp),
            visualTransformation = if (password) {
                PasswordVisualTransformation()
            } else {
                androidx.compose.ui.text.input.VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            colors = authFieldColors()
        )
    }
}
