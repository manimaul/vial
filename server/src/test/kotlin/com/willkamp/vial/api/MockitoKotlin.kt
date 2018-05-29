package com.willkamp.vial.api

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing
import org.mockito.stubbing.Stubber

object MockitoKotlin {
    inline fun <reified T> argCaptor() : ArgumentCaptor<T> = ArgumentCaptor.forClass(T::class.java)

    inline fun <reified T> whenever(obj: T) : OngoingStubbing<T> = Mockito.`when`(obj)!!

    fun <T> Stubber.whenever(obj: T) : T {
        return this.`when`(obj) as T
    }

    inline fun <reified T> mock(): T = Mockito.mock(T::class.java)

    inline fun <reified T> isA(): T = Mockito.isA(T::class.java)

    inline fun <reified T> nullable(): T? = Mockito.nullable(T::class.java)

    fun <T> anyArg(): T {
        val retVal: T? = ArgumentMatchers.any()
        @Suppress("UNCHECKED_CAST")
        return retVal as T
    }

    fun <T> any(): T {
        val retVal: T? = Mockito.any()
        @Suppress("UNCHECKED_CAST")
        return retVal as T
    }

    fun <T> eq(obj: T): T {
        val retVal: T? = Mockito.eq(obj)
        @Suppress("UNCHECKED_CAST")
        return retVal as T
    }

    fun <T> eqNull(): T? = Mockito.eq(null)

    fun <T> ArgumentCaptor<T>.capture(): T {
        val retVal: T? = capture()
        @Suppress("UNCHECKED_CAST")
        return retVal as T
    }
}


