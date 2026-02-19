package com.maksimowiczm.foodyou.app.ui.personalization

import androidx.compose.runtime.Composable

@Composable
internal expect fun rememberSmartScaleToggle(onToggle: (Boolean) -> Unit): (Boolean) -> Unit
