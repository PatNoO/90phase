package com.example.a90phase.presentation.viewmodels

import androidx.annotation.StringRes
import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.presentation.R

@StringRes
internal fun DomainError.toMessageRes(): Int = when (this) {
    is DomainError.DatabaseError -> R.string.error_database
    is DomainError.CalculationFailed -> R.string.error_calculation_failed
    is DomainError.SyncError, is DomainError.NetworkError -> R.string.error_sync
    is DomainError.NotFound -> R.string.error_not_found
    is DomainError.InsufficientData -> R.string.error_insufficient_data
    is DomainError.ValidationError -> R.string.error_validation
    is DomainError.DiscoveryPhaseAlreadyActive -> R.string.error_discovery_already_active
    is DomainError.PermissionDenied -> R.string.error_permission_denied
    is DomainError.AuthFailed, is DomainError.UserNotAuthenticated -> R.string.error_auth_failed
}
