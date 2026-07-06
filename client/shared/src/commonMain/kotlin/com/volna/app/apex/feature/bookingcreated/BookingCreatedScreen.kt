package com.volna.app.apex.feature.bookingcreated

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.volna.app.apex.core.ui.ApexPrimaryButton
import com.volna.app.apex.core.ui.ApexScreen
import com.volna.app.apex.core.ui.ApexSecondaryButton
import com.volna.app.apex.core.ui.StatusBadge

@Composable
fun BookingCreatedScreen(
    bookingId: String,
    onOpenMyBookings: () -> Unit,
    onBackToSlots: () -> Unit,
) {
    ApexScreen(title = "Бронь создана") {
        StatusBadge("Ожидает подтверждения")
        Text("Ваша заявка на бронирование создана и ожидает ручного подтверждения администратором.")
        Text("Номер брони: $bookingId")
        ApexPrimaryButton(
            text = "Мои брони",
            onClick = onOpenMyBookings,
        )
        ApexSecondaryButton(
            text = "К списку заездов",
            onClick = onBackToSlots,
        )
    }
}
