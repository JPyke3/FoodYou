package com.maksimowiczm.foodyou.scale.infrastructure

import androidx.datastore.preferences.core.booleanPreferencesKey

internal object ScalePreferencesKeys {
    val SCALE_ENABLED = booleanPreferencesKey("scale:enabled")
}
