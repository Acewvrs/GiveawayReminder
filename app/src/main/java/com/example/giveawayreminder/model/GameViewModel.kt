package com.example.giveawayreminder.model

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.giveawayreminder.GameApplication
import com.example.giveawayreminder.data.Game
import com.example.giveawayreminder.data.GamesRepository
import com.example.giveawayreminder.data.UserPreferencesRepository
import com.example.giveawayreminder.data.WorkManagerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application, val gamesRepository: GamesRepository,
                    private val workManagerRepository: WorkManagerRepository, val userPreferencesRepository: UserPreferencesRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(GameUiState(displayDetail = false, selectedID = 0))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val notificationIntervalState: StateFlow<String> =
        userPreferencesRepository.notificationInterval.map {notificationInterval ->
            notificationInterval
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "Every Day"
        )

    val notificationHourState: StateFlow<String> =
        userPreferencesRepository.notificationHourOfDay.map {notificationInterval ->
            notificationInterval
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "12 am"
        )

    val updateIntervalState: StateFlow<String> =
        userPreferencesRepository.listUpdateInterval.map {updateInterval ->
            updateInterval
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = "6 hours"
        )

    val oldGameListState: StateFlow<String> =
        userPreferencesRepository.oldGameList.map { oldList ->
            oldList
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    // save instance of application
    private val app = application

    init {
        getPromotionsInfo()
        scheduleReminderNotification()
        scheduleGameListUpdate()
    }

    // TESTING ONLY
    fun sendNotification() {
        workManagerRepository.sendNotification()
    }

    // TESTING ONLY
    fun receiveGameData() {
        viewModelScope.launch {
            workManagerRepository.retrieveGameData()
        }
    }

    /**
     * Gets Mars photos information from the Mars API Retrofit service and updates the
     * [MarsPhoto] [List] [MutableList].
     */
    fun getPromotionsInfo() {
        viewModelScope.launch {
            _uiState.update {currentState ->
                currentState.copy(
                    games = gamesRepository.getGamePromotions(),
                    displayDetail = currentState.displayDetail,
                    selectedID = currentState.selectedID
                )
            }
        }
    }

    // NOTIFICATION SETTINGS
    /**
    hourString: hour of day in string e.g. 10 am, 8 pm, 12 am
    e.g. 10 pm -> 20
    **/
    private fun convertHourToMilitaryHour(hourString : String) : Int {
        val hourArray = hourString.split(" ").toTypedArray()
        val hourNumber = hourArray[0].toInt()
        val hourAmPm = hourArray[1]

        return if (hourNumber == 12 && hourAmPm == "am") 0
        else if (hourNumber == 12 && hourAmPm == "pm") 12
        else if (hourAmPm == "pm") { hourNumber + 12 }
        else { hourNumber }
    }

    /**
     * 3 possible options for intervalString: "Every Day", "2 Days", Or "7 Days"
     */
    private fun convertIntervalToHours(intervalString : String) : Long {
        val intervalArray = intervalString.split(" ").toTypedArray()
        val intervalNumber = intervalArray[0]

        return if (intervalNumber == "Every") 24
        else intervalNumber.toLong() * 24
    }

    /**
     * 3 possible options for intervalString: "6 hours", "12 Days", Or "18 Days"
     */
    private fun convertStringToInterval(intervalString : String) : Long {
        val intervalArray = intervalString.split(" ").toTypedArray()
        val intervalNumber = intervalArray[0]

        return intervalNumber.toLong()
    }

    private fun scheduleReminderNotification() {
        Log.d("time", "Reminder: " + convertIntervalToHours(notificationIntervalState.value).toString() + " " + convertHourToMilitaryHour(notificationHourState.value))
        workManagerRepository.setNotificationInterval(app, convertIntervalToHours(notificationIntervalState.value), convertHourToMilitaryHour(notificationHourState.value))
    }

    private fun scheduleGameListUpdate() {
        Log.d("time", "List: " + convertStringToInterval(updateIntervalState.value).toString())
        workManagerRepository.setListUpdateInterval(app, convertStringToInterval(updateIntervalState.value))
    }

    /**
     * This sets the default time at the first launch of the app
     */
    fun saveAndSetNotificationHour(hourOfDay: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveHourOfDay(hourOfDay)
            scheduleReminderNotification()
        }
    }

    fun saveAndSetNotificationInterval(intervalHour: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveNotificationInterval(intervalHour)
            scheduleReminderNotification()
        }
    }

    fun saveAndSetListUpdateInterval(intervalHour: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveUpdateInterval(intervalHour)
            scheduleGameListUpdate()
        }
    }

    // FACTORY METHOD
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameViewModel(
                    application = application,
                    gamesRepository = application.container.gamesRepository,
                    workManagerRepository = application.container.workManagerRepository,
                    userPreferencesRepository = application.userPreferencesRepository
                )
            }
        }
    }
}

/*
 * Data class containing various UI States for the game list screen
 */
data class GameUiState(
    val games: List<Game> = emptyList(),
    val displayDetail: Boolean = false,
    val selectedID: Int = 0,
)

