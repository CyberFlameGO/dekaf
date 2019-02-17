package org.jetbrains.dekaf.impl

import org.jetbrains.dekaf.core.DBLeasedSession
import org.jetbrains.dekaf.core.DBScriptRunner
import org.jetbrains.dekaf.core.DBTransaction
import org.jetbrains.dekaf.core.QueryResultLayout
import org.jetbrains.dekaf.inter.InterSession
import org.jetbrains.dekaf.sql.SqlCommand
import org.jetbrains.dekaf.sql.SqlQuery
import org.jetbrains.dekaf.sql.SqlScript
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.function.Consumer
import java.util.function.Function


internal open class BaseSession: DBLeasedSession {

    /// SETTINGS \\\

    val facade: BaseFacade

    val inter: InterSession


    /// STATE \\\

    var closed: Boolean = false

    var insideTran: Boolean = false

    val runners = ConcurrentLinkedDeque<BaseRunner>()


    /// INITIALIZATION \\\

    constructor(facade: BaseFacade, inter: InterSession) {
        this.facade = facade
        this.inter = inter
    }


    /// TRANSACTIONS \\\

    override fun <R : Any?> inTransaction(operation: Function<DBTransaction, R>): R {
        beginTransaction()
        try {
            val result = operation.apply(this)
            commit()
            return result
        }
        catch (e: Exception) {
            rollback()
            throw e
        }
    }

    override fun inTransactionDo(operation: Consumer<DBTransaction>) {
        beginTransaction()
        try {
            operation.accept(this)
            commit()
        }
        catch (e: Exception) {
            rollback()
            throw e
        }
    }

    override fun beginTransaction() {
        inter.begin()
        insideTran = true
    }

    override val isInTransaction: Boolean
        get() = insideTran

    override fun commit() {
        inter.commit()
        insideTran = false
    }

    override fun rollback() {
        inter.rollback()
        insideTran = false
    }


    /// RUNNERS \\\

    override fun command(command: SqlCommand): BaseCommandRunner {
        val commandText = command.sourceText
        return command(commandText)
    }

    override fun command(commandText: String): BaseCommandRunner {
        val interSeance = inter.openSeance()
        val runner = BaseCommandRunner(this, interSeance, commandText)
        runners.add(runner)
        return runner
    }

    override fun <S> query(query: SqlQuery<S>): BaseQueryRunner<S> {
        return query(query.sourceText, query.layout)
    }

    override fun <T> query(queryText: String, layout: QueryResultLayout<T>): BaseQueryRunner<T> {
        val interSeance = inter.openSeance()
        val runner = BaseQueryRunner(this, interSeance, queryText, layout)
        runners.add(runner)
        return runner
    }

    override fun script(script: SqlScript): DBScriptRunner {
        val scriptRunner = BaseScriptRunner(this, script)
        runners.add(scriptRunner)
        return scriptRunner
    }

    override fun ping(): Int {
        return inter.ping()
    }

    override val isClosed: Boolean
        get() = closed

    override fun close() {
        inter.close()
        closed = true
        facade.sessionClosed(this)
    }

    override fun <I : Any?> getSpecificService(serviceClass: Class<I>, serviceName: String): I? =
        inter.getSpecificService(serviceClass, serviceName)
    


    /// OTHER \\\

    internal fun runnerClosed(runner: BaseRunner) {
        runners.remove(runner)
    }

}