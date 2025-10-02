package cloud.mallne.dicentra.areaassist.codex.service

import cloud.mallne.dicentra.areaassist.codex.model.ActionDTO
import cloud.mallne.dicentra.areaassist.extensions.ChronoExtensions.toInstant
import cloud.mallne.dicentra.areaassist.extensions.ChronoExtensions.toLocalDateTime
import cloud.mallne.dicentra.areaassist.model.actions.ServersideAction
import cloud.mallne.dicentra.synapse.model.RequiresTransactionContext
import cloud.mallne.dicentra.synapse.statics.Serialization
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.json.jsonb
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.koin.core.annotation.Single
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

/**
 * Service class that provides CRUD operations for managing scope-related data.
 *
 * This service interacts with a PostgreSQL database using the Exposed library
 * and performs operations within transactional contexts provided by `DatabaseService`.
 *
 * @constructor Initializes the `ScopeService` and creates the `Scopes` table if it does not already exist.
 */
@Single
class ActionsService() {
    object Actions : IdTable<String>() {
        val action = jsonb<ServersideAction>("action", Serialization())
        val scope = varchar("scope", 255).nullable()
        val created = datetime("created").defaultExpression(CurrentDateTime)

        @OptIn(ExperimentalTime::class)
        val expires = datetime("expires").default(Clock.System.now().plus(14.days).toLocalDateTime())
        override val id: Column<EntityID<String>> = varchar("id", 36).entityId()
    }

    /**
     * Creates a new scope record in the database and returns its generated ID.
     *
     * @param scopeDTO the data transfer object containing the scope information
     *                 to be saved in the database. It includes the scope's name
     *                 and associated attachments.
     * @return the ID of the newly created scope record.
     */
    @OptIn(ExperimentalTime::class)
    @RequiresTransactionContext
    suspend fun create(actionDTO: ActionDTO) = Actions.insert {
        it[action] = actionDTO.action
        it[scope] = actionDTO.scope
        it[expires] = actionDTO.expires.toLocalDateTime()
        it[id] = actionDTO.id
    }[Actions.id].value

    /**
     * Retrieves a `ScopeDTO` object corresponding to the specified ID from the database.
     *
     * @param id the unique identifier of the scope to be retrieved.
     * @return the `ScopeDTO` object if found, or `null` if no scope exists with the given ID.
     */
    @OptIn(ExperimentalTime::class)
    @RequiresTransactionContext
    suspend fun read(id: String): ActionDTO? {
        return Actions.selectAll()
            .where { Actions.id eq id }
            .map {
                ActionDTO(
                    it[Actions.id].value,
                    it[Actions.scope],
                    it[Actions.action],
                    it[Actions.expires].toInstant()
                )
            }
            .singleOrNull()
    }

    @OptIn(ExperimentalTime::class)
    @RequiresTransactionContext
    suspend fun compact() = Actions.deleteWhere {
        Actions.expires greaterEq Clock.System.now().toLocalDateTime()
    }

    /**
     * Deletes a scope record from the database based on the provided ID.
     *
     * @param id the unique identifier of the scope to be deleted.
     */
    @RequiresTransactionContext
    suspend fun delete(id: String) {
        Actions.deleteWhere { Actions.id eq id }
    }

    @OptIn(ExperimentalTime::class)
    @RequiresTransactionContext
    suspend fun readAll() = Actions.selectAll()
        .map { ActionDTO(it[Actions.id].value, it[Actions.scope], it[Actions.action], it[Actions.expires].toInstant()) }
        .toList()
}