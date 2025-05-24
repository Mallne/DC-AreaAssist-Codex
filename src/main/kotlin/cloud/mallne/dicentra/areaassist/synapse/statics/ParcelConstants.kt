package cloud.mallne.dicentra.areaassist.synapse.statics

import cloud.mallne.dicentra.areaassist.synapse.model.ParcelServiceOptions
import cloud.mallne.dicentra.areaassist.synapse.statics.bundeslaender.Bundesland
import cloud.mallne.dicentra.aviator.core.AviatorExtensionSpec
import cloud.mallne.dicentra.aviator.core.ServiceMethods
import cloud.mallne.dicentra.aviator.koas.Operation
import cloud.mallne.dicentra.aviator.koas.PathItem
import cloud.mallne.dicentra.aviator.koas.extensions.ReferenceOr
import cloud.mallne.dicentra.aviator.koas.info.License
import cloud.mallne.dicentra.aviator.koas.io.Schema
import cloud.mallne.dicentra.aviator.koas.parameters.Parameter


object ParcelConstants {
    val locator = APIService.Services.PARCEL_SERVICE.locator(
        ServiceMethods.GATHER
    )

    object DefaultKeys {
        val PLOT = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "plot",
            translations = ParcelServiceOptions.Companion.PreDefined.PLOT.toTranslatable(),
            hideInUI = true, //The Plot gets handled differently
            icon = ParcelServiceOptions.Companion.KeyIcon.ShareLocation
        )
        val PLOT_NUMERATOR = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "plot.numerator",
            translations = ParcelServiceOptions.Companion.PreDefined.PLOT_NUMERATOR.toTranslatable(),
            hideInUI = true, //The Plot gets handled differently
            icon = ParcelServiceOptions.Companion.KeyIcon.ShareLocation
        ) //plotNumerator/Nenner
        val PLOT_SEPARATOR = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "plot.separator",
            translations = ParcelServiceOptions.Companion.PreDefined.PLOT_SEPARATOR.toTranslatable(),
            hideInUI = true, //The Plot gets handled differently
            icon = ParcelServiceOptions.Companion.KeyIcon.ShareLocation
        ) //plotSeparator/Teiler
        val PLOT_DENOMINATOR = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "plot.denominator",
            translations = ParcelServiceOptions.Companion.PreDefined.PLOT_DENOMINATOR.toTranslatable(),
            hideInUI = true, //The Plot gets handled differently
            icon = ParcelServiceOptions.Companion.KeyIcon.ShareLocation
        )//plotDenominator/Zähler
        val AREA = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "area",
            type = ParcelServiceOptions.Companion.KeyType.Number,
            translations = ParcelServiceOptions.Companion.PreDefined.AREA.toTranslatable(),
            format = ParcelServiceOptions.Companion.KeyFormat(
                unit = ParcelServiceOptions.Companion.KeyFormat.Companion.UnitFormat(
                    unitSystem = SISystems.AREA,
                    ingestUnit = "squareMeters"
                )
            ),
            icon = ParcelServiceOptions.Companion.KeyIcon.ZoomOutMap
        ) //area
        val DISTRICT = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "district",
            translations = ParcelServiceOptions.Companion.PreDefined.DISTRICT.toTranslatable(),
            icon = ParcelServiceOptions.Companion.KeyIcon.LocationCity
        ) //district/Gemarkung
        val DISTRICT_ID = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "district.id",
            translations = ParcelServiceOptions.Companion.PreDefined.DISTRICT_ID.toTranslatable()
        ) // districtKey/Gemarkungsschlüssel
        val DISTRICT_COMPARTMENT = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "district.compartment",
            translations = ParcelServiceOptions.Companion.PreDefined.DISTRICT_COMPARTMENT.toTranslatable()
        ) //_/Flur
        val DISTRICT_COMPARTMENT_ID = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "district.compartment.id",
            translations = ParcelServiceOptions.Companion.PreDefined.DISTRICT_COMPARTMENT_ID.toTranslatable()
        ) //areaNumber/Flurschlüssel
        val DISTRICT_MUNICIPALITY = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "district.municipality",
            translations = ParcelServiceOptions.Companion.PreDefined.DISTRICT_MUNICIPALITY.toTranslatable()
        )//_/Gemeinde
        val DISTRICT_MUNICIPALITY_ID = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "district.municipality.id",
            translations = ParcelServiceOptions.Companion.PreDefined.DISTRICT_MUNICIPALITY_ID.toTranslatable()
        ) //areaCode/Gemeindeschlüssel
        val DISTRICT_REGION = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "district.region",
            translations = ParcelServiceOptions.Companion.PreDefined.DISTRICT_REGION.toTranslatable()
        )//_/Kreis
        val DISTRICT_REGION_ID = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "district.region.id",
            translations = ParcelServiceOptions.Companion.PreDefined.DISTRICT_REGION_ID.toTranslatable()
        )//_/Kreisschlüssel
        val LOCATION = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "location",
            translations = ParcelServiceOptions.Companion.PreDefined.LOCATION.toTranslatable(),
            icon = ParcelServiceOptions.Companion.KeyIcon.Flag
        )//locationDesignation/Lagebezeichnung
        val LANDREGISTERNUMBER = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "landRegisterNumber",
            translations = ParcelServiceOptions.Companion.PreDefined.LANDREGISTERNUMBER.toTranslatable(),
            icon = ParcelServiceOptions.Companion.KeyIcon.MenuBook
        )//landRegisterNumber/Grundbuchnummer
        val USAGE = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "usage",
            type = ParcelServiceOptions.Companion.KeyType.Nothing,
            translations = ParcelServiceOptions.Companion.PreDefined.USAGE.toTranslatable(),
            icon = ParcelServiceOptions.Companion.KeyIcon.Info
        )//_/Nutzung (von der API)
        val USAGE_HINT = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "usage.hint",
            translations = ParcelServiceOptions.Companion.PreDefined.USAGE_HINT.toTranslatable(),
            readonly = true
        )//_/Nutzung (von der API)
        val USAGE_BUILDINGS = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "usage.buildings",
            type = ParcelServiceOptions.Companion.KeyType.Boolean,
            translations = ParcelServiceOptions.Companion.PreDefined.USAGE_BUILDINGS.toTranslatable(),
            icon = ParcelServiceOptions.Companion.KeyIcon.House
        )//buildings/Bebauung
        val USAGE_LEGALDEVIATION = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "usage.legalDeviation",
            type = ParcelServiceOptions.Companion.KeyType.Boolean,
            translations = ParcelServiceOptions.Companion.PreDefined.USAGE_LEGALDEVIATION.toTranslatable()
        )//_/Abweichender Rechtszustand
        val PARCELID = ParcelServiceOptions.Companion.ParcelKey(
            identifier = "id",
            translations = ParcelServiceOptions.Companion.PreDefined.PARCELID.toTranslatable(),
            readonly = true,
            icon = ParcelServiceOptions.Companion.KeyIcon.Fingerprint
        )//_/Abweichender Rechtszustand

        fun fillIn(
            plot: String? = null,
            plotNumerator: String? = null,
            plotDenominator: String? = null,
            plotSeparator: String? = null,
            area: String? = null,
            district: String? = null,
            districtId: String? = null,
            districtCompartment: String? = null,
            districtCompartmentId: String? = null,
            districtMunicipality: String? = null,
            districtMunicipalityId: String? = null,
            districtRegion: String? = null,
            districtRegionId: String? = null,
            location: String? = null,
            landRegisterNumber: String? = null,
            usage: String? = null,
            usageHint: String? = null,
            usageBuildings: String? = null,
            usageLegalDeviation: String? = null,
            parcelId: String? = null,
        ): List<ParcelServiceOptions.Companion.ParcelKey> = listOf(
            PLOT.copy(reference = plot),
            PLOT_NUMERATOR.copy(reference = plotNumerator),
            PLOT_SEPARATOR.copy(reference = plotSeparator),
            PLOT_DENOMINATOR.copy(reference = plotDenominator),
            AREA.copy(reference = area),
            DISTRICT.copy(reference = district),
            DISTRICT_ID.copy(reference = districtId),
            DISTRICT_COMPARTMENT.copy(reference = districtCompartment),
            DISTRICT_COMPARTMENT_ID.copy(reference = districtCompartmentId),
            DISTRICT_MUNICIPALITY.copy(reference = districtMunicipality),
            DISTRICT_MUNICIPALITY_ID.copy(reference = districtMunicipalityId),
            DISTRICT_REGION.copy(reference = districtRegion),
            DISTRICT_REGION_ID.copy(reference = districtRegionId),
            LOCATION.copy(reference = location),
            LANDREGISTERNUMBER.copy(reference = landRegisterNumber),
            USAGE.copy(reference = usage),
            USAGE_HINT.copy(reference = usageHint),
            USAGE_BUILDINGS.copy(reference = usageBuildings),
            USAGE_LEGALDEVIATION.copy(reference = usageLegalDeviation),
            PARCELID.copy(reference = parcelId),
        )
    }

    object Path {
        val params = listOf(
            ReferenceOr.value(
                Parameter(
                    Parameters.WHERE,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                )
            ),
            ReferenceOr.value(
                Parameter(
                    Parameters.OUT_FIELDS,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                )
            ),
            ReferenceOr.value(
                Parameter(
                    Parameters.GEOMETRY,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                ),
            ),
            ReferenceOr.value(
                Parameter(
                    Parameters.RETURN_GEOMETRY,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                ),
            ),
            ReferenceOr.value(
                Parameter(
                    Parameters.OUT_SR,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                ),
            ),
            ReferenceOr.value(
                Parameter(
                    Parameters.IN_SR,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                ),
            ),
            ReferenceOr.value(
                Parameter(
                    Parameters.GEOMETRY_TYPE,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                ),
            ),
            ReferenceOr.value(
                Parameter(
                    Parameters.SPATIAL_REL,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                ),
            ),
            ReferenceOr.value(
                Parameter(
                    Parameters.FILE,
                    Parameter.Input.Query,
                    schema = ReferenceOr.value(
                        Schema(
                            type = Schema.Type.Basic.String
                        )
                    )
                )
            )
        )

        object Parameters {
            const val WHERE = "where"
            const val OUT_FIELDS = "outFields"
            const val GEOMETRY = "geometry"
            const val RETURN_GEOMETRY = "returnGeometry"
            const val IN_SR = "inSR"
            const val OUT_SR = "outSR"
            const val GEOMETRY_TYPE = "geometryType"
            const val SPATIAL_REL = "spatialRel"
            const val FILE = "f"
        }
    }

    private val LC_DL_ZERO = License(
        name = "Datenlizenz Deutschland – Zero",
        url = "https://www.govdata.de/dl-de/zero-2-0"
    )
    private const val LC_DL_BY = "https://www.govdata.de/dl-de/by-2-0"
    private const val LC_CC_BY = "https://creativecommons.org/licenses/by/4.0/"

    val DE_TH = PathItem(
        summary = "Flurstücke Thüringen",
        get = Operation(
            operationId = Bundesland.THUERINGEN.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.THUERINGEN.definition.roughBoundaries,
                    correspondsTo = Bundesland.THUERINGEN.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "flaeche",
                        parcelId = "flstkennz",
                        district = "gemarkung",
                        districtId = "gemaschl",
                        districtCompartment = "flur",
                        districtCompartmentId = "flurschl",
                        districtMunicipality = "gemeinde",
                        districtMunicipalityId = "gmdschl",
                        districtRegion = "kreis",
                        districtRegionId = "kreisschl",
                        usageLegalDeviation = "abwrecht",
                        plot = "flurstnr",
                        plotNumerator = "flstnrzae",
                        plotDenominator = "flstnrnen",
                        location = "lagebeztxt",
                        usageHint = "tntext"
                    ),
                    parcelLinkReference = Bundesland.THUERINGEN.iso3166_2 + "_default",
                    license = License(
                        name = "© GDI-Th",
                        identifier = "Flurstücke Thüringen",
                        url = LC_DL_BY
                    )
                ).usable()
            ),
        ),
        parameters = Path.params
    )
    val DE_SN = PathItem(
        summary = "Flurstücke Sachsen",
        get = Operation(
            operationId = Bundesland.SACHSEN.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.SACHSEN.definition.roughBoundaries,
                    correspondsTo = Bundesland.SACHSEN.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "AREA_m2",
                        parcelId = "NATIONALCA",
                        district = "ADMIN_UNIT",
                        districtId = "ZONING",
                        plotNumerator = "LABEL",
                    ),
                    parcelLinkReference = Bundesland.SACHSEN.iso3166_2 + "_default",
                    license = License(
                        name = "© Geobasisinformation und Vermessung Sachsen (GeoSN)",
                        identifier = "Flurstücke Sachsen",
                        url = LC_DL_BY
                    )
                ).usable(),
            )
        ),
        parameters = Path.params
    )
    val DE_BB = PathItem(
        summary = "Flurstücke Brandenburg",
        get = Operation(
            operationId = Bundesland.BRANDENBURG.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.BRANDENBURG.definition.roughBoundaries,
                    correspondsTo = Bundesland.BRANDENBURG.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "flaeche",
                        parcelId = "flstkennz",
                        district = "gemarkung",
                        districtCompartmentId = "flur",
                        plotNumerator = "flurstnr",
                        districtMunicipalityId = "gmdschl",
                        location = "lagebeztxt"
                    ),
                    parcelLinkReference = Bundesland.BRANDENBURG.iso3166_2 + "_default",
                    license = License(
                        name = "GeoBasis-DE/LGB, 2023",
                        identifier = "Flurstücke Brandenburg",
                        url = LC_DL_BY
                    )
                ).usable(),
            )
        ),
        parameters = Path.params
    )
    val DE_HE = PathItem(
        summary = "Flurstücke Hessen",
        get = Operation(
            operationId = Bundesland.HESSEN.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.HESSEN.definition.roughBoundaries,
                    correspondsTo = Bundesland.HESSEN.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "amtlicheFlaeche",
                        parcelId = "flurstueckskennzeichen",
                        districtId = "gemarkung_AX_Gemarkung_Schluess",
                        plotNumerator = "flurstuecksnummer_AX_Flurstueck",
                        plotDenominator = "flurstuecksnummer_AX_Flurstue_1",
                    ),
                    parcelLinkReference = Bundesland.HESSEN.iso3166_2 + "_default",
                    license = LC_DL_ZERO.copy(identifier = "Flurstücke Hessen")
                ).usable(),
            )
        ),
        parameters = Path.params
    )
    val DE_HH = PathItem(
        summary = "Flurstücke Hamburg",
        get = Operation(
            operationId = Bundesland.HAMBURG.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.HAMBURG.definition.roughBoundaries,
                    correspondsTo = Bundesland.HAMBURG.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "areaValue",
                        parcelId = "nationalCadastralReference",
                        plotNumerator = "label",
                    ),
                    parcelLinkReference = Bundesland.HAMBURG.iso3166_2 + "_default",
                    license = License(
                        name = "Freie und Hansestadt Hamburg, Landesbetrieb Geoinformation und Vermessung (LGV)",
                        url = LC_DL_BY,
                        identifier = "Flurstücke Hamburg"
                    )
                ).usable(),
            )
        ),
        parameters = Path.params
    )
    val DE_NW = PathItem(
        summary = "Flurstücke Nordrhein-Westfalen",
        get = Operation(
            operationId = Bundesland.NORDRHEIN_WESTFALEN.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.NORDRHEIN_WESTFALEN.definition.roughBoundaries,
                    correspondsTo = Bundesland.NORDRHEIN_WESTFALEN.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "amtlicheFlaeche",
                        parcelId = "flurstueckskennzeichen",
                        districtId = "gemarkung_AX_Gemarkung_Schluess",
                        districtCompartmentId = "flurnummer",
                        plotNumerator = "flurstuecksnummer_AX_Flurstueck",
                        plotDenominator = "flurstuecksnummer_AX_Flurstue_1",
                    ),
                    parcelLinkReference = Bundesland.NORDRHEIN_WESTFALEN.iso3166_2 + "_default",
                    license = LC_DL_ZERO.copy(identifier = "Flurstücke Nordrhein-Westfalen")
                ).usable(),
            )
        ),
        parameters = Path.params
    )
    val DE_ST = PathItem(
        summary = "Flurstücke Sachsen-Anhalt",
        get = Operation(
            operationId = Bundesland.SACHSEN_ANHALT.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.SACHSEN_ANHALT.definition.roughBoundaries,
                    correspondsTo = Bundesland.SACHSEN_ANHALT.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "flaeche",
                        parcelId = "flstkennz",
                        district = "gemarkung",
                        districtId = "gemaschl",
                        districtCompartmentId = "flur",
                        plotNumerator = "flstnrzae",
                        plotDenominator = "flstnrnen",
                        districtMunicipalityId = "gmdschl",
                        location = "lagebeztxt"
                    ),
                    parcelLinkReference = Bundesland.SACHSEN_ANHALT.iso3166_2 + "_default",
                    license = License(
                        name = "GeoBasis-DE / LVermGeo LSA, [2023]",
                        identifier = "Flurstücke Sachsen-Anhalt",
                        url = "https://www.lvermgeo.sachsen-anhalt.de/datei/anzeigen/id/3567,501/Nutzungsbedingungen.pdf"
                    )
                ).usable(),
            )
        ),
        parameters = Path.params
    )
    val DE_BE = PathItem(
        summary = "Flurstücke Berlin",
        get = Operation(
            operationId = Bundesland.BERLIN.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.BERLIN.definition.roughBoundaries,
                    correspondsTo = Bundesland.BERLIN.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        area = "afl",
                        parcelId = "fsko",
                        district = "namgem",
                        districtId = "gmk",
                        districtCompartmentId = "fln",
                        plotNumerator = "zae",
                        plotDenominator = "nen",
                    ),
                    parcelLinkReference = Bundesland.BERLIN.iso3166_2 + "_default",
                    license = License(
                        name = "Geoportal Berlin / ALKIS Berlin - Flurstücke",
                        identifier = "Flurstücke Berlin",
                        url = LC_DL_BY
                    )
                ).usable(),
            )
        ),
        parameters = Path.params
    )
    val DE_NI = PathItem(
        summary = "Flurstücke Niedersachsen",
        get = Operation(
            operationId = Bundesland.NIEDERSACHSEN.iso3166_2,
            extensions = mapOf(
                AviatorExtensionSpec.ServiceLocator.O.key to locator.usable(),
                AviatorExtensionSpec.ServiceOptions.O.key to ParcelServiceOptions(
                    bounds = Bundesland.NIEDERSACHSEN.definition.roughBoundaries,
                    correspondsTo = Bundesland.NIEDERSACHSEN.iso3166_2,
                    keys = DefaultKeys.fillIn(
                        district = "gmk__bez",
                        districtCompartmentId = "fln",
                        plotNumerator = "fsn__zae",
                        plotDenominator = "fsn__nen",
                        parcelId = "fsk",
                        area = "afl",
                        location = "gem__bez"
                    ),
                    parcelLinkReference = Bundesland.NIEDERSACHSEN.iso3166_2 + "_default",
                    license = License(
                        name = "LGLN Open Geodata",
                        identifier = "Flurstücke Niedersachsen",
                        url = LC_CC_BY
                    )
                ).usable(),
            )
        ),
        parameters = Path.params
    )
}