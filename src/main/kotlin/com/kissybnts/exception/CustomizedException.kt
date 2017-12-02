package com.kissybnts.exception

import com.kissybnts.app.DefaultMessages

class ResourceNotFoundException(message: String): Exception(message)

class ProviderAuthenticationErrorException(message: String = DefaultMessages.Error.AUTH_PROCESS_FAILED): Exception(message)

class InvalidCredentialException(message: String = DefaultMessages.Error.INVALID_CREDENTIAL): Exception(message)