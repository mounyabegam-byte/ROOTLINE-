package com.example.data.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.model.UserRole
import kotlinx.coroutines.flow.StateFlow

/**
 * Represents the distinct states of authentication.
 */
sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
    data class Error(val message: String) : AuthState()
    data object Unauthenticated : AuthState()
}

/**
 * AuthContext manages and provides application-wide authentication context,
 * active user session persistence, and login/signup lifecycle operations.
 */
interface AuthContext {
    val currentUser: StateFlow<AuthUser?>
    val currentRole: StateFlow<UserRole>
    val authState: StateFlow<AuthState>
    val isLoading: StateFlow<Boolean>
    val authError: StateFlow<String?>

    val isAuthenticated: Boolean
        get() = currentUser.value != null

    fun login(email: String, pass: String, onSuccess: (() -> Unit)? = null)

    fun signup(
        name: String,
        email: String,
        pass: String,
        role: UserRole,
        department: String,
        rollNumber: String?,
        onSuccess: (() -> Unit)? = null
    )

    fun logout()

    fun sendPasswordReset(email: String, onComplete: ((Boolean, String?) -> Unit)? = null)

    fun clearError()

    fun refreshSession()
}

/**
 * CompositionLocal to expose the active AuthContext throughout the Compose hierarchy.
 */
val LocalAuthContext = staticCompositionLocalOf<AuthContext> {
    error("No AuthContext provided. Wrap your composables in ProvideAuthContext.")
}

@Composable
fun ProvideAuthContext(
    authContext: AuthContext,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalAuthContext provides authContext) {
        content()
    }
}
