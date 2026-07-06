package com.volna.app.apex.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.volna.app.apex.design.ApexColors
import com.volna.app.apex.design.ApexErrorBlock
import com.volna.app.apex.design.ApexInput
import com.volna.app.apex.design.ApexPrimaryButton
import com.volna.app.apex.design.ApexScreen
import com.volna.app.apex.design.ApexScreenWithBack
import com.volna.app.apex.design.ApexSecondaryButton
import com.volna.app.apex.design.ShadowCard

@Composable
fun PhoneInputScreen(
    state: PhoneInputState,
    onEvent: (PhoneInputEvent) -> Unit,
) {
    ApexScreen(screenCode = "SCR-001", title = "") {
        Spacer(Modifier.height(34.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 150.dp, height = 74.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(ApexColors.Black),
                contentAlignment = Alignment.Center,
            ) {
                Text("A", color = ApexColors.Accent, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(16.dp))
            Text("АПЕКС", style = MaterialTheme.typography.headlineMedium)
            Text("картинг-центр", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(Modifier.height(136.dp))

        ShadowCard {
            Text(
                text = "Вход в Апекс",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Введите номер телефона — отправим SMS-код для подтверждения.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(10.dp))
            ApexInput(
                value = state.phone,
                onValueChange = { onEvent(PhoneInputEvent.PhoneChanged(it)) },
                label = "Телефон",
                placeholder = "+7 (___) ___-__-__",
                enabled = !state.isLoading,
                error = state.phoneError,
            )
            Text(
                text = "Без авторизации нельзя создать бронь на заезд.",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (state.error != null) {
                ApexErrorBlock(error = state.error, onRetry = { onEvent(PhoneInputEvent.RetryClicked) })
            }
            ApexPrimaryButton(
                text = "Получить код",
                onClick = { onEvent(PhoneInputEvent.ContinueClicked) },
                enabled = state.canContinue,
                loading = state.isLoading,
            )
        }
    }
}

@Composable
fun SmsCodeScreen(
    state: SmsCodeState,
    onEvent: (SmsCodeEvent) -> Unit,
) {
    ApexScreenWithBack(
        screenCode = "SCR-002",
        title = "SMS-код",
        onBack = { onEvent(SmsCodeEvent.BackClicked) },
    ) {
        Spacer(Modifier.height(128.dp))
        ShadowCard {
            Text(
                text = "Подтверждение",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text("Мы отправили код на ${state.phone}", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(42.dp))
            Text("Код из SMS", style = MaterialTheme.typography.bodyLarge)

            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp),
            ) {
                repeat(4) { index ->
                    val char = state.code.getOrNull(index)?.toString().orEmpty()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(2.dp, ApexColors.Black, RoundedCornerShape(20.dp))
                            .background(ApexColors.Background),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(char, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            androidx.compose.material3.OutlinedTextField(
                value = state.code,
                onValueChange = { onEvent(SmsCodeEvent.CodeChanged(it.filter(Char::isDigit))) },
                modifier = Modifier.height(1.dp),
                singleLine = true,
            )

            Text(
                text = "Отправить код повторно (00:37)",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )

            if (state.error != null) {
                ApexErrorBlock(error = state.error)
            }

            ApexPrimaryButton(
                text = "Подтвердить",
                onClick = { onEvent(SmsCodeEvent.SubmitClicked) },
                enabled = state.canSubmit,
                loading = state.isLoading,
            )
            ApexSecondaryButton(
                text = "Отправить код ещё раз",
                onClick = { onEvent(SmsCodeEvent.ResendClicked) },
                enabled = !state.isLoading,
            )
        }
    }
}
