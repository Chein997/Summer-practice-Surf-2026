
package domain

import (
	"errors"
	"regexp"
	"strings"
	"time"
)

var (
	ErrValidation             = errors.New("validation error")
	ErrSlotCancelled          = errors.New("slot cancelled")
	ErrNoFreePlaces           = errors.New("no free places")
	ErrDuplicateBooking       = errors.New("duplicate booking")
	ErrCancellationUnavailable = errors.New("cancellation unavailable")
)

var (
	phoneRegexp = regexp.MustCompile(`^\+[1-9][0-9]{7,14}$`)
	emailRegexp = regexp.MustCompile(`^[^@\s]+@[^@\s]+\.[^@\s]+$`)
)

type Customer struct {
	ID        string
	Phone     string
	CreatedAt time.Time
}

type Money struct {
	Amount   int64
	Currency string
}

type TrackConfigurationType string

const (
	TrackConfigurationShort TrackConfigurationType = "SHORT"
	TrackConfigurationLong  TrackConfigurationType = "LONG"
)

type TrackConfiguration struct {
	ID          string
	Name        string
	Type        TrackConfigurationType
	Description string
}

type RideLevelCode string

const (
	RideLevelNovice      RideLevelCode = "NOVICE"
	RideLevelExperienced RideLevelCode = "EXPERIENCED"
)

type RideLevel struct {
	ID          string
	Code        RideLevelCode
	Name        string
	Description string
}

type Marshal struct {
	ID    string
	Name  string
	Phone string
}

type RideSlotStatus string

const (
	RideSlotAvailable    RideSlotStatus = "AVAILABLE"
	RideSlotNoFreePlaces RideSlotStatus = "NO_FREE_PLACES"
	RideSlotCancelled    RideSlotStatus = "CANCELLED"
)

type CenterCancelReasonType string

const (
	CenterCancelWeather            CenterCancelReasonType = "WEATHER"
	CenterCancelTechnicalFailure   CenterCancelReasonType = "TECHNICAL_FAILURE"
	CenterCancelTrackUnavailable   CenterCancelReasonType = "TRACK_UNAVAILABLE"
	CenterCancelMarshalUnavailable CenterCancelReasonType = "MARSHAL_UNAVAILABLE"
	CenterCancelOrganizational     CenterCancelReasonType = "ORGANIZATIONAL"
	CenterCancelOther              CenterCancelReasonType = "OTHER"
)

type CenterCancellation struct {
	ReasonType CenterCancelReasonType
	ReasonText string
	CancelledAt time.Time
}

type RideSlot struct {
	ID                 string
	TrackConfiguration TrackConfiguration
	RideLevel           RideLevel
	Marshal             *Marshal
	StartAt             time.Time
	DurationMinutes     int
	Capacity            int
	FreePlaces          int
	Price               Money
	Status              RideSlotStatus
	Address             string
	MeetingPoint        string
	SafetyRules         string
	CancellationTerms   string
	CenterCancellation  *CenterCancellation
}

func (s RideSlot) CanBook() bool {
	return s.Status == RideSlotAvailable && s.FreePlaces > 0
}

func (s RideSlot) ValidateForBooking() error {
	if s.Status == RideSlotCancelled {
		return ErrSlotCancelled
	}
	if s.Status != RideSlotAvailable || s.FreePlaces <= 0 {
		return ErrNoFreePlaces
	}
	return nil
}

type BookingStatus string

const (
	BookingPendingConfirmation BookingStatus = "PENDING_CONFIRMATION"
	BookingActive              BookingStatus = "ACTIVE"
	BookingCancelledByClient   BookingStatus = "CANCELLED_BY_CLIENT"
	BookingCancelledByCenter   BookingStatus = "CANCELLED_BY_CENTER"
	BookingRejectedByCenter    BookingStatus = "REJECTED_BY_CENTER"
	BookingCompleted           BookingStatus = "COMPLETED"
	BookingNoShow              BookingStatus = "NO_SHOW"
)

type BookingProfile struct {
	FullName string
	Phone    string
	Email    string
	Age      int
}

func (p BookingProfile) Validate() error {
	if strings.TrimSpace(p.FullName) == "" {
		return ErrValidation
	}
	if !phoneRegexp.MatchString(p.Phone) {
		return ErrValidation
	}
	if !emailRegexp.MatchString(p.Email) {
		return ErrValidation
	}
	if p.Age < 16 || p.Age > 120 {
		return ErrValidation
	}
	return nil
}

type BookingConsents struct {
	SafetyRulesAccepted     bool
	ParentalConsentAccepted bool
}

func (c BookingConsents) ValidateForAge(age int) error {
	if !c.SafetyRulesAccepted {
		return ErrValidation
	}
	if age < 18 && !c.ParentalConsentAccepted {
		return ErrValidation
	}
	return nil
}

type Booking struct {
	ID                 string
	SlotID             string
	ClientID           string
	Profile            BookingProfile
	Consents           BookingConsents
	Status             BookingStatus
	CreatedAt          time.Time
	UpdatedAt          time.Time
	CancelledAt        *time.Time
	CancelSource       string
	CenterCancellation *CenterCancellation
	Slot               *RideSlot
}

func NewPendingBooking(slotID, clientID string, profile BookingProfile, consents BookingConsents, now time.Time) (*Booking, error) {
	if err := profile.Validate(); err != nil {
		return nil, err
	}
	if err := consents.ValidateForAge(profile.Age); err != nil {
		return nil, err
	}

	return &Booking{
		SlotID:    slotID,
		ClientID:  clientID,
		Profile:   profile,
		Consents:  consents,
		Status:    BookingPendingConfirmation,
		CreatedAt: now,
		UpdatedAt: now,
	}, nil
}

func (b Booking) CanCancel(now time.Time) bool {
	if b.Slot == nil {
		return false
	}
	if b.Status != BookingPendingConfirmation && b.Status != BookingActive {
		return false
	}
	return b.Slot.StartAt.After(now.Add(time.Hour))
}

type PushPlatform string

const (
	PushPlatformIOS     PushPlatform = "IOS"
	PushPlatformAndroid PushPlatform = "ANDROID"
)

type PushDeviceToken struct {
	ID        string
	ClientID  string
	Platform  PushPlatform
	Token     string
	AppVersion string
	Locale    string
	CreatedAt time.Time
	LastSeenAt *time.Time
}
