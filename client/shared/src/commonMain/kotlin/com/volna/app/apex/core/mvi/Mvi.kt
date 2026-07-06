package com.volna.app.apex.core.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

interface UiState
interface UiEvent
interface UiEffect

abstract class Store<State : UiState, Event : UiEvent, Effect : UiEffect>(
    initialState: State,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state

    private val effects = Channel<Effect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()

    protected val currentState: State
        get() = _state.value

    fun dispatch(event: Event) {
        handleEvent(event)
    }

    protected abstract fun handleEvent(event: Event)

    protected fun setState(reducer: State.() -> State) {
        _state.value = _state.value.reducer()
    }

    protected fun sendEffect(effect: Effect) {
        scope.launch {
            effects.send(effect)
        }
    }

    protected fun launch(block: suspend CoroutineScope.() -> Unit) {
        scope.launch(block = block)
    }
}
