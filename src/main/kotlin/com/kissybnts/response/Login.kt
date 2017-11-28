package com.kissybnts.response

import com.kissybnts.route.User

data class LoginResponse(val user: User, val token: String)