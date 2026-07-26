package com.manage.health.healthtrackerapplication.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.GoogleFitService
import com.manage.health.healthtrackerapplication.data.model.HealthData
import com.manage.health.healthtrackerapplication.data.model.UserGoals
import com.manage.health.healthtrackerapplication.data.repository.HealthRepository
import com.manage.health.healthtrackerapplication.data.service.WearableDeviceService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HealthViewModel @Inject constructor(
    private val healthRepository: HealthRepository,
    private val googleFitService: GoogleFitService,
    private val wearableDeviceService: WearableDeviceService
) : ViewModel() {

    private val _currentHealthData = MutableStateFlow<HealthData?>(null)
    val currentHealthData: StateFlow<HealthData?> = _currentHealthData.asStateFlow()

    private val _todayDate = MutableStateFlow(getCurrentDate())

    // Reactive Flow for Today's Health Data
    val todayHealthData: StateFlow<HealthData?> = _todayDate
        .flatMapLatest { date ->
            healthRepository.getHealthDateByDataFlow(date)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

    private val _userGoals = MutableStateFlow<UserGoals?>(null)
    val userGoals: StateFlow<UserGoals?> = _userGoals.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Wearable device integration
    private val _connectedWearables = MutableStateFlow<List<String>>(emptyList())
    val connectedWearables: StateFlow<List<String>> = _connectedWearables.asStateFlow()

    private val _isWearableSyncInProgress = MutableStateFlow(false)
    val isWearableSyncInProgress: StateFlow<Boolean> = _isWearableSyncInProgress.asStateFlow()

    init {
        loadUserGoals()
        loadTodayHealthData()
        checkWearableDevices()
    }

    private fun loadUserGoals() {
        viewModelScope.launch {
            healthRepository.getUserGoals().collect { goals ->
                _userGoals.value = goals ?: UserGoals()
            }
        }
    }

    private fun loadTodayHealthData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val today = getCurrentDate()
                val healthData = healthRepository.getHealthDataByDate(today)

                if (healthData == null) {
                    val nowString = LocalDateTime.now().toString()

                    val newHealthData = HealthData(
                        date = today,
                        createdAt = nowString,
                        updatedAt = nowString
                    )

                    healthRepository.insertOrUpdateHealthData(newHealthData)
                    _currentHealthData.value = newHealthData
                } else {
                    _currentHealthData.value = healthData
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load health data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addWaterIntake(amount: Int) {
        viewModelScope.launch {
            try {
                healthRepository.addWaterIntake(amount)
                refreshData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add water intake: ${e.message}"
            }
        }
    }

    fun addSteps(steps: Int) {
        viewModelScope.launch {
            try {
                healthRepository.addSteps(steps)
                refreshData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add steps: ${e.message}"
            }
        }
    }

    fun addSleep(sleepStart: String, sleepEnd: String, duration: Float, quality: Int = 0) {
        viewModelScope.launch {
            try {
                healthRepository.addSleepLog(sleepStart, sleepEnd, duration, quality)
                refreshData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add sleep log: ${e.message}"
            }
        }
    }

    fun addRunningActivity(steps: Int, distance: Float, duration: Int, calories: Int) {
        viewModelScope.launch {
            try {
                healthRepository.addRunningActivity(steps, distance, duration, calories)
                refreshData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add running activity: ${e.message}"
            }
        }
    }

    fun updateUserGoals(goals: UserGoals) {
        viewModelScope.launch {
            try {
                healthRepository.updateUserGoals(goals)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update goals: ${e.message}"
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            try {
                _todayDate.value = getCurrentDate()
                loadTodayHealthData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to refresh data: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    suspend fun syncWithGoogleFit() {
        try {
            _isLoading.value = true

            if (googleFitService.isGoogleFitAvailable()) {
                val steps = googleFitService.getTodaySteps()
                val distance = googleFitService.getTodayDistance()
                val calories = googleFitService.getTodayCalories()
                val heartRate = googleFitService.getLatestHeartRate()

                val currentData = _currentHealthData.value ?: HealthData(
                    date = getCurrentDate(),
                    steps = 0,
                    waterIntake = 0,
                    sleepHours = 0f,
                    distance = 0f,
                    caloriesBurned = 0,
                    heartRate = 0,
                    healthScore = 0,
                    userId = ""
                )

                val updatedData = currentData.copy(
                    steps = steps,
                    distance = distance,
                    caloriesBurned = calories,
                    heartRate = heartRate
                )

                healthRepository.insertOrUpdateHealthData(updatedData)
                _currentHealthData.value = updatedData
            } else {
                _errorMessage.value = "Google Fit is not available. Please check your connection and permissions."
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to sync with Google Fit: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun isGoogleFitAvailable(): Boolean {
        return googleFitService.isGoogleFitAvailable()
    }

    suspend fun getWeeklyStepsFromGoogleFit(): List<Int> {
        return try {
            googleFitService.getWeeklyStepHistory()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to get weekly steps: ${e.message}"
            emptyList()
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                healthRepository.resetAllData()
                loadTodayHealthData()
                loadUserGoals()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reset data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun checkWearableDevices() {
        viewModelScope.launch {
            try {
                val devices = wearableDeviceService.getConnectedDevices()
                _connectedWearables.value = devices
            } catch (e: Exception) {
                _errorMessage.value = "Failed to check wearable devices: ${e.message}"
            }
        }
    }

    private fun getCurrentDate(): String {
        return LocalDate.now().toString()
    }


    // refresh wearable devices

    fun refreshWearableDevices() {
        checkWearableDevices()
    }

    fun syncWearableData() {
        viewModelScope.launch {
            try {
                _isWearableSyncInProgress.value = true
                _errorMessage.value = null

                val connectedDevices = wearableDeviceService.getConnectedDevices()

                if (connectedDevices.isNotEmpty()) { // ✅ Fixed logic: Check if NOT empty
                    val syncSuccess = wearableDeviceService.syncWearableData()

                    if (syncSuccess) {
                        val wearableData = wearableDeviceService.getAggregatedWearableData()

                        val currentData = _currentHealthData.value ?: HealthData(
                            date = getCurrentDate(),
                            steps = 0,
                            waterIntake = 0,
                            sleepHours = 0f,
                            distance = 0f,
                            caloriesBurned = 0,
                            heartRate = 0,
                            healthScore = 0,
                            userId = ""
                        )

                        if (wearableData.isNotEmpty()) {
                            val updatedData = currentData.copy(
                                steps = (wearableData["steps"] as? Number)?.toInt()
                                    ?: currentData.steps,
                                caloriesBurned = (wearableData["calories"] as? Number)?.toInt()
                                    ?: currentData.caloriesBurned,
                                heartRate = (wearableData["heartRate"] as? Number)?.toInt()
                                    ?: currentData.heartRate,
                                distance = (wearableData["distance"] as? Number)?.toFloat()
                                    ?: currentData.distance,
                                sleepHours = (wearableData["sleepHours"] as? Number)?.toFloat()
                                    ?: currentData.sleepHours
                            )

                            healthRepository.insertOrUpdateHealthData(updatedData)
                            _currentHealthData.value = updatedData
                        } else {
                            _errorMessage.value = "Sync completed, but no data was received."
                        }
                    } else {
                        _errorMessage.value = "Sync failed. Please check device connection."
                    }
                } else {
                    _errorMessage.value =
                        "No wearable devices found. Please connect a device first."
                }

                checkWearableDevices()

            } catch (e: Exception) {
                _errorMessage.value = "Failed to sync wearable data: ${e.message}"
            } finally {
                _isWearableSyncInProgress.value = false
            }
        }
    }

    suspend fun hasConnectedWearable(): Boolean{
        return try {
            wearableDeviceService.hasConnectedWearables()
        }catch (e: Exception){
            _errorMessage.value = "Failed to Check wearable connections :${e.message}"
            false
        }
    }

    suspend fun getSamsungHealthData(): Map<String, Any>{
        return try {

            wearableDeviceService.getSamsungHealthData()
        }catch (e: Exception){
            _errorMessage.value = "Failed to get Samsung Healh Data :${e.message}"
            emptyMap()
        }
    }

    suspend fun getWearOsData(): Map<String, Any>{
        return try {
            wearableDeviceService.getWearOSData()
        }catch (e: Exception){
            _errorMessage.value = "Failed tp get Wear Os Data:${e.message}"
            emptyMap()
        }
    }

    suspend fun isSamsungHealthAvailable(): Boolean{
        return try {
            wearableDeviceService.isSamsungHealthAvailable()
        }catch (e: Exception){
            _errorMessage.value = "Failed To Check Samsung Health ava;iablity :${e.message}"
            false
        }
    }


    suspend fun isWearOsConnected(): Boolean{
        return try {
            wearableDeviceService.isWearOSConnected()
        }catch (e: Exception){
            _errorMessage.value = "Failed to Check Wear os Connection :${e.message}"
            false
        }
    }

    suspend fun getConnectionStatus(): Map<String, Any> {
        return try {
            val googleFitAvailable = googleFitService.isGoogleFitAvailable()
            val wearableConnected = wearableDeviceService.hasConnectedWearables()
            val connectedDevices = wearableDeviceService.getConnectedDevices()
            val googleFitDataSources = googleFitService.getAvailableDataSources()

            mapOf(
                "googleFit" to mapOf(
                    "available" to googleFitAvailable,
                    "dataSources" to googleFitDataSources
                ),
                "wearables" to mapOf(
                    "connected" to wearableConnected,
                    "devices" to connectedDevices
                ),
                "lastChecked" to System.currentTimeMillis()
            )
        } catch (e: Exception) {
            _errorMessage.value = "Failed to get connection status: ${e.message}"
            emptyMap()
        }
    }

}