package com.manage.health.healthtrackerapplication.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.manage.health.healthtrackerapplication.data.database.HealthDataBase
import com.manage.health.healthtrackerapplication.data.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject



@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val healthRepository: HealthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val currentUser = auth.currentUser
            _user.value = currentUser

            if (currentUser != null) {
                viewModelScope.launch {
                    try {
                        healthRepository.syncLocalDataToFirebase()
                    } catch (_: Exception) {
                    }
                }
            } else {
//                HealthDataBase.clearInstance()
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Sign In Failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Sign Up Failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        try {
            firebaseAuth.signOut()
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Sign Out Failed"
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}