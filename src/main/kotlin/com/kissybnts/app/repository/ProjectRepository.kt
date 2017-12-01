package com.kissybnts.app.repository

import com.kissybnts.extension.toJavaLocalDateTime
import com.kissybnts.app.model.ProjectModel
import com.kissybnts.app.request.CreateProjectRequest
import com.kissybnts.app.table.ProjectTable
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