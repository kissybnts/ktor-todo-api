package kissybnts.ktor_todo.app.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import kissybnts.ktor_todo.app.enumeration.AuthProvider

sealed class AuthProviderUser

@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubUser(val id: Int, val login: String, val name: String? = null, val avatarUrl: String): AuthProviderUser()

fun AuthProviderUser.toOAuthUser(providerCode: String): OAuthUser {
    return when (this) {
        is GitHubUser -> OAuthUser(name?: login, avatarUrl, AuthProvider.GitHub, providerCode, id)
    }
}

data class OAuthUser(val name: String, val imageUrl: String, val providerType: AuthProvider, val providerCode: String, val providerId: Int)