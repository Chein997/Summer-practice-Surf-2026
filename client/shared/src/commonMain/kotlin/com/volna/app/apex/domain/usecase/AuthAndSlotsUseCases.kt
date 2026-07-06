package com.volna.app.apex.domain.usecase

import com.volna.app.apex.domain.model.AuthToken
import com.volna.app.apex.domain.model.RideSlot
import com.volna.app.apex.domain.repository.AuthRepository
import com.volna.app.apex.domain.repository.RideSlotsRepository
import com.volna.app.apex.domain.repository.TokenRepository

class RequestSmsCodeUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(phone: String) {
        require(phone.matches(Regex("^\\+[1-9][0-9]{7,14}$"))) {
            "Введите телефон в международном формате, например +79990000000"
        }
        repository.requestSms(phone)
    }
}

class VerifySmsCodeUseCase(
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository,
) {
    suspend operator fun invoke(phone: String, code: String): AuthToken {
        require(code.length in 4..8) { "Введите SMS-код" }
        val token = authRepository.verifySms(phone, code)
        tokenRepository.saveAccessToken(token.accessToken)
        return token
    }
}

class ObserveAuthStateUseCase(
    private val tokenRepository: TokenRepository,
) {
    suspend operator fun invoke(): Boolean = tokenRepository.getAccessToken() != null
}

class LoadRideSlotsUseCase(
    private val repository: RideSlotsRepository,
) {
    suspend operator fun invoke(): List<RideSlot> =
        repository.getRideSlots(days = 7, includeUnavailable = true)
}

class LoadRideSlotDetailsUseCase(
    private val repository: RideSlotsRepository,
) {
    suspend operator fun invoke(slotId: String): RideSlot = repository.getRideSlot(slotId)
}
