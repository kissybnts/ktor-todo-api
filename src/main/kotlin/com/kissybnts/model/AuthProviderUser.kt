package com.kissybnts.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.kissybnts.route.User

@JsonIgnoreProperties(ignoreUnknown = true)
data class GitHubUser(val id: Int, val login: String, val name: String, val avatarUrl: String) {
    // TODO need to fetch from database or insert User record
    // TODO remove String literal "github"
    fun toUser(code: String): User = User(1, this.name, this.avatarUrl, "github", code, this.id)
}