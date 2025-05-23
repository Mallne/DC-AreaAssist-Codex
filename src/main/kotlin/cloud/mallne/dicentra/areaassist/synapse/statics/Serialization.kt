package cloud.mallne.dicentra.areaassist.synapse.statics

import kotlinx.serialization.json.Json

object Serialization {
    private val json: Json = Json {
        ignoreUnknownKeys = true
    }

    operator fun invoke() = json
}