package cloud.mallne.dicentra.synapse.service

import cloud.mallne.dicentra.synapse.model.dto.ScopeDTO
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.koin.core.annotation.Single

/**
 * Service class that provides CRUD operations for managing scope-related data.
 *
 * This service interacts with a PostgreSQL database using the Exposed library
 * and performs operations within transactional contexts provided by `DatabaseService`.
 *
 * @constructor Initializes the `ScopeService` and creates the `Scopes` table if it does not already exist.
 *
 * @property databaseService The `DatabaseService` instance used to execute database transactions.
 */
@Single
class ScopeService(private val databaseService: DatabaseService) {
    object Scopes : IntIdTable() {
        val name = varchar("name", 255)
        val attaches = varchar("attaches", 255)
        val created = datetime("created").defaultExpression(CurrentDateTime)
    }

    init {
        databaseService.transaction {
            SchemaUtils.create(Scopes)
        }
    }

    /**
     * Creates a new scope record in the database and returns its generated ID.
     *
     * @param scopeDTO the data transfer object containing the scope information
     *                 to be saved in the database. It includes the scope's name
     *                 and associated attachments.
     * @return the ID of the newly created scope record.
     */
    suspend fun create(scopeDTO: ScopeDTO): Int = databaseService {
        Scopes.insert {
            it[name] = scopeDTO.name
            it[attaches] = scopeDTO.attaches
        }[Scopes.id].value
    }

    /**
     * Retrieves a `ScopeDTO` object corresponding to the specified ID from the database.
     *
     * @param id the unique identifier of the scope to be retrieved.
     * @return the `ScopeDTO` object if found, or `null` if no scope exists with the given ID.
     */
    suspend fun read(id: Int): ScopeDTO? {
        return databaseService {
            Scopes.selectAll()
                .where { Scopes.id eq id }
                .map { ScopeDTO(it[Scopes.id].value, it[Scopes.name], it[Scopes.attaches]) }
                .singleOrNull()
        }
    }

    /**
     * Retrieves a list of `ScopeDTO` objects from the database that have an attachment
     * matching the provided parameter.
     *
     * @param attachment the attachment identifier used to filter the scopes in the database.
     * @return a list of `ScopeDTO` objects with the specified attachment.
     */
    suspend fun readForAttachment(attachment: String): List<ScopeDTO> {
        return databaseService {
            Scopes.selectAll()
                .where { Scopes.attaches eq attachment }
                .map { ScopeDTO(it[Scopes.id].value, it[Scopes.name], it[Scopes.attaches]) }
        }
    }

    /**
     * Retrieves a list of `ScopeDTO` objects from the database where the scope name matches the given parameter.
     *
     * @param name the name of the scope to filter for in the database.
     * @return a list of `ScopeDTO` objects that have the specified name.
     */
    suspend fun readForName(name: String): List<ScopeDTO> {
        return databaseService {
            Scopes.selectAll()
                .where { Scopes.name eq name }
                .map { ScopeDTO(it[Scopes.id].value, it[Scopes.name], it[Scopes.attaches]) }
        }
    }

    /**
     * Updates an existing scope record in the database using the provided data transfer object.
     *
     * @param scopeDTO the data transfer object containing the updated scope information.
     *                 This includes the scope's unique identifier, name, and associated attachments.
     */
    suspend fun update(scopeDTO: ScopeDTO) {
        databaseService {
            Scopes.update({ Scopes.id eq scopeDTO.id }) {
                it[name] = scopeDTO.name
                it[attaches] = scopeDTO.attaches
            }
        }
    }

    /**
     * Deletes a scope record from the database based on the provided ID.
     *
     * @param id the unique identifier of the scope to be deleted.
     */
    suspend fun delete(id: Int) {
        databaseService {
            Scopes.deleteWhere { Scopes.id.eq(id) }
        }
    }

    /**
     * Deletes a scope record from the database where the scope name matches the specified parameter.
     *
     * @param name the name of the scope to be deleted.
     */
    suspend fun deleteByName(name: String) {
        databaseService {
            Scopes.deleteWhere { Scopes.name.eq(name) }
        }
    }

    companion object Attachment {
        const val USER_PREFIX = "user:"

        fun user(username: String) = USER_PREFIX + username
    }
}