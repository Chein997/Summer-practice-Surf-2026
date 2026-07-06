package com.volna.app.apex.feature.bookingdetails

import com.volna.app.apex.core.error.AppError
import com.volna.app.apex.core.error.toUiError
import com.volna.app.apex.core.mvi.Store
import com.volna.app.apex.data.fake.AppErrorException
import com.volna.app.apex.domain.usecase.LoadBookingDetailsUseCase
import kotlinx.coroutines.CoroutineScope

class BookingDetailsStore(
    scope: CoroutineScope,
    bookingId: String,
    private val loadBookingDetails: LoadBookingDetailsUseCase,
) : Store<BookingDetailsState, BookingDetailsEvent, BookingDetailsEffect>(
    initialState = BookingDetailsState(bookingId = bookingId),
    scope = scope,
) {
    override fun handleEvent(event: BookingDetailsEvent) {
        when (event) {
            BookingDetailsEvent.ScreenOpened,
            BookingDetailsEvent.RetryClicked -> load()
            BookingDetailsEvent.BackClicked -> sendEffect(BookingDetailsEffect.NavigateBack)
            BookingDetailsEvent.CancelClicked -> currentState.booking?.let {
                if (it.canCancel) sendEffect(BookingDetailsEffect.NavigateToCancelConfirmation(it.id))
            }
            BookingDetailsEvent.ChooseAnotherRideClicked -> sendEffect(BookingDetailsEffect.NavigateToSlots)
        }
    }

    private fun load() {
        launch {
            setState { copy(isLoading = true, error = null) }
            try {
                setState { copy(isLoading = false, booking = loadBookingDetails(currentState.bookingId)) }
            } catch (e: AppErrorException) {
                setState { copy(isLoading = false, error = e.error.toUiError()) }
            } catch (_: Throwable) {
                setState { copy(isLoading = false, error = AppError.Unknown.toUiError()) }
            }
        }
    }
}
