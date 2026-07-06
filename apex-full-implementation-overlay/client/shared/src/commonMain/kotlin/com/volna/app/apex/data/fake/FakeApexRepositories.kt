package com.volna.app.apex.data.fake

import com.volna.app.apex.core.error.AppError
import com.volna.app.apex.domain.model.AuthToken
import com.volna.app.apex.domain.model.Booking
import com.volna.app.apex.domain.model.BookingConsents
import com.volna.app.apex.domain.model.BookingProfile
import com.volna.app.apex.domain.model.BookingStatus
import com.volna.app.apex.domain.model.CenterCancellation
import com.volna.app.apex.domain.model.Marshal
import com.volna.app.apex.domain.model.Money
import com.volna.app.apex.domain.model.PushPlatform
import com.volna.app.apex.domain.model.RideLevel
import com.volna.app.apex.domain.model.RideLevelCode
import com.volna.app.apex.domain.model.RideSlot
import com.volna.app.apex.domain.model.RideSlotStatus
import com.volna.app.apex.domain.model.TrackConfiguration
import com.volna.app.apex.domain.model.TrackConfigurationType
import com.volna.app.apex.domain.repository.AuthRepository
import com.volna.app.apex.domain.repository.BookingsRepository
import com.volna.app.apex.domain.repository.CreateBookingCommand
import com.volna.app.apex.domain.repository.PushRepository
import com.volna.app.apex.domain.repository.RideSlotsRepository
import com.volna.app.apex.domain.repository.TokenRepository

class InMemoryTokenRepository : TokenRepository {
    private var token: String? = null

    override suspend fun getAccessToken(): String? = token

    override suspend fun saveAccessToken(token: String) {
        this.token = token
    }

    override suspend fun clearAccessToken() {
        token = null
    }
}

class FakeAuthRepository : AuthRepository {
    override suspend fun requestSms(phone: String) = Unit

    override suspend fun verifySms(phone: String, code: String): AuthToken {
        if (code != "1111") {
            throw AppError.Business(
                code = "INVALID_SMS_CODE",
                message = "Неверный SMS-код. Для fake-режима используйте 1111.",
            ).asException()
        }
        return AuthToken(
            accessToken = "fake-access-token",
            expiresInSeconds = 3600,
        )
    }

    override suspend fun logout() = Unit
}

class FakeRideSlotsRepository : RideSlotsRepository {
    private val shortTrack = TrackConfiguration(
        id = "track-short",
        name = "Короткая трасса",
        type = TrackConfigurationType.SHORT,
    )

    private val longTrack = TrackConfiguration(
        id = "track-long",
        name = "Длинная трасса",
        type = TrackConfigurationType.LONG,
    )

    private val novice = RideLevel(
        id = "level-novice",
        code = RideLevelCode.NOVICE,
        name = "Новичковый",
    )

    private val experienced = RideLevel(
        id = "level-experienced",
        code = RideLevelCode.EXPERIENCED,
        name = "Опытный",
    )

    private val marshal = Marshal(
        id = "marshal-1",
        name = "Алексей Смирнов",
    )

    private val slots = listOf(
        RideSlot(
            id = "slot-available",
            trackConfiguration = shortTrack,
            rideLevel = novice,
            marshal = marshal,
            startAt = "Завтра, 10:00",
            durationMinutes = 15,
            capacity = 10,
            freePlaces = 7,
            price = Money(amount = 180000, currency = "RUB"),
            status = RideSlotStatus.AVAILABLE,
            address = "г. Москва, Картинг-центр «Апекс», ул. Примерная, 1",
            meetingPoint = "Главный вход, стойка регистрации",
            safetyRules = "Перед заездом необходимо пройти инструктаж и соблюдать правила безопасности.",
            cancellationTerms = "Клиент может отменить бронь, если до начала заезда осталось больше 1 часа.",
        ),
        RideSlot(
            id = "slot-full",
            trackConfiguration = shortTrack,
            rideLevel = novice,
            marshal = marshal,
            startAt = "Послезавтра, 12:00",
            durationMinutes = 15,
            capacity = 10,
            freePlaces = 0,
            price = Money(amount = 180000, currency = "RUB"),
            status = RideSlotStatus.NO_FREE_PLACES,
            address = "г. Москва, Картинг-центр «Апекс», ул. Примерная, 1",
            meetingPoint = "Главный вход, стойка регистрации",
            safetyRules = "Перед заездом необходимо пройти инструктаж и соблюдать правила безопасности.",
            cancellationTerms = "Клиент может отменить бронь, если до начала заезда осталось больше 1 часа.",
        ),
        RideSlot(
            id = "slot-cancelled",
            trackConfiguration = longTrack,
            rideLevel = experienced,
            marshal = marshal,
            startAt = "Через 3 дня, 18:00",
            durationMinutes = 20,
            capacity = 8,
            freePlaces = 8,
            price = Money(amount = 250000, currency = "RUB"),
            status = RideSlotStatus.CANCELLED,
            address = "г. Москва, Картинг-центр «Апекс», ул. Примерная, 1",
            meetingPoint = "Главный вход, стойка регистрации",
            safetyRules = "Перед заездом необходимо пройти инструктаж и соблюдать правила безопасности.",
            cancellationTerms = "Клиент может отменить бронь, если до начала заезда осталось больше 1 часа.",
            centerCancellation = CenterCancellation(
                reasonType = "TECHNICAL_FAILURE",
                reasonText = "Заезд отменён из-за технической неисправности оборудования.",
                cancelledAt = "Сегодня, 09:30",
            ),
        ),
    )

