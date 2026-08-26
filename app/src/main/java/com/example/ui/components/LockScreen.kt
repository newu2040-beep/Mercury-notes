package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.R
import com.example.ui.theme.MercuryPink
import com.example.ui.theme.MercuryTheme
import com.example.ui.theme.MercuryViolet
import com.example.util.BiometricAuthHelper

@Composable
fun LockScreen(
    onUnlockSuccess: () -> Unit,
    onVerifyPin: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val glass = MercuryTheme.glass
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    fun tryNativeBiometric() {
        val activity = context as? FragmentActivity
        if (activity != null && BiometricAuthHelper.isBiometricAvailable(context)) {
            BiometricAuthHelper.promptBiometricUnlock(
                activity = activity,
                title = "Unlock Mercurynotes",
                subtitle = "Use Face Recognition, Fingerprint or Device PIN",
                onSuccess = {
                    onUnlockSuccess()
                },
                onError = { err ->
                    // User canceled or failed; fallback to manual PIN
                }
            )
        }
    }

    // Auto-prompt hardware biometrics on screen load
    LaunchedEffect(Unit) {
        tryNativeBiometric()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(glass.canvasBackground)
            .statusBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.mercurynotes_icon),
                contentDescription = "Mercurynotes",
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Mercurynotes",
                style = MaterialTheme.typography.titleLarge,
                color = glass.textPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isError) "Incorrect PIN, please try again" else "Unlock with Face ID, Fingerprint, or PIN",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) Color(0xFFEF4444) else glass.textSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PIN Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) MercuryViolet else glass.searchBarBackground
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Numeric Keypad
            val buttons = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                buttons.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        row.forEach { key ->
                            KeypadButton(
                                text = key,
                                onClick = {
                                    when (key) {
                                        "DEL" -> {
                                            if (enteredPin.isNotEmpty()) {
                                                enteredPin = enteredPin.dropLast(1)
                                                isError = false
                                            }
                                        }
                                        "BIO" -> {
                                            tryNativeBiometric()
                                        }
                                        else -> {
                                            if (enteredPin.length < 4) {
                                                enteredPin += key
                                                isError = false
                                                if (enteredPin.length == 4) {
                                                    if (onVerifyPin(enteredPin)) {
                                                        onUnlockSuccess()
                                                    } else {
                                                        isError = true
                                                        enteredPin = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit
) {
    val glass = MercuryTheme.glass

    GlassCard(
        modifier = Modifier.size(68.dp),
        shape = CircleShape,
        backgroundColor = glass.cardBackgroundElevated,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (text) {
                "DEL" -> {
                    Icon(
                        imageVector = Icons.Default.Backspace,
                        contentDescription = "Delete",
                        tint = glass.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                "BIO" -> {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometrics",
                        tint = MercuryViolet,
                        modifier = Modifier.size(28.dp)
                    )
                }
                else -> {
                    Text(
                        text = text,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = glass.textPrimary
                    )
                }
            }
        }
    }
}
