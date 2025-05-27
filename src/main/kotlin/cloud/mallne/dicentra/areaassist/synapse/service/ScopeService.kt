package cloud.mallne.dicentra.areaassist.synapse.service

import cloud.mallne.dicentra.areaassist.synapse.model.dto.ScopeDTO
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.koin.core.annotation.Single

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

    suspend fun create(scopeDTO: ScopeDTO): Int = databaseService {
        Scopes.insert {
            it[name] = scopeDTO.name
            it[attaches] = scopeDTO.attaches
        }[Scopes.id].value
    }

    suspend fun read(id: Int): ScopeDTO? {
        return databaseService {
            Scopes.selectAll()
                .where { Scopes.id eq id }
                .map { ScopeDTO(it[Scopes.id].value, it[Scopes.name], it[Scopes.attaches]) }
                .singleOrNull()
        }
    }

    suspend fun readForAttachment(attachment: String): List<ScopeDTO> {
        return databaseService {
            Scopes.selectAll()
                .where { Scopes.attaches eq attachment }
                .map { ScopeDTO(it[Scopes.id].value, it[Scopes.name], it[Scopes.attaches]) }
        }
    }

    suspend fun readForName(name: String): List<ScopeDTO> {
        return databaseService {
            Scopes.selectAll()
                .where { Scopes.name eq name }
                .map { ScopeDTO(it[Scopes.id].value, it[Scopes.name], it[Scopes.attaches]) }
        }
    }

    suspend fun update(scopeDTO: ScopeDTO) {
        databaseService {
            Scopes.update({ Scopes.id eq scopeDTO.id }) {
                it[name] = scopeDTO.name
                it[attaches] = scopeDTO.attaches
            }
        }
    }

    suspend fun delete(id: Int) {
        databaseService {
            Scopes.deleteWhere { Scopes.id.eq(id) }
        }
    }

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