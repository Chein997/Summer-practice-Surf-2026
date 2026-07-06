package com.volna.app.apex.feature.mybookings

import com.volna.app.apex.core.error.AppError
import com.volna.app.apex.core.error.toUiError
import com.volna.app.apex.core.mvi.Store
import com.volna.app.apex.data.fake.AppErrorException
import com.volna.app.apex.domain.usecase.LoadMyBookingsUseCase
import kotlinx.coroutines.CoroutineScope

class MyBookingsStore(
    scope: CoroutineScope,
    private val loadMyBookings: LoadMyBookingsUseCase,
) : Store<MyBookingsState, MyBookingsEvent, MyBookingsEffect>(
    initialState = MyBookingsState(),
    scope = scope,
) {
    override fun handleEvent(event: MyBookingsEvent) {
        when (event) {
            MyBookingsEvent.ScreenOpened,
            MyBookingsEvent.RetryClicked -> load()
            is MyBookingsEvent.BookingClicked -> sendEffect(MyBookingsEffect.NavigateToBookingDetails(event.bookingId))
        }
    }

    private fun load() {
        launch {
            setState { copy(isLoading = true, error = null) }
            try {
                setState { copy(isLoading = false, items = loadMyBookings()) }
            } catch (e: AppErrorException) {
                setState { copy(isLoading = false, error = e.error.toUiError()) }
            } catch (_: Throwable) {
                setState { copy(isLoading = false, error = AppError.Unknown.toUiError()) }
            }
        }
    }
}
