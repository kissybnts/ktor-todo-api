package kissybnts.ktor_todo.exception

import kissybnts.ktor_todo.app.DefaultMessages

class ResourceNotFoundException(message: String): Exception(message)

class ProviderAuthenticationErrorException(message: String = DefaultMessages.Error.AUTH_PROCESS_FAILED): Exception(message)

class InvalidCredentialException(message: String = DefaultMessages.Error.INVALID_CREDENTIAL): Exception(message)