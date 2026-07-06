package com.volna.app.apex.feature.auth

import com.volna.app.apex.core.error.UiError
import com.volna.app.apex.core.mvi.UiEffect
import com.volna.app.apex.core.mvi.UiEvent
import com.volna.app.apex.core.mvi.UiState

data class PhoneInputState(
    val phone: String = "",
    val phoneError: String? = null,
    val isLoading: Boolean = false,
    val error: UiError? = null,
) : UiState {
    val canContinue: Boolean
        get() = !isLoading && phone.isNotBlank()
}

sealed interface PhoneInputEvent : UiEvent {
    data class PhoneChanged(val value: String) : PhoneInputEvent
    data object ContinueClicked : PhoneInputEvent
    data object RetryClicked : PhoneInputEvent
}

sealed interface PhoneInputEffect : UiEffect {
    data class NavigateToSmsCode(val phone: String) : PhoneInputEffect
}

data class SmsCodeState(
    val phone: String,
    val code: String = "",
    val codeError: String? = null,
    val isLoading: Boolean = false,
    val error: UiError? = null,
) : UiState {
    val canSubmit: Boolean
        get() = !isLoading && code.length >= 4
}

sealed interface SmsCodeEvent : UiEvent {
    data class CodeChanged(val value: String) : SmsCodeEvent
    data object SubmitClicked : SmsCodeEvent
    data object BackClicked : SmsCodeEvent
    data object ResendClicked : SmsCodeEvent
}

sealed interface SmsCodeEffect : UiEffect {
    data object NavigateToSlots : SmsCodeEffect
    data object NavigateBack : SmsCodeEffect
}
