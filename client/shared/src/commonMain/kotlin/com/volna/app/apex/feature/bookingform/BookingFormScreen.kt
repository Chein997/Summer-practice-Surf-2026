package com.volna.app.apex.feature.bookingform

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.volna.app.apex.core.ui.ApexErrorBlock
import com.volna.app.apex.core.ui.ApexPrimaryButton
import com.volna.app.apex.core.ui.ApexScreen
import com.volna.app.apex.core.ui.ApexSecondaryButton
import com.volna.app.apex.core.ui.ApexTextField

@Composable
fun BookingFormScreen(
    state: BookingFormState,
    onEvent: (BookingFormEvent) -> Unit,
) {
    ApexScreen(title = "Бронирование") {
        ApexTextField(
            value = state.fullName,
            onValueChange = { onEvent(BookingFormEvent.FullNameChanged(it)) },
            label = "Имя",
            error = state.fieldErrors["fullName"],
            enabled = !state.isLoading,
        )
        ApexTextField(
            value = state.phone,
            onValueChange = { onEvent(BookingFormEvent.PhoneChanged(it)) },
            label = "Телефон",
            error = state.fieldErrors["phone"],
            enabled = !state.isLoading,
        )
        ApexTextField(
            value = state.email,
            onValueChange = { onEvent(BookingFormEvent.EmailChanged(it)) },
            label = "Email",
            error = state.fieldErrors["email"],
            enabled = !state.isLoading,
        )
        ApexTextField(
            value = state.age,
            onValueChange = { onEvent(BookingFormEvent.AgeChanged(it)) },
            label = "Возраст",
            error = state.fieldErrors["age"],
            enabled = !state.isLoading,
        )

        Row {
            Checkbox(
                checked = state.safetyRulesAccepted,
                onCheckedChange = { onEvent(BookingFormEvent.SafetyRulesToggled) },
                enabled = !state.isLoading,
            )
            Spacer(Modifier.width(8.dp))
            Text("Я согласен с правилами безопасности")
        }

        Row {
            Checkbox(
                checked = state.parentalConsentAccepted,
                onCheckedChange = { onEvent(BookingFormEvent.ParentalConsentToggled) },
                enabled = !state.isLoading,
            )
            Spacer(Modifier.width(8.dp))
            Text("Есть согласие родителя или законного представителя, если мне меньше 18 лет")
        }

        if (state.error != null) {
            ApexErrorBlock(
                error = state.error,
                onPrimaryAction = { onEvent(BookingFormEvent.BackClicked) },
            )
        }

        ApexPrimaryButton(
            text = "Создать бронь",
            onClick = { onEvent(BookingFormEvent.SubmitClicked) },
            enabled = state.canSubmit,
            isLoading = state.isLoading,
        )
        ApexSecondaryButton(
            text = "Назад",
            onClick = { onEvent(BookingFormEvent.BackClicked) },
            enabled = !state.isLoading,
        )
    }
}
