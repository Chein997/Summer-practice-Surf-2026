package com.volna.app.apex.feature.cancelbooking

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.volna.app.apex.design.ApexColors
import com.volna.app.apex.design.ApexErrorBlock
import com.volna.app.apex.design.ApexPrimaryButton
import com.volna.app.apex.design.ApexScreen
import com.volna.app.apex.design.ApexScreenWithBack
import com.volna.app.apex.design.ApexSecondaryButton
import com.volna.app.apex.design.InfoPanel
import com.volna.app.apex.design.ShadowCard
import com.volna.app.apex.design.SuccessCircle

@Composable
fun CancelBookingScreen(
    state: CancelBookingState,
    onEvent: (CancelBookingEvent) -> Unit,
) {
    ApexScreenWithBack(
        screenCode = "SCR-010",
        title = "Отмена брони",
        onBack = { onEvent(CancelBookingEvent.BackClicked) },
    ) {
        Spacer(Modifier.height(280.dp))
        ShadowCard {
            Text(
                text = "Отменить бронь?",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "После отмены место вернётся в расписание, а заявка перейдёт в статус «Отменена клиентом».",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(48.dp))
            InfoPanel(
                text = "Сегодня, 18:30\nТрасса А · 1 место",
                background = ApexColors.SurfaceMuted,
            )
        }

        if (state.error != null) {
            Spacer(Modifier.height(18.dp))
            ApexErrorBlock(error = state.error)
        }

        Spacer(Modifier.height(56.dp))
        ApexPrimaryButton(
            text = "Да, отменить",
            onClick = { onEvent(CancelBookingEvent.ConfirmClicked) },
            enabled = !state.isLoading,
            loading = state.isLoading,
        )
        Spacer(Modifier.height(16.dp))
        ApexSecondaryButton(
            text = "Оставить бронь",
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
    ApexScreen(screenCode = "SCR-011", title = "Бронь отменена") {
        Spacer(Modifier.height(180.dp))
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        ) {
            SuccessCircle(symbol = "×", background = ApexColors.SurfaceMuted)
            Spacer(Modifier.height(58.dp))
            Text("Бронь отменена", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))
            Text(
                text = "Статус заявки изменён. Вы можете выбрать другой доступный заезд.",
                modifier = Modifier.fillMaxWidth(0.8f),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Spacer(Modifier.height(150.dp))
        ApexPrimaryButton(
            text = "Выбрать другой заезд",
            onClick = onBackToSlots,
        )
        Spacer(Modifier.height(16.dp))
        ApexSecondaryButton(
            text = "Мои брони",
            onClick = onOpenMyBookings,
        )
    }
}
