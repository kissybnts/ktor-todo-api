package kissybnts.ktor_todo.app.response

import kissybnts.ktor_todo.app.model.UserModel

data class LoginResponse(val user: UserModel, val token: String, val refreshToken: String)