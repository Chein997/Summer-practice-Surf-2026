package com.volna.app.apex.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.volna.app.apex.core.ui.ApexErrorBlock
import com.volna.app.apex.core.ui.ApexPrimaryButton
import com.volna.app.apex.core.ui.ApexScreen
import com.volna.app.apex.core.ui.ApexSecondaryButton
import com.volna.app.apex.core.ui.ApexTextField

@Composable
fun PhoneInputScreen(
    state: PhoneInputState,
    onEvent: (PhoneInputEvent) -> Unit,
) {
    ApexScreen(title = "Вход") {
        Text("Введите номер телефона, чтобы получить SMS-код.")
        ApexTextField(
            value = state.phone,
            onValueChange = { onEvent(PhoneInputEvent.PhoneChanged(it)) },
            label = "Телефон",
            error = state.phoneError,
            enabled = !state.isLoading,
        )
        if (state.error != null) {
            ApexErrorBlock(
                error = state.error,
                onRetry = { onEvent(PhoneInputEvent.RetryClicked) },
            )
        }
        ApexPrimaryButton(
            text = "Получить код",
            onClick = { onEvent(PhoneInputEvent.ContinueClicked) },
            enabled = state.canContinue,
            isLoading = state.isLoading,
        )
    }
}

@Composable
fun SmsCodeScreen(
    state: SmsCodeState,
    onEvent: (SmsCodeEvent) -> Unit,
) {
    ApexScreen(title = "SMS-код") {
        Text("Мы отправили код на номер ${state.phone}. В fake-режиме используйте код 1111.")
        ApexTextField(
            value = state.code,
            onValueChange = { onEvent(SmsCodeEvent.CodeChanged(it)) },
            label = "Код из SMS",
            error = state.codeError,
            enabled = !state.isLoading,
        )
        if (state.error != null) {
            ApexErrorBlock(error = state.error)
        }
        ApexPrimaryButton(
            text = "Войти",
            onClick = { onEvent(SmsCodeEvent.SubmitClicked) },
            enabled = state.canSubmit,
            isLoading = state.isLoading,
        )
        ApexSecondaryButton(
            text = "Отправить код ещё раз",
            onClick = { onEvent(SmsCodeEvent.ResendClicked) },
            enabled = !state.isLoading,
        )
        ApexSecondaryButton(
            text = "Назад",
            onClick = { onEvent(SmsCodeEvent.BackClicked) },
            enabled = !state.isLoading,
        )
    }
}
