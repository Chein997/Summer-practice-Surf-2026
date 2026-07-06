package com.volna.app.apex.feature.cancelbooking

import com.volna.app.apex.core.error.AppError
import com.volna.app.apex.core.error.toUiError
import com.volna.app.apex.core.mvi.Store
import com.volna.app.apex.data.fake.AppErrorException
import com.volna.app.apex.domain.usecase.CancelBookingUseCase
import kotlinx.coroutines.CoroutineScope

class CancelBookingStore(
    scope: CoroutineScope,
    bookingId: String,
    private val cancelBooking: CancelBookingUseCase,
) : Store<CancelBookingState, CancelBookingEvent, CancelBookingEffect>(
    initialState = CancelBookingState(bookingId = bookingId),
    scope = scope,
) {
    override fun handleEvent(event: CancelBookingEvent) {
        when (event) {
            CancelBookingEvent.BackClicked -> sendEffect(CancelBookingEffect.NavigateBack)
            CancelBookingEvent.ConfirmClicked -> submit()
        }
    }

    private fun submit() {
        launch {
            setState { copy(isLoading = true, error = null) }
            try {
                val booking = cancelBooking(currentState.bookingId)
                sendEffect(CancelBookingEffect.NavigateToCancelled(booking.id))
                setState { copy(isLoading = false) }
            } catch (e: AppErrorException) {
                setState { copy(isLoading = false, error = e.error.toUiError()) }
            } catch (_: Throwable) {
                setState { copy(isLoading = false, error = AppError.Unknown.toUiError()) }
            }
        }
    }
}
