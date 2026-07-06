package com.volna.app.apex.domain.repository

import com.volna.app.apex.domain.model.AuthToken
import com.volna.app.apex.domain.model.Booking
import com.volna.app.apex.domain.model.BookingConsents
import com.volna.app.apex.domain.model.BookingProfile
import com.volna.app.apex.domain.model.PushPlatform
import com.volna.app.apex.domain.model.RideSlot

interface AuthRepository {
    suspend fun requestSms(phone: String)
    suspend fun verifySms(phone: String, code: String): AuthToken
    suspend fun logout()
}

interface RideSlotsRepository {
    suspend fun getRideSlots(days: Int = 7, includeUnavailable: Boolean = true): List<RideSlot>
    suspend fun getRideSlot(slotId: String): RideSlot
}

data class CreateBookingCommand(
    val slotId: String,
    val profile: BookingProfile,
    val consents: BookingConsents,
)

interface BookingsRepository {
    suspend fun createBooking(command: CreateBookingCommand): Booking
    suspend fun getMyBookings(): List<Booking>
    suspend fun getBooking(bookingId: String): Booking
    suspend fun cancelBooking(bookingId: String): Booking
}

interface PushRepository {
    suspend fun registerDeviceToken(
        platform: PushPlatform,
        token: String,
        appVersion: String?,
        locale: String?,
    )

    suspend fun deleteDeviceToken(token: String)
}

interface TokenRepository {
    suspend fun getAccessToken(): String?
    suspend fun saveAccessToken(token: String)
    suspend fun clearAccessToken()
}
