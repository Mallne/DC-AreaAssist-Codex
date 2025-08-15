package cloud.mallne.dicentra.synapse.statics

import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import cloud.mallne.dicentra.aviator.koas.Operation
import cloud.mallne.dicentra.aviator.koas.PathItem
import cloud.mallne.dicentra.aviator.koas.info.Info
import cloud.mallne.dicentra.aviator.koas.info.License
import cloud.mallne.dicentra.aviator.koas.servers.Server
import cloud.mallne.dicentra.aviator.model.ServiceLocator

object APIService {
    val discovery = OpenAPI(
        extensions = mapOf(
            AviatorExtensionSpec.Version.key to Serialization().parseToJsonElement(
                AviatorExtensionSpec.SpecVersion
            )
        ),
        servers = listOf(
            Server(
                "https://cloud.mallne.cloud/api/areaassist/relay"
            ),
        ),
        info = Info(
            title = "DiCentra AreaAssist Discovery",
            description = "The an exposed Aviator Discovery Endpoint to update the config",
            version = AviatorExtensionSpec.SpecVersion,
            license = License(
                "Apache 2.0",
                identifier = "DiCentra AreaAssist Discovery",
                url = "https://github.com/Mallne/DC-AreaAssist-Server"
            )
        ),
        paths = mapOf(
            "/discovery" to PathItem(
                get = Operation(
                    extensions = mapOf(
                        AviatorExtensionSpec.ServiceLocator.O.key to
                                Services.DISCOVERY_SERVICE.locator(
                                    ServiceMethods.GATHER
                                ).usable(),
                        AviatorExtensionSpec.ServiceOptions.O.key to
                                InflatedServiceOptions.empty.usable()
                    ),
                )
            )
        )
    )
    val mapLight = OpenAPI(
        extensions = mapOf(
            AviatorExtensionSpec.Version.key to Serialization().parseToJsonElement(
                AviatorExtensionSpec.SpecVersion
            )
        ),
        servers = listOf(
            Server(
                "https://sgx.geodatenzentrum.de"
            ),
        ),
        info = Info(
            title = "Basemap Light",
            description = "Official German Vector Map",
            version = AviatorExtensionSpec.SpecVersion,
            license = License(
                "basemap.de / BKG | Datenquellen: © GeoBasis-DE",
                identifier = "Basemap Light"
            )
        ),
        paths = mapOf(
            "/gdz_basemapde_vektor/styles/bm_web_top.json" to PathItem(
                get = Operation(
                    extensions = mapOf(
                        AviatorExtensionSpec.ServiceLocator.O.key to Services.MAPLAYER_LIGHT.locator(
                            ServiceMethods.GATHER
                        ).usable(),
                        AviatorExtensionSpec.ServiceOptions.O.key to InflatedServiceOptions.empty.usable()
                    ),
                )
            )
        )
    )
    val mapDark = OpenAPI(
        extensions = mapOf(
            AviatorExtensionSpec.Version.key to Serialization().parseToJsonElement(
                AviatorExtensionSpec.SpecVersion
            )
        ),
        servers = listOf(
            Server(
                "https://basemap.de"
            ),
        ),
        info = Info(
            title = "Basemap Dark",
            description = "Official German Vector Map",
            version = AviatorExtensionSpec.SpecVersion,
            license = License(
                "basemap.de / BKG | Datenquellen: © GeoBasis-DE",
                identifier = "Basemap Dark"
            )
        ),
        paths = mapOf(
            "/data/produkte/web_vektor/styles/bm_web_drk.json" to PathItem(
                get = Operation(
                    extensions = mapOf(
                        AviatorExtensionSpec.ServiceLocator.O.key to Services.MAPLAYER_DARK.locator(
                            ServiceMethods.GATHER
                        ).usable(),
                        AviatorExtensionSpec.ServiceOptions.O.key to InflatedServiceOptions.empty.usable()
                    ),
                )
            )
        )
    )
    val esri = OpenAPI(
        extensions = mapOf(
            AviatorExtensionSpec.Version.key to Serialization().parseToJsonElement(
                AviatorExtensionSpec.SpecVersion
            )
        ),
        servers = listOf(
            Server(
                "https://services2.arcgis.com/jUpNdisbWqRpMo35/arcgis/rest/services"
            )
        ),
        info = Info(
            title = "Esri",
            description = "Location based Open Data for Germany",
            version = AviatorExtensionSpec.SpecVersion
        ),
        paths = mapOf(
            "/thueringen_flstck/FeatureServer/0/query" to ParcelConstants.DE_TH,
            "/Flurstuecke_Sachsen/FeatureServer/0/query" to ParcelConstants.DE_SN,
            "/Flurstücke_Brandenburg/FeatureServer/0/query" to ParcelConstants.DE_BB,
            "/flstk_hessen/FeatureServer/0/query" to ParcelConstants.DE_HE,
            "/Flurstuecke_Hamburg/FeatureServer/0/query" to ParcelConstants.DE_HH,
            "/flstk_nrw/FeatureServer/0/query" to ParcelConstants.DE_NW,
            "/Flurstuecke_Sachsen_Anhalt/FeatureServer/0/query" to ParcelConstants.DE_ST,
            "/Flurst_Berlin/FeatureServer/0/query" to ParcelConstants.DE_BE,
            "/NDS_Flurstuecke/FeatureServer/0/query" to ParcelConstants.DE_NI,
        ),
    )
    val brightSky = OpenAPI(
        extensions = mapOf(
            AviatorExtensionSpec.Version.key to Serialization().parseToJsonElement(
                AviatorExtensionSpec.SpecVersion
            )
        ),
        servers = listOf(
            Server(
                "https://api.brightsky.dev"
            ),
        ),
        info = Info(
            title = "Brightsky",
            description = "Open Source API for German Weather Service",
            version = AviatorExtensionSpec.SpecVersion,
            license = License(
                identifier = "Brightsky",
                name = "MIT License",
                url = "https://github.com/jdemaeyer/brightsky/?tab=MIT-1-ov-file#readme"
            )
        ),
        paths = mapOf(
            "/alerts" to WeatherConstants.WEATHER_ALERTS,
            "/current_weather" to WeatherConstants.CURRENT_WEATHER,
            "/radar" to WeatherConstants.WEATHER_RADAR,
            "/sources" to WeatherConstants.WEATHER_SOURCES,
            "/synop" to WeatherConstants.WEATHER_SYNOP,
            "/weather" to WeatherConstants.WEATHER,
        )
    )
    val apis = listOf(
        discovery,
        mapLight,
        mapDark,
        esri,
        brightSky,
    )

    enum class Services(
        private val serviceLocator: String,
    ) {
        DISCOVERY_SERVICE("&.dicentraDiscoveryAgent"),
        WEATHER_SERVICE_CURRENT("&.scribe.weatherService.current"),
        WEATHER_SERVICE_WARNING("&.scribe.weatherService.warning"),
        WEATHER_SERVICE_FORECAST("&.scribe.weatherService.forecast"),
        MAPLAYER_DARK("&.surveyor.map.dark"),
        MAPLAYER_LIGHT("&.surveyor.map.light"),
        PARCEL_SERVICE("&.curator.parcelService");

        fun locator(flavour: ServiceMethods): ServiceLocator {
            return ServiceLocator(
                this.serviceLocator,
                flavour
            )
        }
    }
}