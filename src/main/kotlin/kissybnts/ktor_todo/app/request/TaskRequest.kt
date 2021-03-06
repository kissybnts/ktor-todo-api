package kissybnts.ktor_todo.app.request

import java.time.LocalDate

data class CreateTaskRequest(val projectId: Int,
                             val name: String,
                             val description: String,
                             val dueDate: LocalDate)

data class UpdateTaskRequest(val name: String,
                             val description: String,
                             val dueDate: LocalDate)