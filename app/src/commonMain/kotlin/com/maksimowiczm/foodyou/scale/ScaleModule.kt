package com.maksimowiczm.foodyou.scale

import com.maksimowiczm.foodyou.scale.domain.scaleDomainModule
import com.maksimowiczm.foodyou.scale.infrastructure.scaleInfrastructureModule
import org.koin.dsl.module

val scaleModule = module {
    scaleDomainModule()
    scaleInfrastructureModule()
}
