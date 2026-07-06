package com.volna.app.apex.feature.cancelbooking

import com.volna.app.apex.core.error.UiError
import com.volna.app.apex.core.mvi.UiEffect
import com.volna.app.apex.core.mvi.UiEvent
import com.volna.app.apex.core.mvi.UiState

data class CancelBookingState(
    val bookingId: String,
    val isLoading: Boolean = false,
    val error: UiError? = null,
) : UiState

sealed interface CancelBookingEvent : UiEvent {
    data object ConfirmClicked : CancelBookingEvent
    data object BackClicked : CancelBookingEvent
}

sealed interface CancelBookingEffect : UiEffect {
    data object NavigateBack : CancelBookingEffect
    data class NavigateToCancelled(val bookingId: String) : CancelBookingEffect
    data object NavigateToSlots : CancelBookingEffect
}
