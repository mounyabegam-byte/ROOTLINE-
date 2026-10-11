package com.example.data.auth

import com.example.model.UserRole

data class AuthUser(
    val uid: String,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val department: String,
    val rollNumber: String? = null
)
