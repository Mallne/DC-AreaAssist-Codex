package cloud.mallne.dicentra.areaassist.synapse.service

import cloud.mallne.dicentra.areaassist.synapse.model.dto.APIServiceDTO
import cloud.mallne.dicentra.areaassist.synapse.statics.Serialization
import cloud.mallne.dicentra.aviator.koas.OpenAPI
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.koin.core.annotation.Single

/**
 * Service responsible for managing API service data within a database. It provides operations
 * to create, read, update and delete API service entries. The service interacts with the
 * database using the provided `DatabaseService` instance.
 *
 * @constructor Initializes the APIDBService with the given `DatabaseService` and ensures
 *              the underlying database table schema is created during initialization.
 *
 * @param databaseService The database service used for executing transactional operations.
 */
@Single
class APIDBService(private val databaseService: DatabaseService) {
    object APIServiceData : IdTable<String>() {
        val service = jsonb<OpenAPI>("service", Serialization())
        val scope = varchar("scope", 255).nullable()
        val created = datetime("created").defaultExpression(CurrentDateTime)
        override val id: Column<EntityID<String>> = varchar("id", 36).entityId()
    }

    init {
        databaseService.transaction {
            SchemaUtils.create(APIServiceData)
        }
    }

    /**
     * Creates a new API service record in the database and returns its generated ID.
     *
     * @param apiService the data transfer object containing the API service information
     *                   to be saved in the database. It includes the service's ID, definition,
     *                   and optional scope.
     * @return the ID of the newly created API service record as a string.
     */
    suspend fun create(apiService: APIServiceDTO): String = databaseService {
        APIServiceData.insert {
            it[id] = apiService.id
            it[service] = apiService.serviceDefinition
            it[scope] = apiService.scope
        }[APIServiceData.id].value
    }

    /**
     * Retrieves a single API service record from the database based on the specified ID.
     *
     * @param id the unique identifier of the API service to be retrieved.
     * @return an `APIServiceDTO` object representing the retrieved API service if found, or `null` if no matching record exists.
     */
    suspend fun read(id: String): APIServiceDTO? {
        return databaseService {
            APIServiceData.selectAll()
                .where { APIServiceData.id eq id }
                .map {
                    APIServiceDTO(
                        it[APIServiceData.id].value,
                        it[APIServiceData.service],
                        it[APIServiceData.scope]
                    )
                }
                .singleOrNull()
        }
    }

    /**
     * Retrieves a list of `APIServiceDTO` objects from the database that match the specified scope.
     *
     * @param scope the scope string used to filter the API services. If null, services with no scope will be retrieved.
     * @return a list of `APIServiceDTO` objects that match the given scope. Returns an empty list if no matching records are found.
     */
    suspend fun readForScope(scope: String?): List<APIServiceDTO> {
        return databaseService {
            APIServiceData.selectAll()
                .where { APIServiceData.scope eq scope }
                .map {
                    APIServiceDTO(
                        it[APIServiceData.id].value,
                        it[APIServiceData.service],
                        it[APIServiceData.scope]
                    )
                }
        }
    }

    /**
     * Retrieves a list of `APIServiceDTO` objects from the database that match the given scopes.
     *
     * @param scope a list of scope strings used to filter the API services. Only services
     *              with scopes included in this list will be retrieved.
     * @return a list of `APIServiceDTO` objects that match the provided scopes. If no services
     *         match, an empty list is returned.
     */
    suspend fun readForScopes(scope: List<String>): List<APIServiceDTO> {
        return databaseService {
            APIServiceData.selectAll()
                .where { APIServiceData.scope inList scope }
                .map {
                    APIServiceDTO(
                        it[APIServiceData.id].value,
                        it[APIServiceData.service],
                        it[APIServiceData.scope]
                    )
                }
        }
    }

    /**
     * Updates an existing API service record in the database using the provided data transfer object.
     *
     * @param apiService the data transfer object containing the updated API service information,
     *                   including the service's unique identifier, new service definition,
     *                   and optional scope.
     */
    suspend fun update(apiService: APIServiceDTO) {
        databaseService {
            APIServiceData.update({ APIServiceData.id eq apiService.id }) {
                it[service] = apiService.serviceDefinition
                it[scope] = apiService.scope
            }
        }
    }

    /**
     * Deletes an API service record from the database based on the provided ID.
     *
     * @param id the unique identifier of the API service to be deleted.
     */
    suspend fun delete(id: String) {
        databaseService {
            APIServiceData.deleteWhere { APIServiceData.id.eq(id) }
        }
    }
}