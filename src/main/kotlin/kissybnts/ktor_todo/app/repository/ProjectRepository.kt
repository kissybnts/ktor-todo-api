package kissybnts.ktor_todo.app.repository

import kissybnts.ktor_todo.extension.toJavaLocalDateTime
import kissybnts.ktor_todo.app.model.ProjectModel
import kissybnts.ktor_todo.app.request.CreateProjectRequest
import kissybnts.ktor_todo.app.table.ProjectTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object ProjectRepository {
    fun selectAll(userId: Int): List<ProjectModel> = transaction { ProjectTable.select { ProjectTable.userId.eq(userId) }.map { ProjectModel(it) } }

    fun select(id: Int, userId: Int): ProjectModel? = transaction { ProjectTable.select { ProjectTable.id.eq(id) and ProjectTable.userId.eq(userId) }.firstOrNull()?.let { ProjectModel(it) } }

    fun insert(project: CreateProjectRequest, userId: Int): ProjectModel {
        val now = DateTime()
        val statement = transaction {
            ProjectTable.insert {
                it[ProjectTable.userId] = userId
                it[ProjectTable.name] = project.name
                it[ProjectTable.description] = project.description
                it[ProjectTable.createdAt] = now
                it[ProjectTable.updatedAt] = now
            }
        }
        val id = statement.generatedKey?.toInt() ?: throw IllegalStateException("Generated id is null")
        return ProjectModel(id, userId, project.name, project.description, now.toJavaLocalDateTime(), now.toJavaLocalDateTime())
    }
}