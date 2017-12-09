package kissybnts.ktor_todo.app.service

import kissybnts.ktor_todo.app.enumeration.AuthProvider
import kissybnts.ktor_todo.app.model.GitHubUser
import kissybnts.ktor_todo.app.model.UserModel
import kissybnts.ktor_todo.app.model.toCushioningUser
import kissybnts.ktor_todo.app.objectMapper
import kissybnts.ktor_todo.app.model.OAuthUser
import kissybnts.ktor_todo.app.repository.UserRepository
import kissybnts.ktor_todo.exception.ProviderAuthenticationErrorException
import io.ktor.client.HttpClient
import io.ktor.client.backend.apache.ApacheBackend
import io.ktor.client.bodyStream
import io.ktor.client.call.call
import io.ktor.client.request.header
import io.ktor.client.utils.url
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kissybnts.ktor_todo.app.DefaultMessages
import kissybnts.ktor_todo.app.repository.UserRepositoryInterface
import kissybnts.ktor_todo.app.request.LoginRequest
import kissybnts.ktor_todo.app.request.SignUpRequest
import kissybnts.ktor_todo.app.utils.PasswordEncryption
import kissybnts.ktor_todo.exception.InvalidCredentialException
import kissybnts.ktor_todo.exception.UserNotFoundException
import java.sql.SQLIntegrityConstraintViolationException

class UserService(private val userRepository: UserRepositoryInterface = UserRepository) {

    fun signUpWithEmail(signUpRequest: SignUpRequest): UserModel {
        if (userRepository.countByEmail(signUpRequest.email) > 0) {
            throw IllegalStateException("${signUpRequest.email} is already registered.")
        }

        val encrypted = PasswordEncryption.passwordEncrypt(signUpRequest.password)
        try {
            return userRepository.insert(signUpRequest.name, signUpRequest.email, encrypted)
        } catch (e: SQLIntegrityConstraintViolationException) {
            throw IllegalStateException(e.message)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun loginWithProvider(providerType: AuthProvider, accessToken: String, code: String): UserModel {
        val cushioningUser = acquireUser(providerType, accessToken, code)
        return loginUpsert(providerType, cushioningUser, code)
    }

    fun loginWithEmail(request: LoginRequest): UserModel {
        val userCredentialPair = userRepository.selectByEmail(request.email)?: throw UserNotFoundException()
        if (PasswordEncryption.isCorrectPassword(request.password, userCredentialPair.second.password)) {
            return userCredentialPair.first
        } else {
            throw InvalidCredentialException()
        }
    }

    private suspend fun acquireUser(providerType: AuthProvider, accessToken: String, code: String): OAuthUser {
        val client = HttpClient(ApacheBackend)

        return try {
            client.acquireUser(providerType, accessToken, code)
        } catch (ex: Exception) {
            throw ProviderAuthenticationErrorException(ex.message?: DefaultMessages.Error.AUTH_PROCESS_FAILED)
        } finally {
            client.close()
        }
    }

    private fun loginUpsert(providerType: AuthProvider, OAuthUser: OAuthUser, code: String): UserModel {
        val user = userRepository.selectByProvider(providerType, OAuthUser.providerId)

        return if (user != null) {
            userRepository.loginUpdate(user.id, code)
            user
        } else {
            userRepository.insert(OAuthUser)
        }
    }

    private suspend fun HttpClient.acquireUser(providerType: AuthProvider, accessToken: String, code: String): OAuthUser {
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

    fun selectById(id: Int): UserModel {
        return userRepository.select(id)?: throw UserNotFoundException()
    }
}