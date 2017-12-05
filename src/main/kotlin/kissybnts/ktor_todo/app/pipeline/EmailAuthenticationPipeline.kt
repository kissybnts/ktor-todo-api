package kissybnts.ktor_todo.app.pipeline

import io.ktor.auth.AuthenticationPipeline

fun AuthenticationPipeline.emailAuthentication() {
    intercept(AuthenticationPipeline.RequestAuthentication) {
        TODO()
    }
}