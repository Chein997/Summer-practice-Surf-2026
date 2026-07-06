package com.volna.app.apex.feature.bookingdetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.volna.app.apex.design.ApexColors
import com.volna.app.apex.design.ApexErrorBlock
import com.volna.app.apex.design.ApexPrimaryButton
import com.volna.app.apex.design.ApexScreenWithBack
import com.volna.app.apex.design.BookingStatusChip
import com.volna.app.apex.design.InfoPanel
import com.volna.app.apex.design.KeyValueRow
import com.volna.app.apex.design.ShadowCard
import com.volna.app.apex.design.titleForDetails
import com.volna.app.apex.domain.model.BookingStatus

@Composable
fun BookingDetailsScreen(
    state: BookingDetailsState,
    onEvent: (BookingDetailsEvent) -> Unit,
) {
    ApexScreenWithBack(
        screenCode = "SCR-009",
        title = "Детали брони",
        onBack = { onEvent(BookingDetailsEvent.BackClicked) },
    ) {
        when {
            state.isLoading -> Box(Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ApexColors.Black)
            }

            state.error != null -> ApexErrorBlock(
                error = state.error,
                onRetry = { onEvent(BookingDetailsEvent.RetryClicked) },
            )

            state.booking != null -> {
                val booking = state.booking
                val slot = booking.slot

                BookingStatusChip(booking.status)
                Spacer(Modifier.height(22.dp))
                Text(slot?.startAt ?: "Дата уточняется", style = MaterialTheme.typography.headlineLarge)
                Text(
                    text = "${slot?.trackConfiguration?.name ?: "Трасса"} · ${slot?.rideLevel?.name ?: "уровень"}",
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(Modifier.height(80.dp))

                ShadowCard {
                    KeyValueRow("Статус", booking.status.titleForDetails())
                    KeyValueRow("Участник", "${booking.profile.fullName}, ${booking.profile.age} лет")
                    KeyValueRow("Телефон", booking.profile.phone)
                    KeyValueRow("Email", booking.profile.email)
                    KeyValueRow("Адрес", slot?.address ?: "Уточняется")
                    KeyValueRow("Цена", slot?.price?.format() ?: "Уточняется")
                }

                Spacer(Modifier.height(86.dp))
                InfoPanel("Отменить бронь можно не позднее чем за 1 час до старта.")

                if (booking.centerCancellation != null) {
                    Spacer(Modifier.height(18.dp))
                    InfoPanel(
                        text = booking.centerCancellation.reasonText ?: booking.centerCancellation.reasonType,
                        background = ApexColors.SurfaceMuted,
                    )
                }

                Spacer(Modifier.height(120.dp))

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
                }
            }
        }
    }
}
