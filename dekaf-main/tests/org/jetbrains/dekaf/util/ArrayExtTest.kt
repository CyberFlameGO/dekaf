package org.jetbrains.dekaf.util


import org.jetbrains.dekaf.assertions.expected
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test


@Tag("basic")
class ArrayExtTest {


    @Test
    fun chopBy_basic() {

        arrayOf(1L, 2L, 3L) chopBy 5 expected arrayOf(arrayOf(1L, 2L, 3L))
        arrayOf(1L, 2L, 3L) chopBy 3 expected arrayOf(arrayOf(1L, 2L, 3L))

        arrayOf(1L, 2L, 3L, 4L, 5L) chopBy 3 expected arrayOf(arrayOf(1L, 2L, 3L), arrayOf(4L, 5L))

    }

    @Test
    fun chopBy_zeroSlice() {
        expectIllegalArgumentException {
            arrayOf(1L, 2L) chopBy 0
        }
    }

    @Test
    fun chopAndPadBy_basic() {

        arrayOf(1L, 2L, 3L) chopAndPadBy 5 expected arrayOf(arrayOf(1L, 2L, 3L, null, null))
        arrayOf(1L, 2L, 3L) chopAndPadBy 3 expected arrayOf(arrayOf(1L, 2L, 3L))

        arrayOf(1L, 2L, 3L, 4L, 5L) chopAndPadBy 3 expected arrayOf(arrayOf(1L, 2L, 3L), arrayOf(4L, 5L, null))

    }

    @Test
    fun chopAndPadBy_zeroSlice() {
        expectIllegalArgumentException {
            arrayOf(1L, 2L) chopAndPadBy 0
        }
    }



}