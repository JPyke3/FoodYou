package com.maksimowiczm.foodyou.app.ui.personalization

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.maksimowiczm.foodyou.common.infrastructure.koin.userPreferencesRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

fun Module.personalization() {
    viewModel { PersonalizationScreenViewModel(settingsRepository = userPreferencesRepository(), dataStore = get<DataStore<Preferences>>()) }
    viewModel {
        PersonalizeNutritionFactsViewModel(settingsRepository = userPreferencesRepository())
    }
}
