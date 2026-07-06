package com.volna.app.apex.feature.bookingcreated

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.volna.app.apex.design.ApexColors
import com.volna.app.apex.design.ApexPrimaryButton
import com.volna.app.apex.design.ApexScreen
import com.volna.app.apex.design.ApexSecondaryButton
import com.volna.app.apex.design.SlotSummaryCard
import com.volna.app.apex.design.SuccessCircle
import com.volna.app.apex.domain.model.RideSlot

@Composable
fun BookingCreatedScreen(
    bookingId: String,
    slot: RideSlot? = null,
    onOpenMyBookings: () -> Unit,
    onBackToSlots: () -> Unit,
) {
    ApexScreen(screenCode = "SCR-007", title = "Заявка создана") {
        Spacer(Modifier.height(120.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(26.dp),
        ) {
            SuccessCircle(symbol = "✓", background = ApexColors.AccentSoft)
            Text(
                text = "Ожидает подтверждения",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Мы отправили заявку в картинг-центр. Статус изменится после ручного подтверждения.",
                modifier = Modifier.fillMaxWidth(0.78f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Start,
            )
        }

        Spacer(Modifier.height(82.dp))

        if (slot != null) {
            SlotSummaryCard(slot)
        } else {
            com.volna.app.apex.design.ShadowCard {
                Text("Заезд", color = ApexColors.TextSecondary)
                Text("Сегодня, 18:30 · Трасса А", style = MaterialTheme.typography.titleLarge)
                Text("1 место · 1 800 ₽", color = ApexColors.TextPrimary)
            }
        }

        Spacer(Modifier.height(92.dp))

        ApexPrimaryButton(
            text = "Перейти в мои брони",
            onClick = onOpenMyBookings,
        )
        Spacer(Modifier.height(14.dp))
        ApexSecondaryButton(
            text = "К списку заездов",
            onClick = onBackToSlots,
        )
    }
}
