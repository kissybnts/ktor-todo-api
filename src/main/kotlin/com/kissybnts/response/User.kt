package com.kissybnts.response

import com.kissybnts.repository.User

data class UserResponse(val id: Int, val name: String, val imageUrl: String, val providerType: String, val providerId: Int) {
    constructor(user: User): this(user.id.value, user.name, user.imageUrl, user.providerType.name, user.providerId)
}