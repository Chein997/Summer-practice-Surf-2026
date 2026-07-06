package com.volna.app.apex.feature.bookingform

import com.volna.app.apex.core.error.AppError
import com.volna.app.apex.core.error.toUiError
import com.volna.app.apex.core.mvi.Store
import com.volna.app.apex.data.fake.AppErrorException
import com.volna.app.apex.domain.model.BookingConsents
import com.volna.app.apex.domain.model.BookingProfile
import com.volna.app.apex.domain.usecase.CreateBookingUseCase
import kotlinx.coroutines.CoroutineScope

class BookingFormStore(
    scope: CoroutineScope,
    slotId: String,
    private val createBooking: CreateBookingUseCase,
) : Store<BookingFormState, BookingFormEvent, BookingFormEffect>(
    initialState = BookingFormState(slotId = slotId),
    scope = scope,
) {
    override fun handleEvent(event: BookingFormEvent) {
        when (event) {
            is BookingFormEvent.FullNameChanged -> setState { copy(fullName = event.value, fieldErrors = fieldErrors - "fullName") }
            is BookingFormEvent.PhoneChanged -> setState { copy(phone = event.value, fieldErrors = fieldErrors - "phone") }
            is BookingFormEvent.EmailChanged -> setState { copy(email = event.value, fieldErrors = fieldErrors - "email") }
            is BookingFormEvent.AgeChanged -> setState { copy(age = event.value.filter { it.isDigit() }, fieldErrors = fieldErrors - "age") }
            BookingFormEvent.SafetyRulesToggled -> setState { copy(safetyRulesAccepted = !safetyRulesAccepted) }
            BookingFormEvent.ParentalConsentToggled -> setState { copy(parentalConsentAccepted = !parentalConsentAccepted) }
            BookingFormEvent.SubmitClicked -> submit()
            BookingFormEvent.BackClicked -> sendEffect(BookingFormEffect.NavigateBack)
        }
    }

    private fun submit() {
        launch {
            val ageInt = currentState.age.toIntOrNull()
            if (ageInt == null) {
                setState { copy(fieldErrors = fieldErrors + ("age" to "Введите возраст")) }
                return@launch
            }

            setState { copy(isLoading = true, error = null) }
            try {
                val booking = createBooking(
                    slotId = currentState.slotId,
                    profile = BookingProfile(
                        fullName = currentState.fullName,
                        phone = currentState.phone,
                        email = currentState.email,
                        age = ageInt,
                    ),
                    consents = BookingConsents(
                        safetyRulesAccepted = currentState.safetyRulesAccepted,
                        parentalConsentAccepted = currentState.parentalConsentAccepted,
                    ),
                )
                sendEffect(BookingFormEffect.NavigateToBookingCreated(booking.id))
                setState { copy(isLoading = false) }
            } catch (e: IllegalArgumentException) {
                setState { copy(isLoading = false, error = AppError.Validation(e.message ?: "Проверьте данные").toUiError()) }
            } catch (e: AppErrorException) {
                setState { copy(isLoading = false, error = e.error.toUiError()) }
            } catch (_: Throwable) {
                setState { copy(isLoading = false, error = AppError.Unknown.toUiError()) }
            }
        }
    }
}
