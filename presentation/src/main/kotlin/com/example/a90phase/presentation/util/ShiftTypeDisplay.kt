package com.example.a90phase.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.a90phase.domain.entities.ShiftType
import com.example.a90phase.presentation.R

/**
 * Domain stays text-free — [ShiftType] carries no display string, so callers resolve the
 * user-facing label here instead of reading `ShiftType.displayName`.
 */
@Composable
fun ShiftType.displayNameRes(): String = when (this) {
    ShiftType.LongerLatency -> stringResource(R.string.shift_longer_latency)
    ShiftType.LongerCycles -> stringResource(R.string.shift_longer_cycles)
    ShiftType.FewerCycles -> stringResource(R.string.shift_fewer_cycles)
}
