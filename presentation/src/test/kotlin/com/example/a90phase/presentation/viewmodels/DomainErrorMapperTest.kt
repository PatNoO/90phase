package com.example.a90phase.presentation.viewmodels

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.presentation.R
import org.junit.Assert.assertEquals
import org.junit.Test

class DomainErrorMapperTest {

    @Test
    fun `maps each DomainError variant to a distinct string resource`() {
        val cases = listOf(
            DomainError.DatabaseError(null) to R.string.error_database,
            DomainError.CalculationFailed(null) to R.string.error_calculation_failed,
            DomainError.SyncError(null) to R.string.error_sync,
            DomainError.NetworkError(null) to R.string.error_sync,
            DomainError.NotFound(null) to R.string.error_not_found,
            DomainError.InsufficientData(null) to R.string.error_insufficient_data,
            DomainError.ValidationError("field", "reason") to R.string.error_validation,
            DomainError.DiscoveryPhaseAlreadyActive(null) to R.string.error_discovery_already_active,
            DomainError.PermissionDenied("perm") to R.string.error_permission_denied,
            DomainError.AuthFailed(null) to R.string.error_auth_failed,
            DomainError.UserNotAuthenticated() to R.string.error_auth_failed,
        )

        cases.forEach { (error, expectedRes) ->
            assertEquals(expectedRes, error.toMessageRes())
        }
    }
}
