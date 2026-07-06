package com.volna.app.apex.app

import androidx.compose.runtime.Composable
import com.volna.app.apex.design.ApexTheme
import com.volna.app.apex.feature.bookingcreated.BookingCreatedScreen
import com.volna.app.apex.feature.cancelbooking.BookingCancelledScreen
import com.volna.app.apex.feature.errorstate.ErrorStateScreen
import com.volna.app.apex.feature.push.PushPermissionScreen
import com.volna.app.apex.core.error.UiError

@Composable
fun ApexDesignedAppRootPreview() {
    ApexTheme {
        BookingCreatedScreen(
            bookingId = "booking-demo",
            onOpenMyBookings = {},
            onBackToSlots = {},
        )
    }
}

@Composable
fun ApexDesignedErrorPreview() {
    ApexTheme {
        ErrorStateScreen(
            error = UiError(
                title = "Действие недоступно",
                message = "показываем понятную причину отказа.",
            ),
            onRefreshSchedule = {},
            onBack = {},
        )
    }
}

@Composable
fun ApexDesignedPushPreview() {
    ApexTheme {
        PushPermissionScreen(
            isLoading = false,
            onAllowClicked = {},
            onSkipClicked = {},
        )
    }
}

@Composable
fun ApexDesignedCancelledPreview() {
    ApexTheme {
        BookingCancelledScreen(
            bookingId = "booking-demo",
            onOpenMyBookings = {},
            onBackToSlots = {},
        )
    }
}
