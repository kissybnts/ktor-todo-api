package com.kissybnts.repository

import com.kissybnts.request.CreateProjectRequest
import com.kissybnts.table.ProjectTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

data class ProjectJSON(
        val id: Int,
        val name: String,
        val description: String) {
    constructor(result: ResultRow) : this(
            result[ProjectTable.id],
            result[ProjectTable.name],
            result[ProjectTable.description])
}

object ProjectRepository {
    fun selectAll(userId: Int): List<ProjectJSON> = transaction { ProjectTable.select { ProjectTable.userId.eq(userId) }.map { ProjectJSON(it) } }

    fun select(id: Int): ProjectJSON? = transaction { ProjectTable.select { ProjectTable.id.eq(id) }.firstOrNull()?.let { ProjectJSON(it) } }

    fun insert(project: CreateProjectRequest): ProjectJSON {
        val statement = transaction {
            ProjectTable.insert {
                // TODO change to use the user id of which logged in user
                it[ProjectTable.userId] = 1
                it[ProjectTable.name] = project.name
                it[ProjectTable.description] = project.description
            }
        }
        val id = statement.generatedKey?.toInt() ?: throw IllegalStateException("Generated id is null")
        return ProjectJSON(id, project.name, project.description)
    }
}