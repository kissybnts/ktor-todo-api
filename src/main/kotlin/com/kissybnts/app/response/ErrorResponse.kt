package com.kissybnts.app.response

data class ErrorResponse(val message: String) {
    constructor(exception: Exception, defaultMessage: String): this(exception.message?: defaultMessage)
}