package cloud.mallne.dicentra.areaassist.synapse.statics

import cloud.mallne.dicentra.areaassist.synapse.exceptions.DiCentraException
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
suspend inline fun RoutingContext.verify(
    value: Boolean,
    lazyMessage: () -> Pair<HttpStatusCode, String>
) {
    contract {
        returns() implies value
    }
    if (!value) {
        val message = lazyMessage()
        call.respond(message.first, message.second)
        throw DiCentraException("A predicate is not matched tor this request!")
    }
}