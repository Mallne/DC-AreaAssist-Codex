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
    private val scm = Schema(config.data.schema)
    val database = Database.Companion.connect(
        url = "jdbc:${config.data.url}",
        user = config.data.user,
        driver = "org.postgresql.Driver",
        password = config.data.password,
    )

    /**
     * Executes a transaction within the context of the configured database and schema.
     * Provides a DSL for database operations. The schema is set before executing the given block.
     *
     * @param T the type of result produced by the provided transaction block.
     * @param block the transactional block to be executed. It operates within the context of the `Transaction`.
     * @return the result of the provided transaction block after execution.
     */
    fun <T> transaction(block: Transaction.() -> T): T {
        return transaction(database) {
            SchemaUtils.setSchema(scm)
            block()
        }
    }

    /**
     * Executes a suspended transaction within the database context, providing a DSL
     * for database operations. This function is a shorthand operator for invoking
     * `dbQuery` with the given transactional block.
     *
     * @param T the type of result produced by the provided transaction block.
     * @param block the transactional block to be executed. It operates within the
     *              context of the `Transaction`.
     * @return the result of the provided transaction block after execution.
     */
    suspend operator fun <T> invoke(block: Transaction.() -> T): T = dbQuery(block)

    /**
     * Executes a suspended transaction within the database context, allowing operations
     * on the database schema set by the service.
     *
     * @param T the type of result expected from the database transaction.
     * @param block the suspendable lambda containing the transaction operations to be executed.
     *              It operates within the context of the `Transaction`.
     * @return the result of the suspended transaction block.
     */
    suspend fun <T> dbQuery(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) {
            SchemaUtils.setSchema(scm)
            block()
        }

    init {
        transaction {
            SchemaUtils.createSchema(scm)
            SchemaUtils.setSchema(scm)
        }
    }
}