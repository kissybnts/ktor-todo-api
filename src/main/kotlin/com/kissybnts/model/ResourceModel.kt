package com.kissybnts.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.kissybnts.extension.toJavaLocalDate
import com.kissybnts.extension.toJavaLocalDateTime
import com.kissybnts.table.AuthProvider
import com.kissybnts.table.ProjectTable
import com.kissybnts.table.TaskTable
import com.kissybnts.table.UserTable
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
            resultRow[UserTable.id].value,
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
            resultRow[ProjectTable.id].value,
            resultRow[ProjectTable.userId].value,
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
            resultRow[TaskTable.id].value,
            resultRow[TaskTable.projectId].value,
            resultRow[TaskTable.name],
            resultRow[TaskTable.description],
            resultRow[TaskTable.dueDate].toJavaLocalDate(),
            resultRow[TaskTable.isCompleted],
            resultRow[TaskTable.createdAt].toJavaLocalDateTime(),
            resultRow[TaskTable.updatedAt].toJavaLocalDateTime()
    )
}