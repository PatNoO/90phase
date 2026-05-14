package com.example.a90phase.domain.common

sealed class DomainError(
    open val message: String?,
) {
    data class DatabaseError(
        override val message: String?,
    ) : DomainError(message)

    data class SyncError(
        override val message: String?,
    ) : DomainError(message)

    data class NetworkError(
        override val message: String?,
    ) : DomainError(message)

    data class NotFound(
        override val message: String?,
    ) : DomainError(message)

    data class CalculationFailed(
        override val message: String?,
    ) : DomainError(message)

    data class ValidationError(
        val field: String,
        val reason: String,
    ) : DomainError("Invalid $field: $reason")

    data class InsufficientData(
        override val message: String?,
    ) : DomainError(message)

    data class DiscoveryPhaseAlreadyActive(
        override val message: String?,
    ) : DomainError(message)

    data class PermissionDenied(
        val permission: String,
    ) : DomainError("Permission denied: $permission")

    data class AuthFailed(
        override val message: String?,
    ) : DomainError(message)

    data class UserNotAuthenticated(
        override val message: String? = "User not signed in",
    ) : DomainError(message)
}
