
package mapper

import (
	"errors"
	"net/http"

	"summer-practice-surf-2026/backend/internal/domain"
)

type ErrorResponse struct {
	Code    string `json:"code"`
	Message string `json:"message"`
}

func MapDomainError(err error) (int, ErrorResponse) {
	switch {
	case err == nil:
		return http.StatusOK, ErrorResponse{}
	case errors.Is(err, domain.ErrValidation):
		return http.StatusUnprocessableEntity, ErrorResponse{Code: "VALIDATION_ERROR", Message: "Проверьте данные формы."}
	case errors.Is(err, domain.ErrSlotCancelled):
		return http.StatusConflict, ErrorResponse{Code: "SLOT_CANCELLED", Message: "Заезд отменён центром."}
	case errors.Is(err, domain.ErrNoFreePlaces):
		return http.StatusConflict, ErrorResponse{Code: "NO_FREE_PLACES", Message: "На этот заезд больше нет свободных мест."}
	case errors.Is(err, domain.ErrDuplicateBooking):
		return http.StatusConflict, ErrorResponse{Code: "DUPLICATE_BOOKING", Message: "У вас уже есть бронь на этот заезд."}
	case errors.Is(err, domain.ErrCancellationUnavailable):
		return http.StatusConflict, ErrorResponse{Code: "ACTION_UNAVAILABLE", Message: "Отмена брони недоступна."}
	default:
		return http.StatusInternalServerError, ErrorResponse{Code: "INTERNAL_ERROR", Message: "Внутренняя ошибка сервиса."}
	}
}
