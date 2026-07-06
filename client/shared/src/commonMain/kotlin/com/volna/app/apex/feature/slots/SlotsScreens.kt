package com.volna.app.apex.feature.slots

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.volna.app.apex.core.ui.ApexErrorBlock
import com.volna.app.apex.core.ui.ApexPrimaryButton
import com.volna.app.apex.core.ui.ApexScreen
import com.volna.app.apex.core.ui.ApexSecondaryButton
import com.volna.app.apex.core.ui.EmptyState
import com.volna.app.apex.core.ui.KeyValue
import com.volna.app.apex.core.ui.StatusBadge
import com.volna.app.apex.core.ui.title
import com.volna.app.apex.domain.model.RideSlot

@Composable
fun SlotsScreen(
    state: SlotsState,
    onEvent: (SlotsEvent) -> Unit,
) {
    ApexScreen(title = "Доступные заезды") {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.error != null -> ApexErrorBlock(
                error = state.error,
                onRetry = { onEvent(SlotsEvent.RetryClicked) },
            )
            state.isEmpty -> EmptyState(
                title = "Пока нет доступных заездов",
                message = "Расписание на ближайшие дни ещё не опубликовано.",
                actionTitle = "Обновить",
                onAction = { onEvent(SlotsEvent.RetryClicked) },
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.items) { slot ->
                    SlotCard(
                        slot = slot,
                        onClick = { onEvent(SlotsEvent.SlotClicked(slot.id)) },
                    )
                }
            }
        }
    }
}

@Composable
fun SlotCard(
    slot: RideSlot,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(slot.startAt, fontWeight = FontWeight.Bold)
                StatusBadge(slot.status.title())
            }
            Text("${slot.trackConfiguration.name} · ${slot.rideLevel.name}")
            Text("Свободно мест: ${slot.freePlaces} из ${slot.capacity}")
            Text("Цена: ${slot.price.format()}")
        }
    }
}

@Composable
fun SlotDetailsScreen(
    state: SlotDetailsState,
    onEvent: (SlotDetailsEvent) -> Unit,
) {
    ApexScreen(title = "Детали заезда") {
        when {
            state.isLoading -> CircularProgressIndicator()
            state.error != null -> ApexErrorBlock(
                error = state.error,
                onRetry = { onEvent(SlotDetailsEvent.RetryClicked) },
            )
            state.slot != null -> {
                val slot = state.slot
                StatusBadge(slot.status.title())
                KeyValue("Дата и время", slot.startAt)
                KeyValue("Длительность", "${slot.durationMinutes} мин")
                KeyValue("Трасса", slot.trackConfiguration.name)
                KeyValue("Уровень", slot.rideLevel.name)
                KeyValue("Маршал", slot.marshal?.name ?: "Не назначен")
                KeyValue("Свободные места", "${slot.freePlaces} из ${slot.capacity}")
                KeyValue("Цена", slot.price.format())
                KeyValue("Адрес", slot.address)
                KeyValue("Место встречи", slot.meetingPoint)

                Text("Правила безопасности", fontWeight = FontWeight.Bold)
                Text(slot.safetyRules)

                Text("Условия отмены", fontWeight = FontWeight.Bold)
                Text(slot.cancellationTerms)

                if (slot.centerCancellation != null) {
                    Text("Причина отмены", fontWeight = FontWeight.Bold)
                    Text(slot.centerCancellation.reasonText ?: slot.centerCancellation.reasonType)
                }

                ApexPrimaryButton(
                    text = "Забронировать",
                    onClick = { onEvent(SlotDetailsEvent.BookClicked) },
                    enabled = slot.canBook,
                )

                if (!slot.canBook) {
                    Text(
                        text = "Этот заезд сейчас недоступен для бронирования.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                ApexSecondaryButton(
                    text = "Назад",
                    onClick = { onEvent(SlotDetailsEvent.BackClicked) },
                )
            }
        }
    }
}
