package com.volna.app.apex.feature.mybookings

import com.volna.app.apex.core.error.UiError
import com.volna.app.apex.core.mvi.UiEffect
import com.volna.app.apex.core.mvi.UiEvent
import com.volna.app.apex.core.mvi.UiState
import com.volna.app.apex.domain.model.Booking

data class MyBookingsState(
    val isLoading: Boolean = false,
    val items: List<Booking> = emptyList(),
    val error: UiError? = null,
) : UiState {
    val isEmpty: Boolean
        get() = !isLoading && error == null && items.isEmpty()
}

sealed interface MyBookingsEvent : UiEvent {
    data object ScreenOpened : MyBookingsEvent
    data object RetryClicked : MyBookingsEvent
    data class BookingClicked(val bookingId: String) : MyBookingsEvent
}

sealed interface MyBookingsEffect : UiEffect {
    data class NavigateToBookingDetails(val bookingId: String) : MyBookingsEffect
}
