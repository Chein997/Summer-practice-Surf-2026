-- +goose Up

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Клиенты мобильного приложения.
CREATE TABLE clients (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    phone text NOT NULL UNIQUE,
    created_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,

    CONSTRAINT clients_phone_e164_chk
    CHECK (phone ~ '^\+[1-9][0-9]{7,14}$')
);

-- Одноразовые SMS-коды для авторизации.
CREATE TABLE otp_codes (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    phone text NOT NULL,
    purpose text NOT NULL,
    code_hash text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    expires_at timestamptz NOT NULL,
    consumed_at timestamptz,
    attempt_count integer NOT NULL DEFAULT 0,

    CONSTRAINT otp_codes_phone_e164_chk
    CHECK (phone ~ '^\+[1-9][0-9]{7,14}$'),

    CONSTRAINT otp_codes_purpose_chk
    CHECK (purpose IN ('LOGIN')),

    CONSTRAINT otp_codes_expires_after_created_chk
    CHECK (expires_at > created_at),

    CONSTRAINT otp_codes_attempt_count_chk
    CHECK (attempt_count >= 0),

    CONSTRAINT otp_codes_consumed_after_created_chk
    CHECK (consumed_at IS NULL OR consumed_at >= created_at)
);

-- Авторизационные сессии клиента.
CREATE TABLE auth_sessions (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id uuid NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    token_hash text NOT NULL UNIQUE,
    created_at timestamptz NOT NULL DEFAULT now(),
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz,

    CONSTRAINT auth_sessions_expires_after_created_chk
    CHECK (expires_at > created_at),

    CONSTRAINT auth_sessions_revoked_after_created_chk
    CHECK (revoked_at IS NULL OR revoked_at >= created_at)
);

-- Конфигурации трассы картинг-центра.
CREATE TABLE track_configurations (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name text NOT NULL,
    type text NOT NULL,
    description text,
    created_at timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT track_configurations_name_chk
    CHECK (char_length(trim(name)) BETWEEN 1 AND 255),

    CONSTRAINT track_configurations_type_chk
    CHECK (type IN ('SHORT', 'LONG'))
);

-- Уровни сложности заездов.
CREATE TABLE ride_levels (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code text NOT NULL UNIQUE,
    name text NOT NULL,
    description text,
    created_at timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT ride_levels_code_chk
    CHECK (code IN ('NOVICE', 'EXPERIENCED')),

    CONSTRAINT ride_levels_name_chk
    CHECK (char_length(trim(name)) BETWEEN 1 AND 255)
);

-- Маршалы / инструкторы заезда.
CREATE TABLE marshals (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name text NOT NULL,
    phone text,
    created_at timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT marshals_name_chk
    CHECK (char_length(trim(name)) BETWEEN 1 AND 255),

    CONSTRAINT marshals_phone_e164_chk
    CHECK (phone IS NULL OR phone ~ '^\+[1-9][0-9]{7,14}$')
);

-- Слоты заездов.
CREATE TABLE ride_slots (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),

    track_configuration_id uuid NOT NULL REFERENCES track_configurations(id) ON DELETE RESTRICT,
    ride_level_id uuid NOT NULL REFERENCES ride_levels(id) ON DELETE RESTRICT,
    marshal_id uuid REFERENCES marshals(id) ON DELETE SET NULL,

    start_at timestamptz NOT NULL,
    duration_minutes integer NOT NULL,

    capacity integer NOT NULL,
    free_places integer NOT NULL,

    price_amount numeric(10, 2) NOT NULL,
    price_currency char(3) NOT NULL DEFAULT 'RUB',

    status text NOT NULL DEFAULT 'AVAILABLE',

    address text NOT NULL,
    meeting_point text NOT NULL,
    safety_rules text NOT NULL,
    cancellation_terms text NOT NULL,

    center_cancel_reason_type text,
    center_cancel_reason_text text,
    center_cancelled_at timestamptz,

    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT ride_slots_duration_chk
    CHECK (duration_minutes > 0),

    CONSTRAINT ride_slots_places_chk
    CHECK (capacity > 0 AND free_places >= 0 AND free_places <= capacity),

    CONSTRAINT ride_slots_price_chk
    CHECK (price_amount >= 0),

    CONSTRAINT ride_slots_currency_chk
    CHECK (price_currency ~ '^[A-Z]{3}$'),

    CONSTRAINT ride_slots_status_chk
    CHECK (status IN ('AVAILABLE', 'NO_FREE_PLACES', 'CANCELLED')),

    CONSTRAINT ride_slots_status_places_chk
    CHECK (
        (status = 'AVAILABLE' AND free_places > 0)
        OR (status = 'NO_FREE_PLACES' AND free_places = 0)
        OR (status = 'CANCELLED')
    ),

    CONSTRAINT ride_slots_center_cancel_reason_type_chk
    CHECK (
        center_cancel_reason_type IS NULL
        OR center_cancel_reason_type IN (
            'WEATHER',
            'TECHNICAL_FAILURE',
            'TRACK_UNAVAILABLE',
            'MARSHAL_UNAVAILABLE',
            'ORGANIZATIONAL',
            'OTHER'
        )
    ),

    CONSTRAINT ride_slots_other_reason_text_chk
    CHECK (
        center_cancel_reason_type IS DISTINCT FROM 'OTHER'
        OR nullif(trim(center_cancel_reason_text), '') IS NOT NULL
    ),

    CONSTRAINT ride_slots_cancelled_fields_chk
    CHECK (
        (
            status = 'CANCELLED'
            AND center_cancelled_at IS NOT NULL
            AND center_cancel_reason_type IS NOT NULL
        )
        OR (
            status <> 'CANCELLED'
            AND center_cancelled_at IS NULL
            AND center_cancel_reason_type IS NULL
            AND center_cancel_reason_text IS NULL
        )
    ),

    CONSTRAINT ride_slots_text_fields_chk
    CHECK (
        char_length(trim(address)) > 0
        AND char_length(trim(meeting_point)) > 0
        AND char_length(trim(safety_rules)) > 0
        AND char_length(trim(cancellation_terms)) > 0
    )
);

