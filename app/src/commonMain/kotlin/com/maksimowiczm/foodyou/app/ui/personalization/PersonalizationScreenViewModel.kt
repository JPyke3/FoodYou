package com.maksimowiczm.foodyou.app.ui.personalization

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.scale.infrastructure.ScalePreferencesKeys
import com.maksimowiczm.foodyou.settings.domain.entity.EnergyFormat
import com.maksimowiczm.foodyou.settings.domain.entity.Settings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class PersonalizationScreenViewModel(
    private val settingsRepository: UserPreferencesRepository<Settings>,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    private val _secureScreen = settingsRepository.observe().map { it.secureScreen }
    val secureScreen =
        _secureScreen.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000),
            initialValue = runBlocking { _secureScreen.first() },
        )

    fun toggleSecureScreen(newState: Boolean) {
        viewModelScope.launch { settingsRepository.update { copy(secureScreen = newState) } }
    }

    private val _energyUnit = settingsRepository.observe().map { it.energyFormat }
    val energyUnit =
        _energyUnit.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000),
            initialValue = runBlocking { _energyUnit.first() },
        )

    fun setEnergyFormat(format: EnergyFormat) {
        viewModelScope.launch { settingsRepository.update { copy(energyFormat = format) } }
    }

    private val _scaleEnabled = dataStore.data.map { it[ScalePreferencesKeys.SCALE_ENABLED] ?: false }
    val scaleEnabled =
        _scaleEnabled.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000),
            initialValue = runBlocking { _scaleEnabled.first() },
        )

    fun setScaleEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[ScalePreferencesKeys.SCALE_ENABLED] = enabled }
        }
    }
}
