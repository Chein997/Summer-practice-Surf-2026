package com.volna.app.apex.app

sealed interface AppRoute {
    data object PhoneInput : AppRoute
    data class SmsCode(val phone: String) : AppRoute
    data object Slots : AppRoute
    data class SlotDetails(val slotId: String) : AppRoute
    data class BookingForm(val slotId: String) : AppRoute
    data class BookingCreated(val bookingId: String) : AppRoute
    data object MyBookings : AppRoute
    data class BookingDetails(val bookingId: String) : AppRoute
    data class CancelBooking(val bookingId: String) : AppRoute
    data class BookingCancelled(val bookingId: String) : AppRoute
    data object PushPermission : AppRoute
}

class SimpleBackStack(
    initial: AppRoute,
) {
    private val stack = mutableListOf(initial)

    val current: AppRoute
        get() = stack.last()

    fun push(route: AppRoute) {
        stack += route
    }

    fun replace(route: AppRoute) {
        stack.clear()
        stack += route
    }

    fun back(): Boolean {
        if (stack.size <= 1) return false
        stack.removeLast()
        return true
    }
}
