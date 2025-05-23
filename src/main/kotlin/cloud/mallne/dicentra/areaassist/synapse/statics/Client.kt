package cloud.mallne.dicentra.areaassist.synapse.statics

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

object Client {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Serialization())
        }
    }

    operator fun invoke() = client
}