package server

import "time"

type Config struct {
	HTTPAddr       string
	DatabaseURL    string
	AccessTokenTTL time.Duration
	OTPDevCode     string
}

type ErrorResponse struct {
	Code    string `json:"code"`
	Message string `json:"message"`
}

type AuthCustomerResponse struct {
	ID    string `json:"id"`
	Phone string `json:"phone"`
}

type AuthTokenResponse struct {
	AccessToken      string               `json:"accessToken"`
	TokenType        string               `json:"tokenType"`
	ExpiresInSeconds int64                `json:"expiresInSeconds"`
	Customer         AuthCustomerResponse `json:"customer"`
}

type RequestSMSRequest struct {
	Phone string `json:"phone"`
}

type VerifySMSRequest struct {
	Phone string `json:"phone"`
	Code  string `json:"code"`
}

type TrackConfigurationResponse struct {
	ID   string `json:"id"`
	Name string `json:"name"`
	Type string `json:"type"`
}

type RideLevelResponse struct {
	ID   string `json:"id"`
	Code string `json:"code"`
	Name string `json:"name"`
}

type MarshalResponse struct {
	ID   string `json:"id"`
	Name string `json:"name"`
}

type MoneyResponse struct {
	Amount   string `json:"amount"`
	Currency string `json:"currency"`
}

type CenterCancellationResponse struct {
	ReasonType  string `json:"reasonType"`
	ReasonText  string `json:"reasonText,omitempty"`
	CancelledAt string `json:"cancelledAt"`
}

type RideSlotResponse struct {
	ID                 string                      `json:"id"`
	TrackConfiguration TrackConfigurationResponse  `json:"trackConfiguration"`
	RideLevel          RideLevelResponse           `json:"rideLevel"`
	Marshal            *MarshalResponse            `json:"marshal,omitempty"`
	StartAt            string                      `json:"startAt"`
	DurationMinutes    int                         `json:"durationMinutes"`
	Capacity           int                         `json:"capacity"`
	FreePlaces         int                         `json:"freePlaces"`
	Price              MoneyResponse               `json:"price"`
	Status             string                      `json:"status"`
	Address            string                      `json:"address"`
	MeetingPoint       string                      `json:"meetingPoint"`
	SafetyRules        string                      `json:"safetyRules"`
	CancellationTerms  string                      `json:"cancellationTerms"`
	CanBook            bool                        `json:"canBook"`
	CenterCancellation *CenterCancellationResponse `json:"centerCancellation,omitempty"`
}

type RideSlotsListResponse struct {
	Items []RideSlotResponse `json:"items"`
}

type BookingProfileRequest struct {
	FullName string `json:"fullName"`
	Phone    string `json:"phone"`
	Email    string `json:"email"`
	Age      int    `json:"age"`
}

type CreateBookingRequest struct {
	SlotID                  string                `json:"slotId"`
	Profile                 BookingProfileRequest `json:"profile"`
	SafetyRulesAccepted     bool                  `json:"safetyRulesAccepted"`
	ParentalConsentAccepted bool                  `json:"parentalConsentAccepted"`
}

type BookingProfileResponse struct {
	FullName string `json:"fullName"`
	Phone    string `json:"phone"`
	Email    string `json:"email"`
	Age      int    `json:"age"`
}

type BookingResponse struct {
	ID                      string                      `json:"id"`
	SlotID                  string                      `json:"slotId"`
	ClientID                string                      `json:"clientId"`
	Profile                 BookingProfileResponse      `json:"profile"`
	SafetyRulesAccepted     bool                        `json:"safetyRulesAccepted"`
	ParentalConsentAccepted bool                        `json:"parentalConsentAccepted"`
	Status                  string                      `json:"status"`
	CreatedAt               string                      `json:"createdAt"`
	UpdatedAt               string                      `json:"updatedAt"`
	CanCancel               bool                        `json:"canCancel"`
	Slot                    *RideSlotResponse           `json:"slot,omitempty"`
	CenterCancellation      *CenterCancellationResponse `json:"centerCancellation,omitempty"`
}

type BookingsListResponse struct {
	Items []BookingResponse `json:"items"`
}

type RegisterDeviceTokenRequest struct {
	Platform   string `json:"platform"`
	Token      string `json:"token"`
	AppVersion string `json:"appVersion,omitempty"`
	Locale     string `json:"locale,omitempty"`
}

type DeleteDeviceTokenRequest struct {
	Token string `json:"token"`
}

type statusResponse struct {
	Status string `json:"status"`
}
