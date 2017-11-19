package com.kissybnts.route

import io.ktor.locations.location

@location("/")
class Index

@location("/{id}") data class ResourceId(val id: Int) {
    companion object {
        const val parameterName: String = "id"
    }
}

@location("/{secondary_id}") data class SecondaryResourceId(val secondaryId: Int) {
    companion object {
        const val parameterName: String = "secondary_id"
    }
}