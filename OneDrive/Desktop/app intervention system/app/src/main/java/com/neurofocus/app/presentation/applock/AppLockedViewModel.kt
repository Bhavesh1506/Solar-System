package com.neurofocus.app.presentation.applock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neurofocus.app.domain.repository.AppBlockRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AppLockedViewModel – Handles the XP transaction for unlocking blocked apps.
 *
 * Manages the "Earn Your Time" payment flow:
 * 1. Loads the user's current XP balance
 * 2. Processes the "Pay 50 XP" action
 * 3. Creates a 15-minute unlock window in the database
 * 4. Reports success/failure back to the Activity
 */
@HiltViewModel
class AppLockedViewModel @Inject constructor(
    private val appBlockRepository: AppBlockRepository
) : ViewModel() {

    data class LockScreenState(
        val xpBalance: Int = 0,
        val xpCost: Int = 50,
        val unlockMinutes: Int = 15,
        val unlockResult: UnlockResult = UnlockResult.IDLE,
        val isProcessing: Boolean = false
    )

    enum class UnlockResult {
        IDLE,          // Initial state
        SUCCESS,       // XP deducted, app unlocked
        INSUFFICIENT   // Not enough XP
    }

    private val _state = MutableStateFlow(LockScreenState())
    val state: StateFlow<LockScreenState> = _state.asStateFlow()

    /** Loads the current XP balance from the database */
    fun loadXpBalance() {
        viewModelScope.launch {
            val balance = appBlockRepository.getXpBalance()
            _state.value = _state.value.copy(xpBalance = balance)
        }
    }

    /**
     * Attempts to unlock the given app by paying XP.
     *
     * @param packageName The blocked app's package name
     * @return true if the unlock succeeded
     */
    fun payToUnlock(packageName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isProcessing = true)

            val success = appBlockRepository.unlockApp(
                packageName = packageName,
                xpCost = _state.value.xpCost,
                durationMinutes = _state.value.unlockMinutes
            )

            val newBalance = appBlockRepository.getXpBalance()

            _state.value = _state.value.copy(
                isProcessing = false,
                xpBalance = newBalance,
                unlockResult = if (success) UnlockResult.SUCCESS else UnlockResult.INSUFFICIENT
            )
        }
    }
}
