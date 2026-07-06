package com.volna.app.apex.feature.slots

import com.volna.app.apex.core.error.UiError
import com.volna.app.apex.core.mvi.UiEffect
import com.volna.app.apex.core.mvi.UiEvent
import com.volna.app.apex.core.mvi.UiState
import com.volna.app.apex.domain.model.RideSlot

data class SlotsState(
    val isLoading: Boolean = false,
    val items: List<RideSlot> = emptyList(),
    val error: UiError? = null,
) : UiState {
    val isEmpty: Boolean
        get() = !isLoading && error == null && items.isEmpty()
}

sealed interface SlotsEvent : UiEvent {
    data object ScreenOpened : SlotsEvent
    data object RetryClicked : SlotsEvent
    data class SlotClicked(val slotId: String) : SlotsEvent
}

sealed interface SlotsEffect : UiEffect {
    data class NavigateToSlotDetails(val slotId: String) : SlotsEffect
}

data class SlotDetailsState(
    val slotId: String,
    val isLoading: Boolean = false,
    val slot: RideSlot? = null,
    val error: UiError? = null,
) : UiState

sealed interface SlotDetailsEvent : UiEvent {
    data object ScreenOpened : SlotDetailsEvent
    data object RetryClicked : SlotDetailsEvent
    data object BackClicked : SlotDetailsEvent
    data object BookClicked : SlotDetailsEvent
}

sealed interface SlotDetailsEffect : UiEffect {
    data object NavigateBack : SlotDetailsEffect
    data class NavigateToBookingForm(val slotId: String) : SlotDetailsEffect
}
