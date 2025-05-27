package cloud.mallne.dicentra.areaassist.synapse.service

import cloud.mallne.dicentra.areaassist.synapse.model.Configuration
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Single

@Single
class DatabaseService(config: Configuration) {
    val database = Database.Companion.connect(
        url = "jdbc:${config.data.url}",
        user = config.data.user,
        driver = "org.postgresql.Driver",
        password = config.data.password,
    )

    fun <T> transaction(block: Transaction.() -> T): T {
        return transaction(database, block)
    }

    suspend operator fun <T> invoke(block: Transaction.() -> T): T = dbQuery(block)

    suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    init {
        val scm = Schema(config.data.schema)
        SchemaUtils.createSchema(scm)
        SchemaUtils.setSchema(scm)
    }
}