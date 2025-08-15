package cloud.mallne.dicentra.synapse.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan("cloud.mallne.dicentra.synapse.di", "cloud.mallne.dicentra.synapse.service")
class AppModule