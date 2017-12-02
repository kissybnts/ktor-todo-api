package kissybnts.ktor_todo.app.repository

import kissybnts.ktor_todo.extension.toJavaLocalDateTime
import kissybnts.ktor_todo.app.model.UserModel
import kissybnts.ktor_todo.app.enumeration.AuthProvider
import kissybnts.ktor_todo.app.table.UserTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime

data class CushioningUser(val name: String, val imageUrl: String, val providerType: AuthProvider, val providerCode: String, val providerId: Int)

object UserRepository {
    // ---------------
    // Select
    // ---------------
    fun select(id: Int): UserModel? = transaction { UserTable.select { UserTable.id.eq(id) } }.firstOrNull()?.let { UserModel(it) }

    fun selectByProvider(providerType: AuthProvider, providerId: Int): UserModel? {
        return transaction {
            UserTable.select{ UserTable.providerType.eq(providerType) and UserTable.providerId.eq(providerId) }.firstOrNull()
        }?.let { UserModel(it) }
    }

    // ---------------
    // Insert
    // ---------------
    fun insert(cushioningUser: CushioningUser): UserModel = transaction { insertWithoutTransaction(cushioningUser) }

    private fun insertWithoutTransaction(cushioningUser: CushioningUser): UserModel {
        val now = DateTime()
        val statement =  UserTable.insert {
            it[UserTable.name] = cushioningUser.name
            it[UserTable.imageUrl] = cushioningUser.imageUrl
            it[UserTable.providerType] = cushioningUser.providerType
            it[UserTable.providerCode] = cushioningUser.providerCode
            it[UserTable.providerId] = cushioningUser.providerId
            it[UserTable.createdAt] = now
            it[UserTable.updatedAt] = now
        }
        val id = statement.generatedKey?.toInt() ?: throw IllegalStateException("Generated id is null.")
        return UserModel(id, cushioningUser.name, cushioningUser.imageUrl, cushioningUser.providerType, cushioningUser.providerCode, cushioningUser.providerId, now.toJavaLocalDateTime(), now.toJavaLocalDateTime())
    }

    // ---------------
    // Update
    // ---------------
    fun loginUpdate(user: UserModel, code: String): UserModel {
        val now = DateTime()
        transaction {
            UserTable.update({ UserTable.id.eq(user.id)}) {
                it[UserTable.providerCode] = code
                it[UserTable.updatedAt] = now
            }
        }
        return user.copy(providerCode = code, updatedAt = now.toJavaLocalDateTime())
    }
}