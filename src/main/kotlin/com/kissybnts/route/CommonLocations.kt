package com.kissybnts.route

import io.ktor.locations.location

@location("/")
class Index

@location("/{id}") data class ResourceId(val id: Int)