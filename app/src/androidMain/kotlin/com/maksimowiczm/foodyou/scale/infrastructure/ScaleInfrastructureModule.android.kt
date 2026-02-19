package com.maksimowiczm.foodyou.scale.infrastructure

import com.maksimowiczm.foodyou.scale.domain.ScaleRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual fun Module.scaleInfrastructureModule() {
    single<ScaleRepository> {
        AndroidBleScaleManager(context = androidContext(), protocol = get())
    }
}
