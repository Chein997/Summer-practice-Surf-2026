package com.volna.app.apex.domain.model

data class Customer(
    val id: String,
    val phone: String,
)

data class AuthToken(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresInSeconds: Long,
)

data class Money(
    val amount: Long,
    val currency: String,
) {
    fun format(): String = "${amount / 100}.${(amount % 100).toString().padStart(2, '0')} $currency"
}

enum class RideSlotStatus {
    AVAILABLE,
    NO_FREE_PLACES,
    CANCELLED,
    UNKNOWN,
}

enum class TrackConfigurationType {
    SHORT,
    LONG,
    UNKNOWN,
}

data class TrackConfiguration(
    val id: String,
    val name: String,
    val type: TrackConfigurationType,
)

enum class RideLevelCode {
    NOVICE,
    EXPERIENCED,
    UNKNOWN,
}

data class RideLevel(
    val id: String,
    val code: RideLevelCode,
    val name: String,
)

data class Marshal(
    val id: String,
    val name: String,
)

data class CenterCancellation(
    val reasonType: String,
    val reasonText: String?,
    val cancelledAt: String,
)

data class RideSlot(
    val id: String,
    val trackConfiguration: TrackConfiguration,
    val rideLevel: RideLevel,
    val marshal: Marshal?,
    val startAt: String,
    val durationMinutes: Int,
    val capacity: Int,
    val freePlaces: Int,
    val price: Money,
    val status: RideSlotStatus,
    val address: String,
    val meetingPoint: String,
    val safetyRules: String,
    val cancellationTerms: String,
    val centerCancellation: CenterCancellation? = null,
) {
    val canBook: Boolean
        get() = status == RideSlotStatus.AVAILABLE && freePlaces > 0
}

enum class BookingStatus {
    PENDING_CONFIRMATION,
    ACTIVE,
    CANCELLED_BY_CLIENT,
    CANCELLED_BY_CENTER,
    REJECTED_BY_CENTER,
    COMPLETED,
    NO_SHOW,
    UNKNOWN,
}

data class BookingProfile(
    val fullName: String,
    val phone: String,
    val email: String,
    val age: Int,
)

data class BookingConsents(
    val safetyRulesAccepted: Boolean,
    val parentalConsentAccepted: Boolean,
)

data class Booking(
    val id: String,
    val slotId: String,
    val customerId: String,
    val profile: BookingProfile,
    val consents: BookingConsents,
    val status: BookingStatus,
    val createdAt: String,
    val canCancel: Boolean,
    val slot: RideSlot?,
    val centerCancellation: CenterCancellation? = null,
)

enum class PushPlatform {
    IOS,
    ANDROID,
    UNSUPPORTED,
}
