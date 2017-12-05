package kissybnts.ktor_todo.app.enumeration

enum class AuthProvider {
    GitHub, Email;

    companion object {
        fun fromName(name: String): AuthProvider {
            return when (name) {
                "github" -> GitHub
                else -> throw IllegalArgumentException("Un supported type: $name")
            }
        }
    }
}