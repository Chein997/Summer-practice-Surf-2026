package com.volna.app.apex.core.error

sealed interface AppError {
    data object NetworkUnavailable : AppError
    data object Unauthorized : AppError
    data object Forbidden : AppError
    data object ServiceUnavailable : AppError
    data object Unknown : AppError

    data class Validation(
        val message: String,
        val fields: Map<String, String> = emptyMap(),
    ) : AppError

    data class Business(
        val code: String,
        val message: String,
    ) : AppError
}

data class UiError(
    val title: String,
    val message: String,
    val retryAvailable: Boolean = false,
    val primaryActionTitle: String? = null,
)

fun AppError.toUiError(): UiError = when (this) {
    AppError.NetworkUnavailable -> UiError(
        title = "Нет соединения",
        message = "Проверьте интернет и попробуйте ещё раз.",
        retryAvailable = true,
    )

    AppError.Unauthorized -> UiError(
        title = "Нужно войти",
        message = "Сессия истекла. Войдите по номеру телефона ещё раз.",
        retryAvailable = false,
        primaryActionTitle = "Войти",
    )

    AppError.Forbidden -> UiError(
        title = "Действие недоступно",
        message = "У вас нет доступа к этому действию.",
    )

    AppError.ServiceUnavailable -> UiError(
        title = "Сервис временно недоступен",
        message = "Попробуйте повторить действие позже.",
        retryAvailable = true,
    )

    is AppError.Validation -> UiError(
        title = "Проверьте данные",
        message = message,
    )

    is AppError.Business -> UiError(
        title = when (code) {
            "NO_FREE_PLACES" -> "Мест больше нет"
            "SLOT_CANCELLED" -> "Заезд отменён"
            "DUPLICATE_BOOKING" -> "Бронь уже есть"
            "ACTION_UNAVAILABLE" -> "Действие недоступно"
            else -> "Действие не выполнено"
        },
        message = message,
        primaryActionTitle = when (code) {
            "NO_FREE_PLACES", "SLOT_CANCELLED" -> "Выбрать другой заезд"
            else -> null
        },
    )

    AppError.Unknown -> UiError(
        title = "Что-то пошло не так",
        message = "Попробуйте повторить действие.",
        retryAvailable = true,
    )
}
