package org.jetbrains.dekaf.core

import org.jetbrains.dekaf.inter.InterLayout
import org.jetbrains.dekaf.inter.InterResultKind
import org.jetbrains.dekaf.inter.InterResultKind.*
import org.jetbrains.dekaf.util.*
import java.util.*
import kotlin.collections.HashMap


sealed class QueryResultLayout<T> {

    /// Definition functions \\\

    abstract val interResultKind: InterResultKind

    abstract val row: QueryRowLayout<*>

    open val justOnePortion: Boolean = false

    fun makeInterLayout(): InterLayout =
            InterLayout(interResultKind, row.interRowKind(),
                        row.interPrimitiveKind(), row.interBaseComponentClass(),
                        row.interColumnNames(), row.interComponentClasses())


    /// Result builder \\\

    abstract inner class Builder {
        abstract fun clear()
        abstract fun add(portion: Any)
        abstract fun build(): T?
    }

    abstract fun makeBuilder(): Builder

}


class QueryResultExistenceLayout: QueryResultLayout<Boolean>() {

    override val interResultKind = RES_EXISTENCE
    override val row = QueryRowExistenceLayout()
    override val justOnePortion = true

    /// Result builder \\\

    inner class MyBuilder: Builder() {

        var existence: Boolean = false

        override fun clear() {
            existence = false
        }

        override fun add(portion: Any) {
            when (portion) {
                is Boolean -> existence = portion
                is Number -> existence = portion.toInt() > 0
                is Array<*> -> existence = portion.isNotEmpty()
                is Collection<*> -> existence = portion.isNotEmpty()
            }
        }

        override fun build(): Boolean? = existence
    }

    override fun makeBuilder() = MyBuilder()
}


class QueryResultOneRowLayout<R>(override val row: QueryRowLayout<R>) : QueryResultLayout<R>() {

    override val interResultKind = RES_ONE_ROW
    override val justOnePortion = true

    /// Result builder \\\

    inner class MyBuilder: Builder() {

        var r: R? = null

        override fun clear() {
            r = null
        }

        override fun add(portion: Any) {
            r = row.transform(portion)
        }

        override fun build(): R? = r
    }

    override fun makeBuilder() = MyBuilder()
}


class QueryResultArrayOfShortLayout : QueryResultLayout<ShortArray>() {
    
    override val interResultKind: InterResultKind = RES_PRIMITIVE_ARRAY

    override val row: QueryRowLayout<*> = QueryRowPrimitiveLayout(JavaPrimitiveKind.JAVA_SHORT)

    /// Result builder \\\

    inner class MyBuilder: Builder() {

        val b = ShortArrayBuilder()

        override fun clear() = b.clear()

        override fun add(portion: Any) = b.addArray(portion as ShortArray)

        override fun build(): ShortArray = b.buildArrayOfShort()
    }

    override fun makeBuilder(): Builder = MyBuilder()
}


class QueryResultArrayOfIntLayout : QueryResultLayout<IntArray>() {

    override val interResultKind: InterResultKind = RES_PRIMITIVE_ARRAY

    override val row: QueryRowLayout<*> = QueryRowPrimitiveLayout(JavaPrimitiveKind.JAVA_INT)

    /// Result builder \\\

    inner class MyBuilder: Builder() {

        val b = IntArrayBuilder()

        override fun clear() = b.clear()

        override fun add(portion: Any) = b.addArray(portion as IntArray)

        override fun build(): IntArray = b.buildArrayOfInt()
    }

    override fun makeBuilder(): Builder = MyBuilder()
}


class QueryResultArrayOfLongLayout : QueryResultLayout<LongArray>() {

    override val interResultKind: InterResultKind = RES_PRIMITIVE_ARRAY

    override val row: QueryRowLayout<*> = QueryRowPrimitiveLayout(JavaPrimitiveKind.JAVA_LONG)

    /// Result builder \\\

    inner class MyBuilder: Builder() {

        val b = LongArrayBuilder()

        override fun clear() = b.clear()

        override fun add(portion: Any) = b.addArray(portion as LongArray)

        override fun build(): LongArray = b.buildArrayOfLong()
    }

    override fun makeBuilder(): Builder = MyBuilder()
}


class QueryResultArrayLayout<R>(override val row: QueryRowLayout<R>) : QueryResultLayout<Array<out R>>() {

    override val interResultKind = RES_TABLE

    /// Result builder \\\

    inner class MyBuilder: Builder() {

        val b = ArrayBuilder<R>(row.rowClass)

        override fun clear() = b.clear()

        override fun add(portion: Any) = b.addArray(portion as Array<out Any?>) { row.transform(it!!) }

        override fun build(): Array<out R> = b.buildArray()
    }

    override fun makeBuilder() = MyBuilder()
}


class QueryResultListLayout<R>(override val row: QueryRowLayout<R>) : QueryResultLayout<List<R>>() {

    override val interResultKind = RES_TABLE

    /// Result builder \\\

    inner class MyBuilder: Builder() {

        var b = ArrayBuilder<R>(row.rowClass)

        override fun clear() = b.clear()

        override fun add(portion: Any) = b.addArray(portion as Array<out Any?>) { row.transform(it!!) }

        override fun build(): List<R> = b.buildList()
    }

    override fun makeBuilder() = MyBuilder()
}

class QueryResultSetLayout<R>(override val row: QueryRowLayout<R>) : QueryResultLayout<Set<R>>() {

    override val interResultKind = RES_TABLE

    /// Result builder \\\

    inner class MyBuilder: Builder() {

        var b = ArrayBuilder<R>(row.rowClass)

        override fun clear() = b.clear()

        override fun add(portion: Any) = b.addArray(portion as Array<out Any?>) { row.transform(it!!) }

        override fun build(): Set<R> = b.buildSet()
    }

    override fun makeBuilder() = MyBuilder()
}


class QueryResultMapLayout<K,V>(val keyClass: Class<K>, val valueClass: Class<V>) : QueryResultLayout<Map<K,V>>() {

    override val interResultKind = RES_TABLE

    override val row = QueryRowMapEntryLayout(keyClass, valueClass)

    /// Result builder \\\

    inner class MyBuilder: Builder() {

        @Suppress("unchecked_cast")
        var b = ArrayBuilder<SerializableMapEntry<K,V>>(SerializableMapEntry::class.java as Class<SerializableMapEntry<K,V>>)

        override fun clear() = b.clear()

        override fun add(portion: Any) = b.addArray(portion as Array<out Any?>) { row.transform(it!!) }

        override fun build(): Map<K,V> {
            val n = b.size()
            if (n == 0) return Collections.emptyMap<K,V>()
            val map = HashMap<K,V>(n)
            b.forEach { map.put(it.key, it.value) }
            return map
        }
    }

    override fun makeBuilder() = MyBuilder()
}
