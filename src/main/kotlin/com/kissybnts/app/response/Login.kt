package com.kissybnts.app.response

import com.kissybnts.app.model.UserModel

data class LoginResponse(val user: UserModel, val token: String)