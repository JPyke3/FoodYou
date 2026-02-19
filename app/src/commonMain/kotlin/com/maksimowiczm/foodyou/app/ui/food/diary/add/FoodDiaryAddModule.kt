package com.maksimowiczm.foodyou.app.ui.food.diary.add

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.maksimowiczm.foodyou.food.domain.entity.FoodId
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel

fun Module.foodDiaryAdd() {
    viewModel {
        AddEntryViewModel(
            createFoodDiaryEntryUseCase = get(),
            observeFoodUseCase = get(),
            foodHistoryRepository = get(),
            deleteFoodUseCase = get(),
            observeMeasurementSuggestionsUseCase = get(),
            mealRepository = get(),
            dateProvider = get(),
            eventBus = get(),
            scaleRepository = get(),
            dataStore = get<DataStore<Preferences>>(),
            foodId = it.get<FoodId>(),
        )
    }
}
