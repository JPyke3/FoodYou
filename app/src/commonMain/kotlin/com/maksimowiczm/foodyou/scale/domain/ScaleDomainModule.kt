package com.maksimowiczm.foodyou.scale.domain

import com.maksimowiczm.foodyou.scale.domain.qnks.QnKsProtocol
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind

fun Module.scaleDomainModule() {
    singleOf(::QnKsProtocol).bind<ScaleProtocol>()
}
