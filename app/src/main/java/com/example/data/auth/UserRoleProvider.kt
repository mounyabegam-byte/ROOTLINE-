package com.example.data.auth

import android.content.Context
import android.util.Log
import com.example.model.UserRole
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserRoleProvider(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val prefs = context.getSharedPreferences("edupulse_role_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val _currentRole = MutableStateFlow(UserRole.STUDENT)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w("UserRoleProvider", "Firestore not available: ${e.message}")
            null
        }
    }

    init {
        // Load cached role
        val savedRoleStr = prefs.getString("user_role", UserRole.STUDENT.name)
        val savedRole = try {
            UserRole.valueOf(savedRoleStr ?: UserRole.STUDENT.name)
        } catch (e: Exception) {
            UserRole.STUDENT
        }
        _currentRole.value = savedRole
    }

    fun setUser(user: AuthUser?) {
        _currentUser.value = user
        if (user != null) {
            _currentRole.value = user.role
            prefs.edit().putString("user_role", user.role.name).apply()
            listenToUserRoleInFirestore(user.uid)
        }
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
        prefs.edit().putString("user_role", role.name).apply()
        _currentUser.value?.let { user ->
            _currentUser.value = user.copy(role = role)
            updateRoleInFirestore(user.uid, role)
        }
    }

    private fun listenToUserRoleInFirestore(uid: String) {
        val db = firestore ?: return
        try {
            db.collection("users").document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("UserRoleProvider", "Error listening to user document: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val roleStr = snapshot.getString("role")
                        if (!roleStr.isNullOrBlank()) {
                            try {
                                val role = UserRole.valueOf(roleStr.uppercase())
                                _currentRole.value = role
                                prefs.edit().putString("user_role", role.name).apply()
                                _currentUser.value = _currentUser.value?.copy(role = role)
                            } catch (e: Exception) {
                                Log.w("UserRoleProvider", "Unknown role from firestore: $roleStr")
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w("UserRoleProvider", "Firestore listen error: ${e.message}")
        }
    }

    private fun updateRoleInFirestore(uid: String, role: UserRole) {
        val db = firestore ?: return
        scope.launch(Dispatchers.IO) {
            try {
                db.collection("users").document(uid)
                    .update("role", role.name)
            } catch (e: Exception) {
                Log.w("UserRoleProvider", "Failed to update role in firestore: ${e.message}")
            }
        }
    }

    fun isStudent(): Boolean = _currentRole.value == UserRole.STUDENT
    fun isFaculty(): Boolean = _currentRole.value == UserRole.FACULTY
    fun isAdmin(): Boolean = _currentRole.value == UserRole.ADMIN || _currentRole.value == UserRole.HOD
}
