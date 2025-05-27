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

    suspend fun create(apiService: APIServiceDTO): String = databaseService {
        APIServiceData.insert {
            it[id] = apiService.id
            it[service] = apiService.serviceDefinition
            it[scope] = apiService.scope
        }[APIServiceData.id].value
    }

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

    suspend fun update(apiService: APIServiceDTO) {
        databaseService {
            APIServiceData.update({ APIServiceData.id eq apiService.id }) {
                it[service] = apiService.serviceDefinition
                it[scope] = apiService.scope
            }
        }
    }

    suspend fun delete(id: String) {
        databaseService {
            APIServiceData.deleteWhere { APIServiceData.id.eq(id) }
        }
    }
}