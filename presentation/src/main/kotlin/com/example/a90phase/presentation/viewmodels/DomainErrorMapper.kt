package com.example.a90phase.presentation.viewmodels

import com.example.a90phase.domain.common.DomainError

internal fun DomainError.toSwedishMessage(): String = when (this) {
    is DomainError.DatabaseError -> "Databasfel — försök igen"
    is DomainError.CalculationFailed -> "Beräkningen misslyckades — kontrollera inställningarna"
    is DomainError.SyncError, is DomainError.NetworkError -> "Synkfel — data sparas lokalt"
    is DomainError.NotFound -> "Data hittades inte"
    is DomainError.InsufficientData -> "Inte tillräckligt med data"
    is DomainError.ValidationError -> "Ogiltigt värde — kontrollera inmatningen"
    is DomainError.DiscoveryPhaseAlreadyActive -> "Utforskningsfasen är redan aktiv"
    is DomainError.PermissionDenied -> "Behörighet nekad"
    is DomainError.AuthFailed, is DomainError.UserNotAuthenticated -> "Inloggning misslyckades"
}
