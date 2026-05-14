package com.example.a90phase.domain.common

sealed class Result<out T> {
    data class Success<T>(
        val data: T,
    ) : Result<T>()

    data class Error(
        val error: DomainError,
    ) : Result<Nothing>()

    data object Loading : Result<Nothing>()

    fun <R> map(transform: (T) -> R): Result<R> =
        when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }

    fun getOrNull(): T? = if (this is Success) data else null

    fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    fun onError(action: (DomainError) -> Unit): Result<T> {
        if (this is Error) action(error)
        return this
    }
}
