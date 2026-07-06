package com.volna.app.apex.feature.cancelbooking

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.volna.app.apex.core.ui.ApexErrorBlock
import com.volna.app.apex.core.ui.ApexPrimaryButton
import com.volna.app.apex.core.ui.ApexScreen
import com.volna.app.apex.core.ui.ApexSecondaryButton
import com.volna.app.apex.core.ui.StatusBadge

@Composable
fun CancelBookingScreen(
    state: CancelBookingState,
    onEvent: (CancelBookingEvent) -> Unit,
) {
    ApexScreen(title = "Отменить бронь?") {
        Text("После отмены место станет доступно другим клиентам.")
        Text("Отменить можно только если до начала заезда осталось больше 1 часа.")
        if (state.error != null) {
            ApexErrorBlock(error = state.error)
        }
        ApexPrimaryButton(
            text = "Да, отменить бронь",
            onClick = { onEvent(CancelBookingEvent.ConfirmClicked) },
            enabled = !state.isLoading,
            isLoading = state.isLoading,
        )
        ApexSecondaryButton(
            text = "Назад",
            onClick = { onEvent(CancelBookingEvent.BackClicked) },
            enabled = !state.isLoading,
        )
    }
}

@Composable
fun BookingCancelledScreen(
    bookingId: String,
    onOpenMyBookings: () -> Unit,
    onBackToSlots: () -> Unit,
) {
    ApexScreen(title = "Бронь отменена") {
        StatusBadge("Отменена вами")
        Text("Бронь $bookingId отменена. Место освобождено.")
        ApexPrimaryButton("Мои брони", onOpenMyBookings)
        ApexSecondaryButton("К списку заездов", onBackToSlots)
    }
}
