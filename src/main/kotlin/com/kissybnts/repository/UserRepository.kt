package com.kissybnts.repository

import com.kissybnts.table.AuthProvider
import com.kissybnts.table.UserTable
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(UserTable)
    var name by UserTable.name
    var imageUrl by UserTable.imageUrl
    var providerType by UserTable.providerType
    var providerCode by UserTable.providerCode
    var providerId by UserTable.providerId
    var createdAt by UserTable.createdAt
    var updatedAt by UserTable.updatedAt
}

data class CushioningUser(val name: String, val imageUrl: String, val providerType: AuthProvider, val providerCode: String, val providerId: Int)

object UserRepository {
    // ---------------
    // Select
    // ---------------
    fun select(id: Int): User? = transaction { User.findById(id) }

    fun selectByProvider(providerType: AuthProvider, providerId: Int): User? {
        return transaction {
            User.find { UserTable.providerType.eq(providerType) and UserTable.providerId.eq(providerId) }.firstOrNull()
        }
    }

    // ---------------
    // Insert
    // ---------------
    fun insert(cushioningUser: CushioningUser): User = transaction { insertWithoutTransaction(cushioningUser) }

    private fun insertWithoutTransaction(cushioningUser: CushioningUser): User {
        return User.new {
            name = cushioningUser.name
            imageUrl = cushioningUser.imageUrl
            providerType = cushioningUser.providerType
            providerCode = cushioningUser.providerCode
            providerId = cushioningUser.providerId
        }
    }

    // ---------------
    // Update
    // ---------------
    fun loginUpdate(user: User, code: String): User {
        return transaction {
            user.apply {
                providerCode = code
                updatedAt = DateTime()
            }
        }
    }
}