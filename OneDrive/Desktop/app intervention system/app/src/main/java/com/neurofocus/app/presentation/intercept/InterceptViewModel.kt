package com.neurofocus.app.presentation.intercept

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurofocus.app.data.local.entity.UserProgress
import com.neurofocus.app.domain.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * InterceptViewModel – Handles XP deduction for the "Pay to Open" flow.
 *
 * Simple responsibilities:
 * 1. Load the user's current XP balance
 * 2. Deduct 50 XP on payment
 * 3. Report success or insufficient XP
 */
@HiltViewModel
class InterceptViewModel @Inject constructor(
    private val progressRepository: ProgressRepository
) : ViewModel() {

    data class InterceptState(
        val xpBalance: Int = 0,
        val xpCost: Int = 50,
        val result: PayResult = PayResult.IDLE,
        val isProcessing: Boolean = false
    )

    enum class PayResult { IDLE, SUCCESS, INSUFFICIENT }

    private val _state = MutableStateFlow(InterceptState())
    val state: StateFlow<InterceptState> = _state.asStateFlow()

    fun loadBalance() {
        viewModelScope.launch {
            val progress = progressRepository.getProgress() ?: UserProgress()
            _state.value = _state.value.copy(xpBalance = progress.totalXp)
        }
    }

    /**
     * Deducts [xpCost] XP from the user's balance.
     * Returns SUCCESS if sufficient XP, INSUFFICIENT otherwise.
     */
    fun payXp() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessing = true)

            val progress = progressRepository.getProgress() ?: UserProgress()
            val cost = _state.value.xpCost

            if (progress.totalXp < cost) {
                _state.value = _state.value.copy(
                    isProcessing = false,
                    result = PayResult.INSUFFICIENT
                )
                return@launch
            }

            // Deduct XP and recalculate level
            val newXp = progress.totalXp - cost
            val newLevel = (newXp / 500) + 1
            progressRepository.updateXpAndLevel(newXp, newLevel)

            _state.value = InterceptState(
                xpBalance = newXp,
                xpCost = cost,
                result = PayResult.SUCCESS,
                isProcessing = false
            )
        }
    }
}
