package kissybnts.ktor_todo.app.model

import com.fasterxml.jackson.annotation.JsonIgnore
import kissybnts.ktor_todo.extension.toJavaLocalDate
import kissybnts.ktor_todo.extension.toJavaLocalDateTime
import kissybnts.ktor_todo.app.enumeration.AuthProvider
import kissybnts.ktor_todo.app.table.ProjectTable
import kissybnts.ktor_todo.app.table.TaskTable
import kissybnts.ktor_todo.app.table.UserTable
import org.jetbrains.exposed.sql.ResultRow
import java.time.LocalDate
import java.time.LocalDateTime

data class UserModel(val id: Int,
                     val name: String,
                     val imageUrl: String,
                     val providerType: AuthProvider,
                     val providerCode: String,
                     val providerId: Int,
                     @JsonIgnore
                     val createdAt: LocalDateTime,
                     @JsonIgnore
                     val updatedAt: LocalDateTime) {
    constructor(resultRow: ResultRow): this(
            resultRow[UserTable.id],
            resultRow[UserTable.name],
            resultRow[UserTable.imageUrl],
            resultRow[UserTable.providerType],
            resultRow[UserTable.providerCode],
            resultRow[UserTable.providerId],
            resultRow[UserTable.createdAt].toJavaLocalDateTime(),
            resultRow[UserTable.updatedAt].toJavaLocalDateTime()
    )
}

data class ProjectModel(val id: Int,
                        val userId: Int,
                        val name: String,
                        val description: String,
                        @JsonIgnore
                        val createdAt: LocalDateTime,
                        @JsonIgnore
                        val updatedAt: LocalDateTime) {
    constructor(resultRow: ResultRow): this(
            resultRow[ProjectTable.id],
            resultRow[ProjectTable.userId],
            resultRow[ProjectTable.name],
            resultRow[ProjectTable.description],
            resultRow[ProjectTable.createdAt].toJavaLocalDateTime(),
            resultRow[ProjectTable.updatedAt].toJavaLocalDateTime()
    )
}

data class TaskModel(val id: Int,
                     val projectId: Int,
                     val name: String,
                     val description: String,
                     val dueDate: LocalDate,
                     val isCompleted: Boolean,
                     @JsonIgnore
                     val createdAt: LocalDateTime,
                     @JsonIgnore
                     val updatedAt: LocalDateTime) {
    constructor(resultRow: ResultRow): this(
            resultRow[TaskTable.id],
            resultRow[TaskTable.projectId],
            resultRow[TaskTable.name],
            resultRow[TaskTable.description],
            resultRow[TaskTable.dueDate].toJavaLocalDate(),
            resultRow[TaskTable.isCompleted],
            resultRow[TaskTable.createdAt].toJavaLocalDateTime(),
            resultRow[TaskTable.updatedAt].toJavaLocalDateTime()
    )
}