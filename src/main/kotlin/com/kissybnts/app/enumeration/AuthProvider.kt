package com.kissybnts.app.enumeration

enum class AuthProvider {
    GitHub;

    companion object {
        fun fromName(name: String): AuthProvider {
            return when (name) {
                "github" -> AuthProvider.GitHub
                else -> throw IllegalArgumentException("Un supported type: $name")
            }
        }
    }
}