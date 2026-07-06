package com.volna.app.apex.feature.mybookings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.volna.app.apex.core.ui.ApexErrorBlock
import com.volna.app.apex.core.ui.ApexScreen
import com.volna.app.apex.core.ui.EmptyState
import com.volna.app.apex.core.ui.StatusBadge
import com.volna.app.apex.core.ui.title
import com.volna.app.apex.domain.model.Booking

@Composable
fun MyBookingsScreen(
    state: MyBookingsState,
    onEvent: (MyBookingsEvent) -> Unit,
) {
    ApexScreen(title = "Мои брони") {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.error != null -> ApexErrorBlock(
                error = state.error,
                onRetry = { onEvent(MyBookingsEvent.RetryClicked) },
            )
            state.isEmpty -> EmptyState(
                title = "Броней пока нет",
                message = "Выберите доступный заезд и создайте бронь.",
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.items) { booking ->
                    BookingCard(
                        booking = booking,
                        onClick = { onEvent(MyBookingsEvent.BookingClicked(booking.id)) },
                    )
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(booking.slot?.startAt ?: "Дата уточняется", fontWeight = FontWeight.Bold)
            StatusBadge(booking.status.title())
            Text(booking.slot?.trackConfiguration?.name ?: "Трасса уточняется")
            Text(booking.slot?.address ?: "Адрес уточняется")
        }
    }
}
