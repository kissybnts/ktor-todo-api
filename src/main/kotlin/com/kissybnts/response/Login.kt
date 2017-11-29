package com.kissybnts.response

data class LoginResponse(val user: UserResponse, val token: String)