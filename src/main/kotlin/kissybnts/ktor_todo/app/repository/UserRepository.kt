package kissybnts.ktor_todo.app.repository

import kissybnts.ktor_todo.extension.toJavaLocalDateTime
import kissybnts.ktor_todo.app.model.UserModel
import kissybnts.ktor_todo.app.enumeration.AuthProvider
import kissybnts.ktor_todo.app.enumeration.AuthType
import kissybnts.ktor_todo.app.model.EmailCredentialModel
import kissybnts.ktor_todo.app.model.OAuthUser
import kissybnts.ktor_todo.app.table.EmailCredentialTable
import kissybnts.ktor_todo.app.table.OAuthCredentialTable
import kissybnts.ktor_todo.app.table.UserTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime

object UserRepository {
    // ---------------
    // Select
    // ---------------
    fun select(id: Int): UserModel? = transaction { UserTable.select { UserTable.id.eq(id) }.firstOrNull() }?.let { UserModel(it) }

    fun selectByProvider(providerType: AuthProvider, providerId: Int): UserModel? {
        return transaction {
            UserTable.innerJoin(OAuthCredentialTable).select { OAuthCredentialTable.providerType.eq(providerType) and OAuthCredentialTable.providerId.eq(providerId) }.firstOrNull()
        }?.let { UserModel(it) }
    }

    fun selectByEmail(email: String): Pair<UserModel, EmailCredentialModel>? {
        return transaction {
            UserTable.innerJoin(EmailCredentialTable).select { EmailCredentialTable.email.eq(email) }.firstOrNull()
        }?.let { Pair(UserModel(it), EmailCredentialModel(it)) }
    }

    // ---------------
    // Insert
    // ---------------
    fun insert(OAuthUser: OAuthUser): UserModel = transaction { insertWithoutTransaction(OAuthUser) }

    fun insert(name: String, email: String, password: String): UserModel = transaction { insertWithoutTransaction(name, email, password) }

    private fun insertWithoutTransaction(OAuthUser: OAuthUser): UserModel {
        val now = DateTime()
        val statement =  UserTable.insert {
            it[UserTable.name] = OAuthUser.name
            it[UserTable.imageUrl] = OAuthUser.imageUrl
            it[UserTable.authType] = AuthType.OAuth
            it[UserTable.createdAt] = now
            it[UserTable.updatedAt] = now
        }
        val id = statement.generatedKey?.toInt() ?: throw IllegalStateException("Generated id is null.")
        OAuthCredentialTable.insert {
            it[OAuthCredentialTable.userId] = id
            it[OAuthCredentialTable.providerType] = OAuthUser.providerType
            it[OAuthCredentialTable.providerCode] = OAuthUser.providerCode
            it[OAuthCredentialTable.providerId] = OAuthUser.providerId
            it[OAuthCredentialTable.createdAt] = now
            it[OAuthCredentialTable.updatedAt] = now
        }
        return UserModel(id, OAuthUser.name, OAuthUser.imageUrl, AuthType.OAuth, now.toJavaLocalDateTime(), now.toJavaLocalDateTime())
    }

    private fun insertWithoutTransaction(name: String, email: String, password: String): UserModel {
        val now = DateTime()
        val statement = UserTable.insert {
            it[UserTable.name]  = name
            it[UserTable.imageUrl] = ""
            it[UserTable.authType] = AuthType.Email
            it[UserTable.createdAt] = now
            it[UserTable.updatedAt] = now
        }
        val id = statement.generatedKey?.toInt() ?: throw IllegalStateException("Generated id is null.")
        EmailCredentialTable.insert {
            it[EmailCredentialTable.userId] = id
            it[EmailCredentialTable.email] = email
            it[EmailCredentialTable.password] = password
            it[EmailCredentialTable.createdAt] = now
            it[EmailCredentialTable.updatedAt] = now
        }
        return UserModel(id, name, "", AuthType.Email, now.toJavaLocalDateTime(), now.toJavaLocalDateTime())
    }

    // ---------------
    // Update
    // ---------------
    fun loginUpdate(userId: Int, code: String) {
        val now = DateTime()
        transaction {
            OAuthCredentialTable.update({ OAuthCredentialTable.userId.eq(userId) }) {
                it[OAuthCredentialTable.providerCode] = code
                it[OAuthCredentialTable.updatedAt] = now
            }
        }
    }
}