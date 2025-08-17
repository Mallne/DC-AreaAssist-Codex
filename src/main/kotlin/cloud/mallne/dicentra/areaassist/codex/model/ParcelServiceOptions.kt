package cloud.mallne.dicentra.areaassist.codex.model

import cloud.mallne.dicentra.areaassist.codex.statics.SISystems
import cloud.mallne.dicentra.areaassist.model.Point
import cloud.mallne.dicentra.areaassist.statics.bundeslaender.Bundesland
import cloud.mallne.dicentra.synapse.statics.Serialization
import cloud.mallne.dicentra.polyfill.Validation
import cloud.mallne.dicentra.aviator.core.InflatedServiceOptions
import cloud.mallne.dicentra.aviator.core.ServiceOptions
import cloud.mallne.dicentra.aviator.koas.info.License
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class ParcelServiceOptions @OptIn(ExperimentalUuidApi::class) constructor(
    val bounds: List<Point<Double>> = listOf(),
    val minimalDefinition: Boolean = true,
    val canInferWithParcelId: Boolean = true,
    val serviceType: String = "ArcGIS",
    val correspondsTo: String = Bundesland.CUSTOM.iso3166_2,
    val parcelLinkReference: String = Uuid.random().toString(),
    val license: License? = null,
    val keys: List<ParcelKey> = listOf(),
    val synapseCatalyst: SynapseCatalystConfig = SynapseCatalystConfig(),
) : InflatedServiceOptions {
    companion object {
        @Serializable
        data class SynapseCatalystConfig(
            val serversideOnly: Boolean = false,
            val includeInAggregation: Boolean = true,
            val includeInSpecific: Boolean = true,
            val includeInMCP: Boolean = true,
        ) {
            init {
                if (serversideOnly) {
                    require(includeInSpecific) {
                        "If the Service is Synapse Catalyst Serverside-Only, then includeInSpecific must be set to true."
                    }
                }
            }
        }

        @Serializable
        data class ParcelKey(
            val identifier: String,
            val reference: String? = null,
            val type: KeyType = KeyType.String,
            val format: KeyFormat = KeyFormat(),
            val translations: KeyTranslation,
            val readonly: Boolean = false, //mark the Property as Readonly, note that an empty readonly Property will be hidden in the UI
            val hideInUI: Boolean = false, // will hide the Property in the UI
            val icon: KeyIcon? = null,
        ) {
            fun isGraphObject(ofGraph: String): Boolean {
                val id = identifier.split(".")
                val graphs = ofGraph.split(".")
                return id.containsAll(graphs)
            }
        }

        @Serializable
        enum class KeyIcon() {
            ZoomOutMap(),
            LocationCity(),
            ModeOfTravel(),
            MenuBook(),
            Info(),
            AccessTime(),
            House(),
            Fingerprint(),
            ShareLocation(),
            Flag(),
        }

        @Serializable
        data class KeyTranslation(
            val predef: PreDefined? = null,
            val l18n: Map<String, String>? = null,
            val l18nDesc: Map<String, String>? = null
        ) {
            init {
                require(Validation.Null.oneOf(this))
            }
        }

        @Serializable
        enum class KeyType {
            String,
            Number,
            Boolean,
            Nothing,
        }

        @Serializable
        data class KeyFormat(
            val chrono: ChronoFormat? = null,
            val unit: UnitFormat? = null,
        ) {

            init {
                //exaclty one or none Property has to be set
                require(Validation.Null.oneOrNoneOf(this)) {
                    "Exactly one or none of chrono and unit must be set."
                }
            }

            companion object {
                @Serializable
                data class ChronoFormat(
                    val unicodePattern: String
                )

                @Serializable
                data class UnitFormat(
                    val unitSystem: SISystems,
                    val ingestUnit: String,
                    val defaultDisplayUnit: String = ingestUnit,
                    val precision: Int = 2,
                    val editable: Boolean = true,
                )
            }
        }


        object GenericJson {
            const val ORIGIN = "origin"
        }

        @Serializable
        enum class PreDefined() {
            PLOT,
            PLOT_NUMERATOR,
            PLOT_SEPARATOR,
            PLOT_DENOMINATOR,
            AREA,
            DISTRICT,
            DISTRICT_ID,
            DISTRICT_COMPARTMENT,
            DISTRICT_COMPARTMENT_ID,
            DISTRICT_MUNICIPALITY,
            DISTRICT_MUNICIPALITY_ID,
            DISTRICT_REGION,
            DISTRICT_REGION_ID,
            LOCATION,
            LANDREGISTERNUMBER,
            USAGE,
            USAGE_HINT,
            USAGE_BUILDINGS,
            USAGE_MANAGED,
            USAGE_LEGALDEVIATION,
            PARCELID,

            //Commons
            ID;

            fun toTranslatable() = KeyTranslation(predef = this)
        }
    }

    override fun usable(): ServiceOptions = Serialization().encodeToJsonElement(this)
}