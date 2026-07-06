package com.volna.app.apex.feature.auth

import com.volna.app.apex.core.error.AppError
import com.volna.app.apex.core.error.toUiError
import com.volna.app.apex.core.mvi.Store
import com.volna.app.apex.data.fake.AppErrorException
import com.volna.app.apex.domain.usecase.RequestSmsCodeUseCase
import com.volna.app.apex.domain.usecase.VerifySmsCodeUseCase
import kotlinx.coroutines.CoroutineScope

class PhoneInputStore(
    scope: CoroutineScope,
    private val requestSmsCode: RequestSmsCodeUseCase,
) : Store<PhoneInputState, PhoneInputEvent, PhoneInputEffect>(
    initialState = PhoneInputState(),
    scope = scope,
) {
    override fun handleEvent(event: PhoneInputEvent) {
        when (event) {
            is PhoneInputEvent.PhoneChanged -> setState {
                copy(phone = event.value, phoneError = null, error = null)
            }
            PhoneInputEvent.ContinueClicked,
            PhoneInputEvent.RetryClicked -> submit()
        }
    }

    private fun submit() {
        launch {
            setState { copy(isLoading = true, error = null, phoneError = null) }
            try {
                requestSmsCode(currentState.phone)
                sendEffect(PhoneInputEffect.NavigateToSmsCode(currentState.phone))
            } catch (e: IllegalArgumentException) {
                setState { copy(phoneError = e.message, isLoading = false) }
                return@launch
            } catch (e: AppErrorException) {
                setState { copy(error = e.error.toUiError(), isLoading = false) }
                return@launch
            } catch (_: Throwable) {
                setState { copy(error = AppError.Unknown.toUiError(), isLoading = false) }
                return@launch
            }
            setState { copy(isLoading = false) }
        }
    }
}

class SmsCodeStore(
    scope: CoroutineScope,
    initialPhone: String,
    private val verifySmsCode: VerifySmsCodeUseCase,
    private val requestSmsCode: RequestSmsCodeUseCase,
) : Store<SmsCodeState, SmsCodeEvent, SmsCodeEffect>(
    initialState = SmsCodeState(phone = initialPhone),
    scope = scope,
) {
    override fun handleEvent(event: SmsCodeEvent) {
        when (event) {
            is SmsCodeEvent.CodeChanged -> setState {
                copy(code = event.value.take(8), codeError = null, error = null)
            }
            SmsCodeEvent.SubmitClicked -> submit()
            SmsCodeEvent.ResendClicked -> resend()
            SmsCodeEvent.BackClicked -> sendEffect(SmsCodeEffect.NavigateBack)
        }
    }

    private fun submit() {
        launch {
            setState { copy(isLoading = true, error = null, codeError = null) }
            try {
                verifySmsCode(currentState.phone, currentState.code)
                sendEffect(SmsCodeEffect.NavigateToSlots)
            } catch (e: IllegalArgumentException) {
                setState { copy(codeError = e.message, isLoading = false) }
                return@launch
            } catch (e: AppErrorException) {
                setState { copy(error = e.error.toUiError(), isLoading = false) }
                return@launch
            } catch (_: Throwable) {
                setState { copy(error = AppError.Unknown.toUiError(), isLoading = false) }
                return@launch
            }
            setState { copy(isLoading = false) }
        }
    }

    private fun resend() {
        launch {
            runCatching { requestSmsCode(currentState.phone) }
        }
    }
}
