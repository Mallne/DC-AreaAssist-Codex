package cloud.mallne.dicentra.areaassist.synapse.routes

import cloud.mallne.dicentra.areaassist.synapse.model.User
import cloud.mallne.dicentra.areaassist.synapse.statics.verify
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Registers the `/user` route under the Ktor application's routing.
 * The route is authenticated and allows a user to retrieve their own profile data.
 *
 * The user must be authenticated to access this endpoint. If the authentication is absent or invalid,
 * the route responds with an HTTP 401 Unauthorized status along with an appropriate error message.
 *
 * If authentication succeeds, the route responds with the authenticated user's data.
 */
fun Application.user() {
    routing {
        authenticate {
            get("/user") {
                val user: User? = call.authentication.principal()
                verify(user != null) { HttpStatusCode.Unauthorized to "You need to be Authenticated for this request!" }
                call.respond(user)
            }
        }
    }
}