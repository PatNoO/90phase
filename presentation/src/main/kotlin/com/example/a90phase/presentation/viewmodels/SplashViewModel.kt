package com.example.a90phase.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a90phase.domain.repositories.OnboardingRepository
import com.example.a90phase.domain.usecases.GetOnboardingStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SplashViewModel @Inject constructor(
    onboardingRepository: OnboardingRepository,
) : ViewModel() {

    private val getOnboardingStateUseCase = GetOnboardingStateUseCase(onboardingRepository)

    val isOnboardingCompleted = getOnboardingStateUseCase()
        .map { it.isCompleted }
        .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = null)
}
