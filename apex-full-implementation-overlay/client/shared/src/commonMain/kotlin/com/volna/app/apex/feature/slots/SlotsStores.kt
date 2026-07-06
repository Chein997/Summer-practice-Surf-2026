package com.volna.app.apex.feature.slots

import com.volna.app.apex.core.error.AppError
import com.volna.app.apex.core.error.toUiError
import com.volna.app.apex.core.mvi.Store
import com.volna.app.apex.data.fake.AppErrorException
import com.volna.app.apex.domain.usecase.LoadRideSlotDetailsUseCase
import com.volna.app.apex.domain.usecase.LoadRideSlotsUseCase
import kotlinx.coroutines.CoroutineScope

class SlotsStore(
    scope: CoroutineScope,
    private val loadRideSlots: LoadRideSlotsUseCase,
) : Store<SlotsState, SlotsEvent, SlotsEffect>(
    initialState = SlotsState(),
    scope = scope,
) {
    override fun handleEvent(event: SlotsEvent) {
        when (event) {
            SlotsEvent.ScreenOpened,
            SlotsEvent.RetryClicked -> load()
            is SlotsEvent.SlotClicked -> sendEffect(SlotsEffect.NavigateToSlotDetails(event.slotId))
        }
    }

    private fun load() {
        launch {
            setState { copy(isLoading = true, error = null) }
            try {
                val slots = loadRideSlots()
                setState { copy(isLoading = false, items = slots) }
            } catch (e: AppErrorException) {
                setState { copy(isLoading = false, error = e.error.toUiError()) }
            } catch (_: Throwable) {
                setState { copy(isLoading = false, error = AppError.Unknown.toUiError()) }
            }
        }
    }
}

class SlotDetailsStore(
    scope: CoroutineScope,
    slotId: String,
    private val loadRideSlotDetails: LoadRideSlotDetailsUseCase,
) : Store<SlotDetailsState, SlotDetailsEvent, SlotDetailsEffect>(
    initialState = SlotDetailsState(slotId = slotId),
    scope = scope,
) {
    override fun handleEvent(event: SlotDetailsEvent) {
        when (event) {
            SlotDetailsEvent.ScreenOpened,
            SlotDetailsEvent.RetryClicked -> load()
            SlotDetailsEvent.BackClicked -> sendEffect(SlotDetailsEffect.NavigateBack)
            SlotDetailsEvent.BookClicked -> currentState.slot?.let {
                if (it.canBook) sendEffect(SlotDetailsEffect.NavigateToBookingForm(it.id))
            }
        }
    }

    private fun load() {
        launch {
            setState { copy(isLoading = true, error = null) }
            try {
                val slot = loadRideSlotDetails(currentState.slotId)
                setState { copy(isLoading = false, slot = slot) }
            } catch (e: AppErrorException) {
                setState { copy(isLoading = false, error = e.error.toUiError()) }
            } catch (_: Throwable) {
                setState { copy(isLoading = false, error = AppError.Unknown.toUiError()) }
            }
        }
    }
}
