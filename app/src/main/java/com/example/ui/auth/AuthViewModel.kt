package com.example.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthContext
import com.example.data.auth.AuthState
import com.example.data.auth.AuthUser
import com.example.data.auth.FirebaseAuthService
import com.example.data.auth.UserRoleProvider
import com.example.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AuthViewModel manages user authentication sessions, login, signup,
 * password reset, and robust session persistence across app restarts.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application), AuthContext {

    val authService = FirebaseAuthService(application)
    val userRoleProvider = UserRoleProvider(application, viewModelScope)

    // Initial session restored immediately from persistent storage
    private val initialUser = authService.getPersistedUser()

    private val _currentUser = MutableStateFlow<AuthUser?>(initialUser)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(
        if (initialUser != null) AuthState.Authenticated(initialUser) else AuthState.Unauthenticated
    )
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    override val authError: StateFlow<String?> = _authError.asStateFlow()

    override val currentRole: StateFlow<UserRole> = userRoleProvider.currentRole

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val firebaseUser = auth.currentUser
        if (firebaseUser == null && _currentUser.value != null && !_currentUser.value!!.uid.startsWith("local_")) {
            // Firebase user logged out
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
            userRoleProvider.setUser(null)
        } else if (firebaseUser != null && _currentUser.value == null) {
            // Firebase user restored or signed in externally
            refreshSession()
        }
    }

    init {
        // Ensure role provider has the persisted user session
        initialUser?.let { user ->
            userRoleProvider.setUser(user)
        }

        // Listen for Firebase auth state changes
        try {
            authService.addAuthStateListener(authStateListener)
        } catch (_: Exception) {
            // Handled gracefully in offline/fallback mode
        }
    }

    suspend fun suspendLogin(email: String, pass: String): Result<AuthUser> {
        if (email.isBlank() || pass.isBlank()) {
            val msg = "Email and password are required."
            _authError.value = msg
            _authState.value = AuthState.Error(msg)
            return Result.failure(Exception(msg))
        }

        _isLoading.value = true
        _authError.value = null
        _authState.value = AuthState.Loading

        val result = authService.signIn(email.trim(), pass)
        result.fold(
            onSuccess = { user ->
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
                userRoleProvider.setUser(user)
                _isLoading.value = false
            },
            onFailure = { error ->
                val message = error.message ?: "Authentication failed."
                _authError.value = message
                _authState.value = AuthState.Error(message)
                _isLoading.value = false
            }
        )
        return result
    }

    suspend fun suspendSignup(
        name: String,
        email: String,
        pass: String,
        role: UserRole,
        department: String,
        rollNumber: String?
    ): Result<AuthUser> {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            val msg = "Please complete all required fields."
            _authError.value = msg
            _authState.value = AuthState.Error(msg)
            return Result.failure(Exception(msg))
        }

        _isLoading.value = true
        _authError.value = null
        _authState.value = AuthState.Loading

        val result = authService.signUp(name.trim(), email.trim(), pass, role, department, rollNumber)
        result.fold(
            onSuccess = { user ->
                _currentUser.value = user
                _authState.value = AuthState.Authenticated(user)
                userRoleProvider.setUser(user)
                _isLoading.value = false
            },
            onFailure = { error ->
                val message = error.message ?: "Sign up failed."
                _authError.value = message
                _authState.value = AuthState.Error(message)
                _isLoading.value = false
            }
        )
        return result
    }

    override fun login(email: String, pass: String, onSuccess: (() -> Unit)?) {
        viewModelScope.launch {
            val res = suspendLogin(email, pass)
            if (res.isSuccess) {
                onSuccess?.invoke()
            }
        }
    }

    override fun signup(
        name: String,
        email: String,
        pass: String,
        role: UserRole,
        department: String,
        rollNumber: String?,
        onSuccess: (() -> Unit)?
    ) {
        viewModelScope.launch {
            val res = suspendSignup(name, email, pass, role, department, rollNumber)
            if (res.isSuccess) {
                onSuccess?.invoke()
            }
        }
    }

    override fun logout() {
        authService.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
        userRoleProvider.setUser(null)
        _authError.value = null
    }

    override fun sendPasswordReset(email: String, onComplete: ((Boolean, String?) -> Unit)?) {
        if (email.isBlank()) {
            onComplete?.invoke(false, "Please enter your email address.")
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = authService.sendPasswordReset(email.trim())
            _isLoading.value = false
            result.fold(
                onSuccess = {
                    onComplete?.invoke(true, "Password reset email sent to $email.")
                },
                onFailure = { error ->
                    onComplete?.invoke(false, error.message ?: "Failed to send reset link.")
                }
            )
        }
    }

    override fun clearError() {
        _authError.value = null
        if (_authState.value is AuthState.Error) {
            _authState.value = if (_currentUser.value != null) {
                AuthState.Authenticated(_currentUser.value!!)
            } else {
                AuthState.Unauthenticated
            }
        }
    }

    fun switchRole(newRole: UserRole) {
        _currentUser.value?.let { user ->
            val updated = user.copy(role = newRole)
            _currentUser.value = updated
            _authState.value = AuthState.Authenticated(updated)
            userRoleProvider.setUser(updated)
        }
    }

    override fun refreshSession() {
        val user = authService.getPersistedUser()
        _currentUser.value = user
        if (user != null) {
            _authState.value = AuthState.Authenticated(user)
            userRoleProvider.setUser(user)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            authService.removeAuthStateListener(authStateListener)
        } catch (_: Exception) {
        }
    }
}
