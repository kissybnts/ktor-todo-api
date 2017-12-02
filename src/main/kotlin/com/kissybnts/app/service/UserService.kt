package com.kissybnts.app.service

import com.kissybnts.app.enumeration.AuthProvider
import com.kissybnts.app.model.GitHubUser
import com.kissybnts.app.model.UserModel
import com.kissybnts.app.model.toCushioningUser
import com.kissybnts.app.pipeline.objectMapper
import com.kissybnts.app.repository.CushioningUser
import com.kissybnts.app.repository.UserRepository
import com.kissybnts.exception.ProviderAuthenticationErrorException
import io.ktor.client.HttpClient
import io.ktor.client.backend.apache.ApacheBackend
import io.ktor.client.bodyStream
import io.ktor.client.call.call
import io.ktor.client.request.header
import io.ktor.client.utils.url
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod

class UserService(private val userRepository: UserRepository = UserRepository) {

    suspend fun loginWithProvider(providerType: AuthProvider, accessToken: String, code: String): UserModel {
        val cushioningUser = acquireUser(providerType, accessToken, code)
        return loginUpsert(providerType, cushioningUser, code)
    }

    private suspend fun acquireUser(providerType: AuthProvider, accessToken: String, code: String): CushioningUser {
        val client = HttpClient(ApacheBackend)

        return try {
            client.acquireUser(providerType, accessToken, code)
        } catch (ex: Exception) {
            throw ProviderAuthenticationErrorException()
        } finally {
            client.close()
        }
    }

    private fun loginUpsert(providerType: AuthProvider, cushioningUser: CushioningUser, code: String): UserModel {
        return userRepository.selectByProvider(providerType, cushioningUser.providerId)?.let {
            userRepository.loginUpdate(it, code)
        } ?: userRepository.insert(cushioningUser)
    }

    private suspend fun HttpClient.acquireUser(providerType: AuthProvider, accessToken: String, code: String): CushioningUser {
        return when (providerType) {
            AuthProvider.GitHub -> {
                val githubUser = acquireGitHubUser(accessToken)
                githubUser.toCushioningUser(code)
            }
        }
    }

    private suspend fun HttpClient.acquireGitHubUser(accessToken: String): GitHubUser {
        val response = call {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            url("https", "api.github.com", 443, "user")
            method = HttpMethod.Get
        }
        return objectMapper.readValue(response.bodyStream.reader(Charsets.UTF_8), GitHubUser::class.java)
    }
}