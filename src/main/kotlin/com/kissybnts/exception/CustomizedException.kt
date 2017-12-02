package com.kissybnts.exception

class ResourceNotFoundException(message: String): Exception(message)

class ProviderAuthenticationErrorException(message: String): Exception(message)

class InvalidCredentialException(message: String = "Invalid credential."): Exception(message)