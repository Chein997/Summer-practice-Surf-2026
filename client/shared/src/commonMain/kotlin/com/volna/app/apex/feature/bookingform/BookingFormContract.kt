package com.volna.app.apex.feature.bookingform

import com.volna.app.apex.core.error.UiError
import com.volna.app.apex.core.mvi.UiEffect
import com.volna.app.apex.core.mvi.UiEvent
import com.volna.app.apex.core.mvi.UiState

data class BookingFormState(
    val slotId: String,
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val age: String = "",
    val safetyRulesAccepted: Boolean = false,
    val parentalConsentAccepted: Boolean = false,
    val fieldErrors: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val error: UiError? = null,
) : UiState {
    val canSubmit: Boolean
        get() = !isLoading &&
            fullName.isNotBlank() &&
            phone.isNotBlank() &&
            email.isNotBlank() &&
            age.isNotBlank() &&
            safetyRulesAccepted
}

sealed interface BookingFormEvent : UiEvent {
    data class FullNameChanged(val value: String) : BookingFormEvent
    data class PhoneChanged(val value: String) : BookingFormEvent
    data class EmailChanged(val value: String) : BookingFormEvent
    data class AgeChanged(val value: String) : BookingFormEvent
    data object SafetyRulesToggled : BookingFormEvent
    data object ParentalConsentToggled : BookingFormEvent
    data object SubmitClicked : BookingFormEvent
    data object BackClicked : BookingFormEvent
}

sealed interface BookingFormEffect : UiEffect {
    data class NavigateToBookingCreated(val bookingId: String) : BookingFormEffect
    data object NavigateBack : BookingFormEffect
    data object NavigateToSlots : BookingFormEffect
}