-- Бронирования клиентов.
-- В MVP одна бронь = одно место на один заезд.
CREATE TABLE bookings (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),

    slot_id uuid NOT NULL REFERENCES ride_slots(id) ON DELETE RESTRICT,
    client_id uuid NOT NULL REFERENCES clients(id) ON DELETE RESTRICT,

    profile_full_name text NOT NULL,
    profile_phone text NOT NULL,
    profile_email text NOT NULL,
    profile_age integer NOT NULL,

    safety_rules_accepted boolean NOT NULL,
    parental_consent_accepted boolean NOT NULL DEFAULT false,

    status text NOT NULL DEFAULT 'PENDING_CONFIRMATION',

    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),

    canceled_at timestamptz,
    cancel_source text,

    center_cancel_reason_type text,
    center_cancel_reason_text text,

    CONSTRAINT bookings_status_chk
    CHECK (
        status IN (
            'PENDING_CONFIRMATION',
            'ACTIVE',
            'CANCELLED_BY_CLIENT',
            'CANCELLED_BY_CENTER',
            'REJECTED_BY_CENTER',
            'COMPLETED',
            'NO_SHOW'
        )
    ),

    CONSTRAINT bookings_full_name_chk
    CHECK (char_length(trim(profile_full_name)) BETWEEN 1 AND 255),

    CONSTRAINT bookings_profile_phone_e164_chk
    CHECK (profile_phone ~ '^\+[1-9][0-9]{7,14}$'),

    CONSTRAINT bookings_email_chk
    CHECK (
        char_length(profile_email) BETWEEN 3 AND 255
        AND profile_email ~* '^[^@\s]+@[^@\s]+\.[^@\s]+$'
    ),

    CONSTRAINT bookings_age_chk
    CHECK (profile_age BETWEEN 16 AND 120),

    CONSTRAINT bookings_safety_rules_accepted_chk
    CHECK (safety_rules_accepted IS TRUE),

    CONSTRAINT bookings_parental_consent_chk
    CHECK (
        profile_age >= 18
        OR parental_consent_accepted IS TRUE
    ),

    CONSTRAINT bookings_cancel_source_chk
    CHECK (
        cancel_source IS NULL
        OR cancel_source IN ('CLIENT', 'CENTER')
    ),

    CONSTRAINT bookings_center_cancel_reason_type_chk
    CHECK (
        center_cancel_reason_type IS NULL
        OR center_cancel_reason_type IN (
            'WEATHER',
            'TECHNICAL_FAILURE',
            'TRACK_UNAVAILABLE',
            'MARSHAL_UNAVAILABLE',
            'ORGANIZATIONAL',
            'OTHER'
        )
    ),

    CONSTRAINT bookings_other_center_reason_text_chk
    CHECK (
        center_cancel_reason_type IS DISTINCT FROM 'OTHER'
        OR nullif(trim(center_cancel_reason_text), '') IS NOT NULL
    ),

    CONSTRAINT bookings_cancel_fields_chk
    CHECK (
        (
            status = 'CANCELLED_BY_CLIENT'
            AND canceled_at IS NOT NULL
            AND cancel_source = 'CLIENT'
            AND center_cancel_reason_type IS NULL
            AND center_cancel_reason_text IS NULL
        )
        OR (
            status = 'CANCELLED_BY_CENTER'
            AND canceled_at IS NOT NULL
            AND cancel_source = 'CENTER'
            AND center_cancel_reason_type IS NOT NULL
        )
        OR (
            status NOT IN ('CANCELLED_BY_CLIENT', 'CANCELLED_BY_CENTER')
            AND canceled_at IS NULL
            AND cancel_source IS NULL
            AND center_cancel_reason_type IS NULL
            AND center_cancel_reason_text IS NULL
        )
    )
);

-- Запрет дубля активной или ожидающей подтверждения брони клиента на один слот.
CREATE UNIQUE INDEX bookings_not_final_client_slot_uidx
ON bookings (client_id, slot_id)
WHERE status IN ('PENDING_CONFIRMATION', 'ACTIVE');

