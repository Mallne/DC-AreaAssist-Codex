package cloud.mallne.dicentra.areaassist.codex.statics

import cloud.mallne.dicentra.areaassist.codex.model.WeatherServiceOptions
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.koas.Operation
import cloud.mallne.dicentra.aviator.koas.PathItem
import cloud.mallne.dicentra.aviator.koas.extensions.ReferenceOr
import cloud.mallne.dicentra.aviator.koas.io.Schema
import cloud.mallne.dicentra.aviator.koas.parameters.Parameter


object WeatherConstants {
    object Path {
        object Parameters {
            const val LAT = "lat"
            const val LON = "lon"
            const val TZ = "tz"
            const val WARN_CELL_ID = "warn_cell_id"
            const val DWD_STATION_ID = "dwd_station_id"
            const val WMO_STATION_ID = "wmo_station_id"
            const val SOURCE_ID = "source_id"
            const val MAX_DIST = "max_dist"
            const val UNITS = "units"
            const val DATE = "date"
            const val LAST_DATE = "last_date"
            const val BBOX = "bbox"
            const val FORMAT = "format"
            const val DISTANCE = "distance"
        }
    }

    val WEATHER_ALERTS = PathItem(
        summary = "Weather Alerts",
        get = Operation(
            operationId = "WeatherAlerts",
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to APIService.Services.WEATHER_SERVICE_WARNING.locator(
                    ServiceMethods.GATHER
                ).usable(),
                AviatorExtensionSpec.ServiceOptions.O.key to WeatherServiceOptions(
                    serviceType = WeatherServiceOptions.Companion.ServiceType.BRIGHTSKY
                ).usable(),
            ),
            parameters = listOf(
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.LAT,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.LON,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.WARN_CELL_ID,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.TZ,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    )
                )
            )
        )
    )
    val CURRENT_WEATHER = PathItem(
        summary = "Current Weather",
        get = Operation(
            operationId = "CurrentSources",
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to APIService.Services.WEATHER_SERVICE_CURRENT.locator(
                    ServiceMethods.GATHER
                ).usable(),
                AviatorExtensionSpec.ServiceOptions.O.key to WeatherServiceOptions(
                    serviceType = WeatherServiceOptions.Companion.ServiceType.BRIGHTSKY
                ).usable(),
            ),
            parameters = listOf(
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.LAT,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.LON,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.DWD_STATION_ID,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.WMO_STATION_ID,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.SOURCE_ID,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.MAX_DIST,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.TZ,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.UNITS,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
            )
        )
    )
    val WEATHER_RADAR = PathItem(
        summary = "Weather Radar",
        get = Operation(
            operationId = "WeatherRadar",
            extensions = mapOf(
                AviatorExtensionSpec.ServiceOptions.O.key to WeatherServiceOptions(
                    serviceType = WeatherServiceOptions.Companion.ServiceType.BRIGHTSKY
                ).usable(),
            ),
            parameters = listOf(
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.DATE,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.LAST_DATE,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.BBOX,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.LAT,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.LON,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.DISTANCE,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.TZ,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.FORMAT,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(
                            Schema(
                                type = Schema.Type.Basic.String
                            )
                        )
                    ),
                )
            )
        )
    )
    val WEATHER_SOURCES = PathItem(
        summary = "Weather Sources",
        get = Operation(
            operationId = "WeatherSources",
            extensions = mapOf(
                AviatorExtensionSpec.ServiceOptions.O.key to WeatherServiceOptions(
                    serviceType = WeatherServiceOptions.Companion.ServiceType.BRIGHTSKY
                ).usable()
            ),
            parameters = listOf(
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.LAT,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.LON,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.DWD_STATION_ID,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.WMO_STATION_ID,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.SOURCE_ID,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.MAX_DIST,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                )
            )
        )
    )
    val WEATHER_SYNOP = PathItem(
        summary = "Weather Synop",
        get = Operation(
            operationId = "WeatherSynop",
            extensions = mapOf(
                AviatorExtensionSpec.ServiceOptions.O.key to WeatherServiceOptions(
                    serviceType = WeatherServiceOptions.Companion.ServiceType.BRIGHTSKY
                ).usable(),
            ),
            parameters = listOf(
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.DATE,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.LAST_DATE,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.DWD_STATION_ID,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.WMO_STATION_ID,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.SOURCE_ID,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.TZ,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.UNITS,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                )
            )
        )
    )
    val WEATHER = PathItem(
        summary = "Weather",
        get = Operation(
            operationId = "WeatherForecast",
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to APIService.Services.WEATHER_SERVICE_FORECAST.locator(
                    ServiceMethods.GATHER
                ).usable(),
                AviatorExtensionSpec.ServiceOptions.O.key to WeatherServiceOptions(
                    serviceType = WeatherServiceOptions.Companion.ServiceType.BRIGHTSKY
                ).usable()
            ),
            parameters = listOf(
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.DATE,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.LAST_DATE,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.LAT,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.LON,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.DWD_STATION_ID,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.WMO_STATION_ID,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.SOURCE_ID,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.MAX_DIST,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.TZ,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                ),
                ReferenceOr.value(
                    Parameter(
                        name = Path.Parameters.UNITS,
                        input = Parameter.Input.Query,
                        schema = ReferenceOr.value(Schema(type = Schema.Type.Basic.String))
                    ),
                )
            )
        )
    )
}