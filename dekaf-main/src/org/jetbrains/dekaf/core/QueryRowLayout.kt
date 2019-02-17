package org.jetbrains.dekaf.core

import org.jetbrains.dekaf.inter.InterRowKind
import org.jetbrains.dekaf.util.*
import org.jetbrains.dekaf.util.Objects.castTo
import org.jetbrains.dekaf.util.Objects.castToArrayOf
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Modifier


sealed class QueryRowLayout<R> {

    abstract val rowClass: Class<R>


    /// Definition functions \\\

    open fun interRowKind(): InterRowKind = InterRowKind.ROW_OBJECTS

    open fun interPrimitiveKind(): JavaPrimitiveKind? = null

    open fun interBaseComponentClass(): Class<*>? = null

    open fun interColumnNames(): Array<String>? = null

    open fun interComponentClasses(): Array<Class<*>>? = null


    /// Data manipulation functions \\\

    abstract fun transform(a: Any): R

}


class QueryRowExistenceLayout: QueryRowLayout<Boolean>()
{
    override val rowClass: Class<Boolean>
        get() = Boolean::class.java

    override fun interRowKind(): InterRowKind = InterRowKind.ROW_NONE

    override fun transform(a: Any): Boolean = java.lang.Boolean.TRUE
}


class QueryRowPrimitiveLayout(val primitiveKind: JavaPrimitiveKind): QueryRowLayout<Any>()
{
    override val rowClass: Class<Any>
        get() = primitiveKind.primitiveClass

    override fun interRowKind(): InterRowKind = InterRowKind.ROW_ONE_VALUE

    override fun interBaseComponentClass(): Class<*> = primitiveKind.primitiveClass

    override fun transform(a: Any): Nothing =
            throw IllegalStateException("The method QueryRowPrimitiveLayout.transform() must be never called")
}


class QueryRowOneValueLayout<V>
(
    val valueClass: Class<V>
)
    : QueryRowLayout<V>()
{
    override val rowClass: Class<V>
        get() = valueClass

    override fun interRowKind(): InterRowKind = InterRowKind.ROW_ONE_VALUE

    override fun interBaseComponentClass(): Class<*> = valueClass

    override fun transform(a: Any): V = castTo(valueClass, a)
}


class QueryRowArrayOfValuesLayout<E>
(
    val componentClass: Class<E>
)
    : QueryRowLayout<Array<out E?>>()
{
    override val rowClass: Class<Array<out E?>> = ArrayUtil.getArrayClass(componentClass)

    override fun interRowKind(): InterRowKind = InterRowKind.ROW_OBJECTS

    override fun interBaseComponentClass(): Class<*> = componentClass

    override fun transform(a: Any): Array<out E?> {
        return castToArrayOf(componentClass, a)
    }
}


@Suppress("platform_class_mapped_to_kotlin")
class QueryRowStructLayout<R> : QueryRowLayout<R>
{
    val cortegeClass: Class<R>

    override val rowClass: Class<R>
        get() = cortegeClass
    
    internal val fields: List<Field>
    internal val constructor: Constructor<R>

    internal val commonComponentClass: Class<out Object?>?
    internal val componentClasses: Array<Class<*>>
    internal val componentNames: Array<String>

    constructor(cortegeClass: Class<R>) : super() {
        this.cortegeClass = cortegeClass
        constructor = cortegeClass.getDefaultConstructor()
        fields = cortegeClass.declaredFields.filter(Field::isApplicable)
        fields.forEach { it.isAccessible = true }
        val n = fields.size
        componentNames = Array(n) { i -> fields[i].name }
        componentClasses = Array(n) { i -> fields[i].type }
        commonComponentClass = null // TODO find the common component class
    }

    override fun interBaseComponentClass(): Class<*>? = commonComponentClass
    override fun interColumnNames(): Array<String>? = componentNames
    override fun interComponentClasses(): Array<Class<*>>? = componentClasses

    override fun transform(a: Any): R {
        val elementClass: Class<out Object?> = commonComponentClass ?: Object::class.java
        val array: Array<out Any?> = castToArrayOf(elementClass, a)
        val struct: R = constructor.instantiate()
        val n = fields.size
        for (i in 0..n-1) {
            val value = array[i]
            if (value != null) {
                val f = fields[i]
                f.set(struct, value)
            }
        }
        return struct
    }

}


class QueryRowMapEntryLayout<K,V> : QueryRowLayout<SerializableMapEntry<*,*>>
{
    val keyClass: Class<K>
    val valueClass: Class<V>

    constructor(keyClass: Class<K>, valueClass: Class<V>) {
        this.keyClass = keyClass
        this.valueClass = valueClass
    }

    override fun interRowKind() = InterRowKind.ROW_MAP_ENTRY

    override val rowClass: Class<SerializableMapEntry<*,*>>
        get() = SerializableMapEntry::class.java

    override fun interComponentClasses() = arrayOf(keyClass, valueClass)

    override fun transform(a: Any): SerializableMapEntry<K,V> {
        @Suppress("UNCHECKED_CAST")
        val entry = a as SerializableMapEntry<K,V>
        return entry
    }

}


private fun Field.isApplicable(): Boolean {
    val m = this.modifiers
    return !Modifier.isPrivate(m) && !Modifier.isFinal(m) && !Modifier.isStatic(m) && !Modifier.isTransient(m)
}

