package com.volna.app.apex.feature.mybookings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.volna.app.apex.design.ApexScreen
import com.volna.app.apex.design.ApexTab
import com.volna.app.apex.design.BookingStatusChip
import com.volna.app.apex.design.ShadowCard
import com.volna.app.apex.domain.model.Booking

@Composable
fun MyBookingsScreen(
    state: MyBookingsState,
    onEvent: (MyBookingsEvent) -> Unit,
    onRides: () -> Unit = {},
    onProfile: () -> Unit = {},
) {
    ApexScreen(
        screenCode = "SCR-008",
        title = "Мои брони",
        showBottomBar = true,
        selectedTab = ApexTab.Bookings,
        onRides = onRides,
        onBookings = { onEvent(MyBookingsEvent.RetryClicked) },
        onProfile = onProfile,
    ) {
        when {
            state.isLoading -> androidx.compose.foundation.layout.Box(
                Modifier.fillMaxWidth().height(400.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ApexColors.Black)
            }

            state.error != null -> ApexErrorBlock(
                error = state.error,
                onRetry = { onEvent(MyBookingsEvent.RetryClicked) },
            )

            state.isEmpty -> Text(
                "Броней пока нет. Выберите доступный заезд и создайте заявку.",
                style = MaterialTheme.typography.bodyLarge,
            )

            else -> {
                Spacer(Modifier.height(22.dp))
                LazyColumn(
                    modifier = Modifier.height(900.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    items(state.items) { booking ->
                        BookingListCard(
                            booking = booking,
                            onClick = { onEvent(MyBookingsEvent.BookingClicked(booking.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingListCard(
    booking: Booking,
    onClick: () -> Unit,
) {
    ShadowCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                BookingStatusChip(booking.status)
                Text(
                    text = booking.slot?.startAt ?: "Дата уточняется",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ApexColors.TextPrimary,
                )
                Text(
                    text = "${booking.slot?.trackConfiguration?.name ?: "Трасса"} · 1 место",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Text("›", color = ApexColors.TextSecondary, fontSize = 42.sp, fontWeight = FontWeight.Bold)
        }
    }
}
