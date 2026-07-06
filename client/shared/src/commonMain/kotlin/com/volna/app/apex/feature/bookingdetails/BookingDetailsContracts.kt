package com.volna.app.apex.feature.bookingdetails

import com.volna.app.apex.core.error.UiError
import com.volna.app.apex.core.mvi.UiEffect
import com.volna.app.apex.core.mvi.UiEvent
import com.volna.app.apex.core.mvi.UiState
import com.volna.app.apex.domain.model.Booking

data class BookingDetailsState(
    val bookingId: String,
    val isLoading: Boolean = false,
    val booking: Booking? = null,
    val error: UiError? = null,
    val isCancelling: Boolean = false,
) : UiState

sealed interface BookingDetailsEvent : UiEvent {
    data object ScreenOpened : BookingDetailsEvent
    data object RetryClicked : BookingDetailsEvent
    data object BackClicked : BookingDetailsEvent
    data object CancelClicked : BookingDetailsEvent
    data object ChooseAnotherRideClicked : BookingDetailsEvent
}

sealed interface BookingDetailsEffect : UiEffect {
    data object NavigateBack : BookingDetailsEffect
    data class NavigateToCancelConfirmation(val bookingId: String) : BookingDetailsEffect
    data object NavigateToSlots : BookingDetailsEffect
}
