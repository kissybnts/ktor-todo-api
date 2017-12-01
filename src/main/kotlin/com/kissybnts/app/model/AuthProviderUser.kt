package com.kissybnts.app.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.kissybnts.app.repository.CushioningUser
import com.kissybnts.app.table.AuthProvider

sealed class AuthProviderUser

@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubUser(val id: Int, val login: String, val name: String, val avatarUrl: String): AuthProviderUser()

fun AuthProviderUser.toCushioningUser(providerCode: String): CushioningUser {
    return when (this) {
        is GitHubUser -> CushioningUser(name, avatarUrl, AuthProvider.GitHub, providerCode, id)
    }
}