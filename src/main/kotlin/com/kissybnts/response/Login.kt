package com.kissybnts.response

import com.kissybnts.model.UserModel

data class LoginResponse(val user: UserModel, val token: String)