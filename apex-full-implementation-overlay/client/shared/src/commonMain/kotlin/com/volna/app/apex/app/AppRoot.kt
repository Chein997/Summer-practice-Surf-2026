package com.volna.app.apex.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.volna.app.apex.core.ui.ApexSecondaryButton
import com.volna.app.apex.data.fake.FakeAuthRepository
import com.volna.app.apex.data.fake.FakeBookingsRepository
import com.volna.app.apex.data.fake.FakePushRepository
import com.volna.app.apex.data.fake.FakeRideSlotsRepository
import com.volna.app.apex.data.fake.InMemoryTokenRepository
import com.volna.app.apex.feature.bookingcreated.BookingCreatedScreen
import com.volna.app.apex.feature.cancelbooking.BookingCancelledScreen
import com.volna.app.apex.feature.push.PushPermissionScreen

@Composable
fun ApexAppRoot() {
    MaterialTheme {
        // Lightweight fake-mode app shell.
        // Full store wiring should be done in the host module with rememberCoroutineScope and DI.
        val authRepository = remember { FakeAuthRepository() }
        val slotsRepository = remember { FakeRideSlotsRepository() }
        val bookingsRepository = remember { FakeBookingsRepository(slotsRepository) }
        val pushRepository = remember { FakePushRepository() }
        val tokenRepository = remember { InMemoryTokenRepository() }

        var route by remember { mutableStateOf<AppRoute>(AppRoute.PhoneInput) }

        Column {
            Row {
                ApexSecondaryButton(
                    text = "Слоты",
                    onClick = { route = AppRoute.Slots },
                )
                ApexSecondaryButton(
                    text = "Мои брони",
                    onClick = { route = AppRoute.MyBookings },
                )
            }

            Text(
                text = "Apex Karting MVP shell. Подключите Store-классы и DI в host-приложении.",
            )

            when (val current = route) {
                AppRoute.PhoneInput -> Text("SCR-001 подключается через PhoneInputScreen + PhoneInputStore")
                is AppRoute.SmsCode -> Text("SCR-002: ${current.phone}")
                AppRoute.Slots -> Text("SCR-003 подключается через SlotsScreen + SlotsStore")
                is AppRoute.SlotDetails -> Text("SCR-005: ${current.slotId}")
                is AppRoute.BookingForm -> Text("SCR-006: ${current.slotId}")
                is AppRoute.BookingCreated -> BookingCreatedScreen(
                    bookingId = current.bookingId,
                    onOpenMyBookings = { route = AppRoute.MyBookings },
                    onBackToSlots = { route = AppRoute.Slots },
                )
                AppRoute.MyBookings -> Text("SCR-008 подключается через MyBookingsScreen + MyBookingsStore")
                is AppRoute.BookingDetails -> Text("SCR-009: ${current.bookingId}")
                is AppRoute.CancelBooking -> Text("SCR-010: ${current.bookingId}")
                is AppRoute.BookingCancelled -> BookingCancelledScreen(
                    bookingId = current.bookingId,
                    onOpenMyBookings = { route = AppRoute.MyBookings },
                    onBackToSlots = { route = AppRoute.Slots },
                )
                AppRoute.PushPermission -> PushPermissionScreen(
                    isLoading = false,
                    onAllowClicked = { route = AppRoute.Slots },
                    onSkipClicked = { route = AppRoute.Slots },
                )
            }

            // Keep references alive and make fake composition obvious.
            listOf(authRepository, bookingsRepository, pushRepository, tokenRepository)
        }
    }
}
