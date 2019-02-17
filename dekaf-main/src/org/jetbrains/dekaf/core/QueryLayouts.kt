@file:JvmName("QueryLayouts")
package org.jetbrains.dekaf.core


/// RESULT LAYOUT \\\

fun layoutExistence(): QueryResultLayout<Boolean> = QueryResultExistenceLayout()

fun<V> layoutSingleValueOf(valueClass: Class<V>): QueryResultLayout<V> = QueryResultOneRowLayout(rowValueOf(valueClass))
inline fun<reified V> layoutSingleValueOf(): QueryResultLayout<V> = layoutSingleValueOf(V::class.java)

fun layoutArrayOfShort(): QueryResultLayout<ShortArray> = QueryResultArrayOfShortLayout()

fun layoutArrayOfInt(): QueryResultLayout<IntArray> = QueryResultArrayOfIntLayout()

fun layoutArrayOfLong(): QueryResultLayout<LongArray> = QueryResultArrayOfLongLayout()

fun<R> layoutOneRowOf(row: QueryRowLayout<R>): QueryResultLayout<R> = QueryResultOneRowLayout(row)

fun<R> layoutArrayOf(row: QueryRowLayout<R>): QueryResultLayout<Array<out R>> = QueryResultArrayLayout(row)

fun<R> layoutListOf(row: QueryRowLayout<R>): QueryResultLayout<List<R>> = QueryResultListLayout(row)

fun<R> layoutSetOf(row: QueryRowLayout<R>): QueryResultLayout<Set<R>> = QueryResultSetLayout(row)

fun<K,V> layoutMapOf(keyClass: Class<K>, valueClass: Class<V>): QueryResultLayout<Map<K,V>> = QueryResultMapLayout(keyClass, valueClass)
inline fun<reified K, reified V> layoutMapOf(): QueryResultLayout<Map<K,V>> = layoutMapOf(K::class.java, V::class.java)



/// ROW LAYOUTS \\\

fun<V> rowValueOf(valueClass: Class<V>): QueryRowLayout<V> = QueryRowOneValueLayout(valueClass)
inline fun<reified V> rowValueOf(): QueryRowLayout<V> = rowValueOf(V::class.java)

fun<R> rowStructOf(cortegeClass: Class<R>): QueryRowLayout<R> = QueryRowStructLayout(cortegeClass)
inline fun<reified R> rowStructOf(): QueryRowLayout<R> = rowStructOf(R::class.java)

fun<E> rowArrayOf(baseElementClass: Class<E>): QueryRowLayout<Array<out E?>> = QueryRowArrayOfValuesLayout<E>(baseElementClass)
inline fun<reified E> rowArrayOf(): QueryRowLayout<Array<out E?>> = rowArrayOf(E::class.java)

