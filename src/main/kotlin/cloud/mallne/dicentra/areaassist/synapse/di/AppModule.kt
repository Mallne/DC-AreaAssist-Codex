package cloud.mallne.dicentra.areaassist.synapse.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan("cloud.mallne.dicentra.areaassist.synapse.di", "cloud.mallne.dicentra.areaassist.synapse.services")
class AppModule