-- Токены устройств для push-уведомлений.
CREATE TABLE push_device_tokens (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),

    client_id uuid NOT NULL REFERENCES clients(id) ON DELETE CASCADE,

    platform text NOT NULL,
    token text NOT NULL,
    app_version text,
    locale text,

    created_at timestamptz NOT NULL DEFAULT now(),
    last_seen_at timestamptz,

    CONSTRAINT push_device_tokens_platform_chk
    CHECK (platform IN ('IOS', 'ANDROID')),

    CONSTRAINT push_device_tokens_token_len_chk
    CHECK (char_length(token) BETWEEN 1 AND 4096),

    CONSTRAINT push_device_tokens_locale_len_chk
    CHECK (locale IS NULL OR char_length(locale) BETWEEN 2 AND 32),

    CONSTRAINT push_device_tokens_last_seen_after_created_chk
    CHECK (last_seen_at IS NULL OR last_seen_at >= created_at),

    UNIQUE (client_id, token)
);

-- Очередь/история push-событий.
CREATE TABLE push_notification_events (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),

    booking_id uuid NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    client_id uuid NOT NULL REFERENCES clients(id) ON DELETE CASCADE,

    type text NOT NULL,
    title text NOT NULL,
    body text NOT NULL,

    scheduled_at timestamptz,
    sent_at timestamptz,
    failed_at timestamptz,
    failure_reason text,

    created_at timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT push_notification_events_type_chk
    CHECK (
        type IN (
            'BOOKING_CONFIRMED',
            'BOOKING_REJECTED',
            'RIDE_REMINDER_24H',
            'RIDE_REMINDER_2H',
            'RIDE_CANCELLED_BY_CENTER'
        )
    ),

    CONSTRAINT push_notification_events_text_chk
    CHECK (
        char_length(trim(title)) BETWEEN 1 AND 255
        AND char_length(trim(body)) BETWEEN 1 AND 1000
    ),

    CONSTRAINT push_notification_events_delivery_state_chk
    CHECK (
        NOT (sent_at IS NOT NULL AND failed_at IS NOT NULL)
    )
);

-- Идемпотентность критичных POST-запросов, например создания брони.
CREATE TABLE idempotency_keys (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),

    client_id uuid REFERENCES clients(id) ON DELETE CASCADE,
    idempotency_key text NOT NULL,
    method text NOT NULL,
    path text NOT NULL,

    request_hash text,
    response_status integer,
    response_body jsonb,

    created_at timestamptz NOT NULL DEFAULT now(),
    expires_at timestamptz NOT NULL,

    CONSTRAINT idempotency_keys_key_len_chk
    CHECK (char_length(idempotency_key) BETWEEN 1 AND 255),

    CONSTRAINT idempotency_keys_method_chk
    CHECK (method IN ('POST', 'PUT', 'PATCH', 'DELETE')),

    CONSTRAINT idempotency_keys_expires_after_created_chk
    CHECK (expires_at > created_at),

    CONSTRAINT idempotency_keys_response_status_chk
    CHECK (response_status IS NULL OR response_status BETWEEN 100 AND 599),

    UNIQUE (client_id, method, path, idempotency_key)
);

CREATE INDEX otp_codes_phone_purpose_created_at_idx
ON otp_codes (phone, purpose, created_at DESC);

CREATE INDEX auth_sessions_client_id_idx
ON auth_sessions (client_id);

CREATE INDEX auth_sessions_expires_at_idx
ON auth_sessions (expires_at);

CREATE INDEX ride_slots_start_at_idx
ON ride_slots (start_at);

CREATE INDEX ride_slots_status_idx
ON ride_slots (status);

CREATE INDEX ride_slots_track_configuration_id_idx
ON ride_slots (track_configuration_id);

CREATE INDEX ride_slots_ride_level_id_idx
ON ride_slots (ride_level_id);

CREATE INDEX bookings_client_id_created_at_idx
ON bookings (client_id, created_at DESC);

CREATE INDEX bookings_slot_id_idx
ON bookings (slot_id);

CREATE INDEX bookings_status_idx
ON bookings (status);

CREATE INDEX push_device_tokens_client_id_idx
ON push_device_tokens (client_id);

CREATE INDEX push_notification_events_client_id_created_at_idx
ON push_notification_events (client_id, created_at DESC);

CREATE INDEX push_notification_events_scheduled_at_idx
ON push_notification_events (scheduled_at)
WHERE sent_at IS NULL AND failed_at IS NULL;

CREATE INDEX idempotency_keys_expires_at_idx
ON idempotency_keys (expires_at);

-- +goose Down

DROP TABLE IF EXISTS idempotency_keys;
DROP TABLE IF EXISTS push_notification_events;
DROP TABLE IF EXISTS push_device_tokens;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS ride_slots;
DROP TABLE IF EXISTS marshals;
DROP TABLE IF EXISTS ride_levels;
DROP TABLE IF EXISTS track_configurations;
DROP TABLE IF EXISTS auth_sessions;
DROP TABLE IF EXISTS otp_codes;
DROP TABLE IF EXISTS clients;
