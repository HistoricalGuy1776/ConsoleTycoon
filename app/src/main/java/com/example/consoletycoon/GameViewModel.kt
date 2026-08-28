package com.example.consoletycoon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Random

data class GameState(
    val money: Int = 50000,
    val revenuePerDay: Int = 1,
    val activeConsoleName: String = "None",
    val date: GameDate = GameDate(1, 11, 2001),
    val isConsoleActive: Boolean = false,
    val consoleAgeDays: Int = 0,
    val consoleLifespanLimit: Int = 0,
    val isSubActive: Boolean = false,
    val subPrice: Int = 10,
    val subscribers: Int = 0,
    val subMonthlyRevenue: Int = 0,
    val includeCloudGaming: Boolean = false,
    val includeDayOneExclusives: Boolean = false,
    val dayTickCounter: Int = 0,
    val logs: List<String> = emptyList()
)

data class GameDate(val day: Int, val month: Int, val year: Int) {
    override fun toString(): String = "$day/$month/$year"
}

class GameViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    private val rand = Random()

    init {
        startGameLoop()
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                tick()
            }
        }
    }

    private fun tick() {
        _uiState.update { state ->
            var nextMoney = state.money + state.revenuePerDay
            var nextIsConsoleActive = state.isConsoleActive
            var nextRevenuePerDay = state.revenuePerDay
            var nextActiveConsoleName = state.activeConsoleName
            var nextConsoleAgeDays = state.consoleAgeDays
            val nextLogs = state.logs.toMutableList()

            if (state.isConsoleActive) {
                nextMoney += state.revenuePerDay
                nextConsoleAgeDays++
                if (nextConsoleAgeDays >= state.consoleLifespanLimit) {
                    nextIsConsoleActive = false
                    nextRevenuePerDay = 0
                    val retiredName = state.activeConsoleName
                    nextActiveConsoleName = "None (Retired)"
                    nextLogs.add("$retiredName has been retired.")
                }
            }

            var nextSubscribers = state.subscribers
            var nextSubMonthlyRevenue = state.subMonthlyRevenue
            var nextDayTickCounter = state.dayTickCounter + 1

            if (state.isSubActive && nextDayTickCounter >= 30) {
                nextDayTickCounter = 0
                
                var featuresValue = 10
                var maintenanceCosts = 100

                if (state.includeCloudGaming) {
                    featuresValue += 8
                    maintenanceCosts += 80
                }
                if (state.includeDayOneExclusives) {
                    featuresValue += 15
                    maintenanceCosts += 2500
                }

                val priceFactor = 20 - state.subPrice
                val subscriberChurn = (featuresValue + priceFactor) * 45
                
                nextSubscribers += subscriberChurn
                if (nextSubscribers < 0) nextSubscribers = 0
                
                nextSubMonthlyRevenue = (nextSubscribers * state.subPrice) - maintenanceCosts
                nextMoney += nextSubMonthlyRevenue
            }

            val nextDate = incrementDate(state.date)
            
            state.copy(
                money = nextMoney,
                activeConsoleName = nextActiveConsoleName,
                revenuePerDay = nextRevenuePerDay,
                isConsoleActive = nextIsConsoleActive,
                consoleAgeDays = nextConsoleAgeDays,
                date = nextDate,
                subscribers = nextSubscribers,
                subMonthlyRevenue = nextSubMonthlyRevenue,
                dayTickCounter = nextDayTickCounter,
                logs = nextLogs.takeLast(5)
            )
        }
    }

    private fun incrementDate(date: GameDate): GameDate {
        var day = date.day + 1
        var month = date.month
        var year = date.year

        if (day > 31) {
            day = 1
            month++
            if (month > 12) {
                month = 1
                year++
            }
        }
        return GameDate(day, month, year)
    }

    fun launchConsole(name: String, cpuIndex: Int, ramIndex: Int, osIndex: Int) {
        var developmentCost = 0
        var baseRevenueGeneration = 0

        when (cpuIndex) {
            0 -> { developmentCost += 5000; baseRevenueGeneration += 150 }
            1 -> { developmentCost += 12000; baseRevenueGeneration += 400 }
            else -> { developmentCost += 30000; baseRevenueGeneration += 1200 }
        }

        when (ramIndex) {
            0 -> { developmentCost += 2000; baseRevenueGeneration += 50 }
            1 -> { developmentCost += 8000; baseRevenueGeneration += 250 }
            else -> { developmentCost += 20000; baseRevenueGeneration += 800 }
        }

        when (osIndex) {
            0 -> { developmentCost += 3000; baseRevenueGeneration += 100 }
            1 -> { developmentCost += 500; baseRevenueGeneration += 25 }
            else -> { developmentCost += 15000; baseRevenueGeneration += 600 }
        }

        if (_uiState.value.money >= developmentCost) {
            _uiState.update { state ->
                state.copy(
                    money = state.money - developmentCost,
                    activeConsoleName = name,
                    revenuePerDay = state.revenuePerDay + baseRevenueGeneration,
                    consoleAgeDays = 0,
                    isConsoleActive = true,
                    consoleLifespanLimit = rand.nextInt(63) + 155
                )
            }
        }
    }

    fun launchSubscriptionService() {
        if (!_uiState.value.isSubActive && _uiState.value.money >= 10000) {
            _uiState.update { state ->
                state.copy(
                    money = state.money - 10000,
                    isSubActive = true,
                    subscribers = 500
                )
            }
        }
    }

    fun updateSubPrice(price: Int) {
        _uiState.update { it.copy(subPrice = price) }
    }

    fun updateCloudGaming(enabled: Boolean) {
        _uiState.update { it.copy(includeCloudGaming = enabled) }
    }

    fun updateDayOneExclusives(enabled: Boolean) {
        _uiState.update { it.copy(includeDayOneExclusives = enabled) }
    }
}
