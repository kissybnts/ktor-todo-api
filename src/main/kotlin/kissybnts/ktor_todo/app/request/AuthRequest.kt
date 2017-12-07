package kissybnts.ktor_todo.app.request

data class LoginRequest(val email: String, val password: String)

data class SignUpRequest(val name: String, val email: String, val password: String)