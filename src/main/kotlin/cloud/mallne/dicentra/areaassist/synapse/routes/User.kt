package cloud.mallne.dicentra.areaassist.synapse.routes

import cloud.mallne.dicentra.areaassist.synapse.model.User
import cloud.mallne.dicentra.areaassist.synapse.statics.verify
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Routing.user() {
    authenticate {
        get("/user") {
            val user: User? = call.authentication.principal()
            verify(user != null) { HttpStatusCode.Unauthorized to "You need to be Authenticated for this request!" }
            call.respond(user)
        }
    }
}