    override suspend fun getRideSlots(days: Int, includeUnavailable: Boolean): List<RideSlot> =
        if (includeUnavailable) slots else slots.filter { it.canBook }

    override suspend fun getRideSlot(slotId: String): RideSlot =
        slots.firstOrNull { it.id == slotId }
            ?: throw AppError.Business("SLOT_NOT_FOUND", "Слот не найден.").asException()
}

class FakeBookingsRepository(
    private val slotsRepository: RideSlotsRepository,
) : BookingsRepository {
    private val bookings = mutableListOf<Booking>()

    override suspend fun createBooking(command: CreateBookingCommand): Booking {
        val slot = slotsRepository.getRideSlot(command.slotId)

        if (slot.status == RideSlotStatus.CANCELLED) {
            throw AppError.Business("SLOT_CANCELLED", "Заезд отменён центром.").asException()
        }
        if (!slot.canBook) {
            throw AppError.Business("NO_FREE_PLACES", "На этот заезд больше нет свободных мест.").asException()
        }
        if (bookings.any { it.slotId == command.slotId && it.status in listOf(BookingStatus.PENDING_CONFIRMATION, BookingStatus.ACTIVE) }) {
            throw AppError.Business("DUPLICATE_BOOKING", "У вас уже есть бронь на этот заезд.").asException()
        }

        val booking = Booking(
            id = "booking-${bookings.size + 1}",
            slotId = command.slotId,
            customerId = "fake-customer",
            profile = command.profile,
            consents = command.consents,
            status = BookingStatus.PENDING_CONFIRMATION,
            createdAt = "Только что",
            canCancel = true,
            slot = slot,
        )
        bookings += booking
        return booking
    }

    override suspend fun getMyBookings(): List<Booking> = bookings.ifEmpty {
        listOf(
            Booking(
                id = "booking-demo",
                slotId = "slot-available",
                customerId = "fake-customer",
                profile = BookingProfile(
                    fullName = "Иван Петров",
                    phone = "+79991112233",
                    email = "ivan.petrov@example.com",
                    age = 24,
                ),
                consents = BookingConsents(
                    safetyRulesAccepted = true,
                    parentalConsentAccepted = false,
                ),
                status = BookingStatus.PENDING_CONFIRMATION,
                createdAt = "Сегодня",
                canCancel = true,
                slot = slotsRepository.getRideSlot("slot-available"),
            )
        )
    }

    override suspend fun getBooking(bookingId: String): Booking =
        getMyBookings().firstOrNull { it.id == bookingId }
            ?: throw AppError.Business("BOOKING_NOT_FOUND", "Бронь не найдена.").asException()

    override suspend fun cancelBooking(bookingId: String): Booking {
        val existing = getBooking(bookingId)
        if (!existing.canCancel) {
            throw AppError.Business("ACTION_UNAVAILABLE", "Отмена этой брони недоступна.").asException()
        }
        val cancelled = existing.copy(
            status = BookingStatus.CANCELLED_BY_CLIENT,
            canCancel = false,
        )
        val index = bookings.indexOfFirst { it.id == bookingId }
        if (index >= 0) bookings[index] = cancelled
        return cancelled
    }
}

class FakePushRepository : PushRepository {
    override suspend fun registerDeviceToken(
        platform: PushPlatform,
        token: String,
        appVersion: String?,
        locale: String?,
    ) = Unit

    override suspend fun deleteDeviceToken(token: String) = Unit
}

class AppErrorException(val error: AppError) : RuntimeException()

fun AppError.asException(): AppErrorException = AppErrorException(this)
