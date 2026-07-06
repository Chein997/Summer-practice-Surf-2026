package com.volna.app.apex.feature.bookingform

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.volna.app.apex.design.ApexCheckRow
import com.volna.app.apex.design.ApexErrorBlock
import com.volna.app.apex.design.ApexInput
import com.volna.app.apex.design.ApexPrimaryButton
import com.volna.app.apex.design.ApexScreenWithBack
import com.volna.app.apex.design.ShadowCard

@Composable
fun BookingFormScreen(
    state: BookingFormState,
    onEvent: (BookingFormEvent) -> Unit,
) {
    ApexScreenWithBack(
        screenCode = "SCR-006",
        title = "Бронирование",
        onBack = { onEvent(BookingFormEvent.BackClicked) },
    ) {
        ShadowCard {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Сегодня, 18:30 · Трасса А",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "1 800 ₽",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Text("1 место · центр подтвердит бронь вручную", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(Modifier.height(28.dp))

        ApexInput(
            value = state.fullName,
            onValueChange = { onEvent(BookingFormEvent.FullNameChanged(it)) },
            label = "Имя",
            placeholder = "Алексей",
            error = state.fieldErrors["fullName"],
            enabled = !state.isLoading,
        )
        ApexInput(
            value = state.phone,
            onValueChange = { onEvent(BookingFormEvent.PhoneChanged(it)) },
            label = "Телефон",
            placeholder = "+7 900 123-45-67",
            error = state.fieldErrors["phone"],
            enabled = !state.isLoading,
        )
        ApexInput(
            value = state.email,
            onValueChange = { onEvent(BookingFormEvent.EmailChanged(it)) },
            label = "Email",
            placeholder = "name@example.com",
            error = state.fieldErrors["email"],
            enabled = !state.isLoading,
        )
        ApexInput(
            value = state.age,
            onValueChange = { onEvent(BookingFormEvent.AgeChanged(it)) },
            label = "Возраст",
            placeholder = "17",
            error = state.fieldErrors["age"],
            enabled = !state.isLoading,
        )

        ApexCheckRow(
            checked = state.safetyRulesAccepted,
            onToggle = { onEvent(BookingFormEvent.SafetyRulesToggled) },
            text = "Я согласен с правилами безопасности",
            enabled = !state.isLoading,
        )

        ApexCheckRow(
            checked = state.parentalConsentAccepted,
            onToggle = { onEvent(BookingFormEvent.ParentalConsentToggled) },
            text = "Есть согласие родителя или законного представителя",
            enabled = !state.isLoading,
        )

        Spacer(Modifier.height(18.dp))
        Text("Минимальный возраст участия — 16 лет.", style = MaterialTheme.typography.bodyMedium)

        if (state.error != null) {
            ApexErrorBlock(
                error = state.error,
                onPrimaryAction = { onEvent(BookingFormEvent.BackClicked) },
            )
        }

        Spacer(Modifier.height(180.dp))
        ApexPrimaryButton(
            text = "Отправить заявку",
            onClick = { onEvent(BookingFormEvent.SubmitClicked) },
            enabled = state.canSubmit,
            loading = state.isLoading,
        )
    }
}
