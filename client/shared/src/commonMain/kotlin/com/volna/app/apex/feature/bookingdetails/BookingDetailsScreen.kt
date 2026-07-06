package com.volna.app.apex.feature.bookingdetails

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.volna.app.apex.core.ui.ApexErrorBlock
import com.volna.app.apex.core.ui.ApexPrimaryButton
import com.volna.app.apex.core.ui.ApexScreen
import com.volna.app.apex.core.ui.ApexSecondaryButton
import com.volna.app.apex.core.ui.KeyValue
import com.volna.app.apex.core.ui.StatusBadge
import com.volna.app.apex.core.ui.title
import com.volna.app.apex.domain.model.BookingStatus

@Composable
fun BookingDetailsScreen(
    state: BookingDetailsState,
    onEvent: (BookingDetailsEvent) -> Unit,
) {
    ApexScreen(title = "Детали брони") {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.error != null -> ApexErrorBlock(
                error = state.error,
                onRetry = { onEvent(BookingDetailsEvent.RetryClicked) },
            )
            state.booking != null -> {
                val booking = state.booking
                val slot = booking.slot

                StatusBadge(booking.status.title())
                KeyValue("Номер брони", booking.id)
                KeyValue("Создана", booking.createdAt)
                KeyValue("Клиент", booking.profile.fullName)
                KeyValue("Телефон", booking.profile.phone)
                KeyValue("Email", booking.profile.email)

                if (slot != null) {
                    KeyValue("Дата и время", slot.startAt)
                    KeyValue("Трасса", slot.trackConfiguration.name)
                    KeyValue("Уровень", slot.rideLevel.name)
                    KeyValue("Маршал", slot.marshal?.name ?: "Не назначен")
                    KeyValue("Адрес", slot.address)
                    KeyValue("Место встречи", slot.meetingPoint)

                    Text("Правила безопасности", fontWeight = FontWeight.Bold)
                    Text(slot.safetyRules)

                    Text("Условия отмены", fontWeight = FontWeight.Bold)
                    Text(slot.cancellationTerms)
                }

                if (booking.centerCancellation != null) {
                    Text("Причина отмены центром", fontWeight = FontWeight.Bold)
                    Text(booking.centerCancellation.reasonText ?: booking.centerCancellation.reasonType)
                }

                if (booking.status == BookingStatus.CANCELLED_BY_CENTER) {
                    ApexPrimaryButton(
                        text = "Выбрать другой заезд",
                        onClick = { onEvent(BookingDetailsEvent.ChooseAnotherRideClicked) },
                    )
                } else if (booking.canCancel) {
                    ApexPrimaryButton(
                        text = "Отменить бронь",
                        onClick = { onEvent(BookingDetailsEvent.CancelClicked) },
                    )
                } else {
                    Text("Отмена этой брони недоступна.")
                }

                ApexSecondaryButton(
                    text = "Назад",
                    onClick = { onEvent(BookingDetailsEvent.BackClicked) },
                )
            }
        }
    }
}
