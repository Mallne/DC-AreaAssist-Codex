package cloud.mallne.dicentra.synapse.statics

import kotlinx.serialization.json.Json

object Serialization {
    private val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    operator fun invoke() = json
}