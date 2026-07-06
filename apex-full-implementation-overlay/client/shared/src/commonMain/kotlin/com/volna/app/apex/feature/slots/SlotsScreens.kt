package com.volna.app.apex.feature.slots

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.volna.app.apex.design.ApexBottomBar
import com.volna.app.apex.design.ApexColors
import com.volna.app.apex.design.ApexErrorBlock
import com.volna.app.apex.design.ApexPrimaryButton
import com.volna.app.apex.design.ApexScreen
import com.volna.app.apex.design.ApexScreenWithBack
import com.volna.app.apex.design.ApexSecondaryButton
import com.volna.app.apex.design.ApexTab
import com.volna.app.apex.design.Chip
import com.volna.app.apex.design.InfoPanel
import com.volna.app.apex.design.KeyValueRow
import com.volna.app.apex.design.ShadowCard
import com.volna.app.apex.design.SlotStatusChip
import com.volna.app.apex.design.TrackIllustration
import com.volna.app.apex.domain.model.RideSlot
import com.volna.app.apex.domain.model.RideSlotStatus

@Composable
fun SlotsScreen(
    state: SlotsState,
    onEvent: (SlotsEvent) -> Unit,
    onMyBookings: () -> Unit = {},
    onProfile: () -> Unit = {},
) {
    ApexScreen(
        screenCode = if (state.isEmpty) "SCR-004" else "SCR-003",
        title = "Заезды",
        subtitle = "Ближайшие 7 дней",
        showBottomBar = true,
        selectedTab = ApexTab.Rides,
        onRides = { onEvent(SlotsEvent.RetryClicked) },
        onBookings = onMyBookings,
        onProfile = onProfile,
    ) {
        when {
            state.isLoading -> Box(Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ApexColors.Black)
            }

            state.error != null -> ApexErrorBlock(
                error = state.error,
                onRetry = { onEvent(SlotsEvent.RetryClicked) },
            )

            state.isEmpty -> EmptyScheduleDesign(
                onRefresh = { onEvent(SlotsEvent.RetryClicked) },
            )

            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Box(
                        modifier = Modifier
                            .clickable(onClick = onMyBookings)
                            .padding(horizontal = 18.dp, vertical = 8.dp),
                    ) {
                        Text("Мои брони", fontSize = 16.sp, color = ApexColors.TextPrimary)
                    }
                }
                LazyColumn(
                    modifier = Modifier.height(900.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    items(state.items) { slot ->
                        SlotListCard(
                            slot = slot,
                            onClick = { onEvent(SlotsEvent.SlotClicked(slot.id)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyScheduleDesign(onRefresh: () -> Unit) {
    Spacer(Modifier.height(150.dp))
    ShadowCard(
        modifier = Modifier.padding(horizontal = 84.dp),
    ) {
        TrackIllustration(compact = true)
    }
    Spacer(Modifier.height(56.dp))
    Text(
        text = "Пока нет расписания",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.headlineMedium,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
    Text(
        text = "На ближайшие дни слотов нет. Когда центр опубликует расписание, заезды появятся здесь.",
        modifier = Modifier.padding(horizontal = 36.dp),
        style = MaterialTheme.typography.bodyLarge,
    )
    Spacer(Modifier.height(80.dp))
    ApexPrimaryButton(
        text = "Обновить",
        onClick = onRefresh,
    )
}

@Composable
fun SlotListCard(
    slot: RideSlot,
    onClick: () -> Unit,
) {
    val muted = slot.status != RideSlotStatus.AVAILABLE
    ShadowCard(
        background = if (muted) ApexColors.SurfaceMuted else ApexColors.Surface,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        TrackIllustration(compact = true)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip(
                text = slot.rideLevel.name,
                color = if (muted) ApexColors.Border else ApexColors.Accent,
            )
            Chip(
                text = slot.trackConfiguration.name,
                color = if (muted) ApexColors.Border else ApexColors.Warning,
            )
        }
        Text(
            text = slot.startAt,
            color = if (muted) ApexColors.TextSecondary else ApexColors.TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text("${slot.durationMinutes} мин · ${slot.address}", style = MaterialTheme.typography.bodyLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val statusText = when (slot.status) {
                RideSlotStatus.AVAILABLE -> "Есть места"
                RideSlotStatus.NO_FREE_PLACES -> "Мест нет"
                RideSlotStatus.CANCELLED -> "Отменён"
                RideSlotStatus.UNKNOWN -> "Неизвестно"
            }
            Text(
                text = statusText,
                color = if (slot.status == RideSlotStatus.NO_FREE_PLACES) ApexColors.Error else ApexColors.TextPrimary,
                fontSize = 16.sp,
            )
            Text(
                text = "${slot.freePlaces} из ${slot.capacity} · ${slot.price.format()}",
                color = ApexColors.TextSecondary,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
fun SlotDetailsScreen(
    state: SlotDetailsState,
    onEvent: (SlotDetailsEvent) -> Unit,
) {
    ApexScreenWithBack(
        screenCode = "SCR-005",
        title = "Детали заезда",
        onBack = { onEvent(SlotDetailsEvent.BackClicked) },
    ) {
        when {
            state.isLoading -> Box(Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ApexColors.Black)
            }

            state.error != null -> ApexErrorBlock(
                error = state.error,
                onRetry = { onEvent(SlotDetailsEvent.RetryClicked) },
            )

            state.slot != null -> {
                val slot = state.slot
                TrackIllustration()
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SlotStatusChip(slot.status)
                    Chip(slot.rideLevel.name, color = ApexColors.Warning)
                }
                Spacer(Modifier.height(18.dp))
                Text(slot.startAt, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(20.dp))
                KeyValueRow("Длительность", "${slot.durationMinutes} минут")
                KeyValueRow("Трасса", slot.trackConfiguration.name)
                KeyValueRow("Свободные места", "${slot.freePlaces} из ${slot.capacity}")
                KeyValueRow("Цена", slot.price.format())
                KeyValueRow("Адрес", slot.address)
                Spacer(Modifier.height(42.dp))
                InfoPanel("Важно: приехать заранее не требуется. Центр подтвердит бронь вручную.", background = ApexColors.AccentSoft)
                Spacer(Modifier.height(160.dp))
                ApexPrimaryButton(
                    text = "Забронировать",
                    onClick = { onEvent(SlotDetailsEvent.BookClicked) },
                    enabled = slot.canBook,
                )
            }
        }
    }
}
