
package bookings

import (
	"context"
	"errors"
	"time"

	"summer-practice-surf-2026/backend/internal/domain"
)

// Ports are intentionally small: implementations can wrap existing postgres repositories.
type SlotRepository interface {
	GetByIDForUpdate(ctx context.Context, slotID string) (*domain.RideSlot, error)
	DecreaseFreePlaces(ctx context.Context, slotID string) error
	IncreaseFreePlaces(ctx context.Context, slotID string) error
}

type BookingRepository interface {
	ExistsNotFinalByClientAndSlot(ctx context.Context, clientID string, slotID string) (bool, error)
	Create(ctx context.Context, booking *domain.Booking) (*domain.Booking, error)
	GetByIDForUpdate(ctx context.Context, bookingID string) (*domain.Booking, error)
	MarkCancelledByClient(ctx context.Context, bookingID string, cancelledAt time.Time) error
}

type TxManager interface {
	WithinTx(ctx context.Context, fn func(ctx context.Context) error) error
}

type Clock interface {
	Now() time.Time
}

type Service struct {
	tx       TxManager
	slots    SlotRepository
	bookings BookingRepository
	clock    Clock
}

func NewService(tx TxManager, slots SlotRepository, bookings BookingRepository, clock Clock) *Service {
	return &Service{tx: tx, slots: slots, bookings: bookings, clock: clock}
}

type CreateBookingCommand struct {
	ClientID                  string
	SlotID                    string
	FullName                  string
	Phone                     string
	Email                     string
	Age                       int
	SafetyRulesAccepted        bool
	ParentalConsentAccepted    bool
}

func (s *Service) CreateBooking(ctx context.Context, cmd CreateBookingCommand) (*domain.Booking, error) {
	var created *domain.Booking

	err := s.tx.WithinTx(ctx, func(txCtx context.Context) error {
		slot, err := s.slots.GetByIDForUpdate(txCtx, cmd.SlotID)
		if err != nil {
			return err
		}
		if err := slot.ValidateForBooking(); err != nil {
			return err
		}

		exists, err := s.bookings.ExistsNotFinalByClientAndSlot(txCtx, cmd.ClientID, cmd.SlotID)
		if err != nil {
			return err
		}
		if exists {
			return domain.ErrDuplicateBooking
		}

		booking, err := domain.NewPendingBooking(
			cmd.SlotID,
			cmd.ClientID,
			domain.BookingProfile{
				FullName: cmd.FullName,
				Phone:    cmd.Phone,
				Email:    cmd.Email,
				Age:      cmd.Age,
			},
			domain.BookingConsents{
				SafetyRulesAccepted:      cmd.SafetyRulesAccepted,
				ParentalConsentAccepted: cmd.ParentalConsentAccepted,
			},
			s.clock.Now(),
		)
		if err != nil {
			return err
		}

		created, err = s.bookings.Create(txCtx, booking)
		if err != nil {
			return err
		}
		if err := s.slots.DecreaseFreePlaces(txCtx, cmd.SlotID); err != nil {
			return err
		}
		return nil
	})

	return created, err
}

func (s *Service) CancelByClient(ctx context.Context, clientID string, bookingID string) error {
	return s.tx.WithinTx(ctx, func(txCtx context.Context) error {
		booking, err := s.bookings.GetByIDForUpdate(txCtx, bookingID)
		if err != nil {
			return err
		}
		if booking.ClientID != clientID {
			return errors.New("forbidden")
		}
		if booking.Slot == nil {
			slot, err := s.slots.GetByIDForUpdate(txCtx, booking.SlotID)
			if err != nil {
				return err
			}
			booking.Slot = slot
		}
		now := s.clock.Now()
		if !booking.CanCancel(now) {
			return domain.ErrCancellationUnavailable
		}
		if err := s.bookings.MarkCancelledByClient(txCtx, bookingID, now); err != nil {
			return err
		}
		return s.slots.IncreaseFreePlaces(txCtx, booking.SlotID)
	})
}
