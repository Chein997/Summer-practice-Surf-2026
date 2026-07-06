package com.volna.app.apex.domain.usecase

import com.volna.app.apex.domain.model.Booking
import com.volna.app.apex.domain.model.BookingConsents
import com.volna.app.apex.domain.model.BookingProfile
import com.volna.app.apex.domain.repository.BookingsRepository
import com.volna.app.apex.domain.repository.CreateBookingCommand

class CreateBookingUseCase(
    private val repository: BookingsRepository,
) {
    suspend operator fun invoke(
        slotId: String,
        profile: BookingProfile,
        consents: BookingConsents,
    ): Booking {
        validateProfile(profile, consents)
        return repository.createBooking(
            CreateBookingCommand(
                slotId = slotId,
                profile = profile,
                consents = consents,
            )
        )
    }

    private fun validateProfile(profile: BookingProfile, consents: BookingConsents) {
        require(profile.fullName.isNotBlank()) { "Введите имя" }
        require(profile.phone.matches(Regex("^\\+[1-9][0-9]{7,14}$"))) {
            "Введите телефон в международном формате"
        }
        require(profile.email.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))) {
            "Введите корректный email"
        }
        require(profile.age >= 16) { "Минимальный возраст — 16 лет" }
        require(consents.safetyRulesAccepted) { "Нужно принять правила безопасности" }
        require(profile.age >= 18 || consents.parentalConsentAccepted) {
            "Для участников младше 18 лет нужно согласие родителя или законного представителя"
        }
    }
}

class LoadMyBookingsUseCase(
    private val repository: BookingsRepository,
) {
    suspend operator fun invoke(): List<Booking> = repository.getMyBookings()
}

class LoadBookingDetailsUseCase(
    private val repository: BookingsRepository,
) {
    suspend operator fun invoke(bookingId: String): Booking = repository.getBooking(bookingId)
}

class CancelBookingUseCase(
    private val repository: BookingsRepository,
) {
    suspend operator fun invoke(bookingId: String): Booking = repository.cancelBooking(bookingId)
}
