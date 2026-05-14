package com.example.a90phase.domain

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResultTest {
    @Test
    fun `Success getOrNull returns data`() {
        val result = Result.Success("hello")
        assertEquals("hello", result.getOrNull())
    }

    @Test
    fun `Error getOrNull returns null`() {
        val result = Result.Error(DomainError.DatabaseError("db fail"))
        assertNull(result.getOrNull())
    }

    @Test
    fun `Loading getOrNull returns null`() {
        assertNull(Result.Loading.getOrNull())
    }

    @Test
    fun `map transforms Success data`() {
        val result = Result.Success(2)
        val mapped = result.map { it * 3 }
        assertEquals(Result.Success(6), mapped)
    }

    @Test
    fun `map on Error propagates error unchanged`() {
        val error = DomainError.CalculationFailed("oops")
        val result: Result<Int> = Result.Error(error)
        val mapped = result.map { it * 3 }
        assertTrue(mapped is Result.Error)
        assertEquals(error, (mapped as Result.Error).error)
    }

    @Test
    fun `map on Loading propagates Loading`() {
        val result: Result<Int> = Result.Loading
        val mapped = result.map { it * 3 }
        assertTrue(mapped is Result.Loading)
    }

    @Test
    fun `onSuccess invoked for Success`() {
        var called = false
        Result.Success(42).onSuccess { called = true }
        assertTrue(called)
    }

    @Test
    fun `onSuccess not invoked for Error`() {
        var called = false
        Result.Error(DomainError.NetworkError(null)).onSuccess { called = true }
        assertTrue(!called)
    }

    @Test
    fun `onError invoked for Error`() {
        var called = false
        Result.Error(DomainError.SyncError("sync fail")).onError { called = true }
        assertTrue(called)
    }

    @Test
    fun `onError not invoked for Success`() {
        var called = false
        Result.Success("ok").onError { called = true }
        assertTrue(!called)
    }

    @Test
    fun `onSuccess and onError chain returns same instance`() {
        val original = Result.Success("data")
        val returned = original.onSuccess { }.onError { }
        assertEquals(original, returned)
    }

    @Test
    fun `ValidationError message includes field and reason`() {
        val error = DomainError.ValidationError("rating", "must be 1-5")
        assertEquals("Invalid rating: must be 1-5", error.message)
    }

    @Test
    fun `UserNotAuthenticated has default message`() {
        val error = DomainError.UserNotAuthenticated()
        assertEquals("User not signed in", error.message)
    }
}
