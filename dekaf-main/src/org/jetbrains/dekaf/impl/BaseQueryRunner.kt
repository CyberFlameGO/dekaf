package org.jetbrains.dekaf.impl

import org.jetbrains.dekaf.core.DBQueryRunner
import org.jetbrains.dekaf.core.ImplementationAccessibleService.Names.*
import org.jetbrains.dekaf.core.QueryResultLayout
import org.jetbrains.dekaf.core.TaskKind
import org.jetbrains.dekaf.inter.InterCursor
import org.jetbrains.dekaf.inter.InterSeance
import org.jetbrains.dekaf.inter.InterTask
import org.jetbrains.dekaf.util.Objects.castTo


internal class BaseQueryRunner<T>: BaseStatementRunner, DBQueryRunner<T> {

    private val layout: QueryResultLayout<T>
    private var portionSize: Int = 100

    private var interCursor: InterCursor? = null
    private var executed = false


    internal constructor(session: BaseSession, interSeance: InterSeance, text: String, layout: QueryResultLayout<T>)
            : super(session, interSeance, text)
    {
        this.layout = layout
    }

    override fun prepare() {
        val task = InterTask(TaskKind.TASK_QUERY, text)
        interSeance.prepare(task)
        prepared = true
    }

    override fun withParams(vararg params: Any?): BaseQueryRunner<T> {
        closeCursor()
        executed = false

        super.withParams(*params)

        return this
    }

    override fun packBy(rowsPerPack: Int): DBQueryRunner<T> {
        this.portionSize = rowsPerPack
        if (executed && interCursor != null) interCursor?.setPortionSize(rowsPerPack)
        return this
    }

    override fun execute(): DBQueryRunner<T> {
        closeCursor()

        if (!prepared) {
            prepare()
        }

        interSeance.execute()
        executed = true

        val interLayout = layout.makeInterLayout()
        val ic = interSeance.openCursor(0.toByte(), interLayout)
        if (ic != null) {
            ic.setPortionSize(portionSize)
            interCursor = ic
        }

        return this
    }

    override fun nextPack(): T? {
        if (!executed) execute()

        val cursor = interCursor ?: return null

        val a = cursor.retrievePortion() ?: return null

        val builder = layout.makeBuilder()
        builder.add(a)
        return builder.build()
    }

    override fun run(): T? {
        execute()

        val cursor = interCursor ?: return null
        val builder = layout.makeBuilder()
        var portion: Any? = cursor.retrievePortion()
        while (portion != null) {
            builder.add(portion)
            portion = cursor.retrievePortion()
        }
        return builder.build()
    }

    private fun closeCursor() {
        val ic = interCursor
        if (ic != null) {
            ic.close()
            interCursor = null
        }
    }

    override fun <I : Any?> getSpecificService(serviceClass: Class<I>, serviceName: String): I? =
            when (serviceName) {
                INTERMEDIATE_SERVICE                         -> castTo(serviceClass, interSeance)
                JDBC_CONNECTION, JDBC_DRIVER, JDBC_STATEMENT -> interSeance.getSpecificService(serviceClass, serviceName)
                JDBC_METADATA, JDBC_RESULT_SET               -> interCursor?.getSpecificService(serviceClass, serviceName)
                else                                         -> null
            }
}