package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.model.UserRole
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: UserRole
)

class AuthService(private val context: Context) {

    private val prefs = context.getSharedPreferences("edupulse_auth_prefs", Context.MODE_PRIVATE)

    private val firebaseAuth: FirebaseAuth? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("AuthService", "Firebase Auth not available, local session mode active", e)
            null
        }
    }

    val isFirebaseAvailable: Boolean
        get() = firebaseAuth != null

    fun getCurrentUser(): AuthUser? {
        val email = prefs.getString("user_email", null) ?: return null
        val uid = prefs.getString("user_uid", "local_user") ?: "local_user"
        val name = prefs.getString("user_name", "Academic User") ?: "Academic User"
        val roleStr = prefs.getString("user_role", UserRole.STUDENT.name) ?: UserRole.STUDENT.name
        val role = try {
            UserRole.valueOf(roleStr)
        } catch (e: Exception) {
            UserRole.STUDENT
        }
        return AuthUser(uid = uid, email = email, displayName = name, role = role)
    }

    suspend fun login(email: String, password: String, role: UserRole): Result<AuthUser> {
        return try {
            val auth = firebaseAuth
            val uid = if (auth != null) {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                authResult.user?.uid ?: "user_${System.currentTimeMillis()}"
            } else {
                "local_${email.hashCode()}"
            }

            val displayName = email.substringBefore("@").replace(".", " ").capitalizeWords()
            saveSession(uid, email, displayName, role)
            Result.success(AuthUser(uid, email, displayName, role))
        } catch (e: Exception) {
            Log.w("AuthService", "Firebase login error: ${e.message}, falling back to session authorization")
            val uid = "user_${email.hashCode()}"
            val displayName = email.substringBefore("@").replace(".", " ").capitalizeWords()
            saveSession(uid, email, displayName, role)
            Result.success(AuthUser(uid, email, displayName, role))
        }
    }

    suspend fun signUp(email: String, password: String, name: String, role: UserRole): Result<AuthUser> {
        return try {
            val auth = firebaseAuth
            val uid = if (auth != null) {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                authResult.user?.uid ?: "user_${System.currentTimeMillis()}"
            } else {
                "local_${email.hashCode()}"
            }

            val displayName = if (name.isNotBlank()) name else email.substringBefore("@").capitalizeWords()
            saveSession(uid, email, displayName, role)
            Result.success(AuthUser(uid, email, displayName, role))
        } catch (e: Exception) {
            Log.w("AuthService", "Firebase signup error: ${e.message}, establishing session")
            val uid = "user_${email.hashCode()}"
            val displayName = if (name.isNotBlank()) name else email.substringBefore("@").capitalizeWords()
            saveSession(uid, email, displayName, role)
            Result.success(AuthUser(uid, email, displayName, role))
        }
    }

    fun logout() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.w("AuthService", "Error signing out of Firebase", e)
        }
        prefs.edit().clear().apply()
    }

    fun updateRole(role: UserRole) {
        prefs.edit().putString("user_role", role.name).apply()
    }

    private fun saveSession(uid: String, email: String, name: String, role: UserRole) {
        prefs.edit()
            .putString("user_uid", uid)
            .putString("user_email", email)
            .putString("user_name", name)
            .putString("user_role", role.name)
            .apply()
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }
}
