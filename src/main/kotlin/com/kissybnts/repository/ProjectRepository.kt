package com.kissybnts.repository

import com.kissybnts.route.NewProject
import com.kissybnts.table.ProjectTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

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
    fun selectAll(userId: Int): List<ProjectJSON> = transaction { selectAllWithoutTransaction(userId) }

    fun selectAllWithoutTransaction(userId: Int): List<ProjectJSON> {
        return ProjectTable.select { ProjectTable.userId.eq(userId) }.map { ProjectJSON(it) }
    }

    fun select(id: Int): ProjectJSON? = transaction { selectWithoutTransaction(id) }

    fun selectWithoutTransaction(id: Int): ProjectJSON? = ProjectTable.select { ProjectTable.id.eq(id) }.firstOrNull()?.let { ProjectJSON(it) }

    fun insert(project: NewProject): ProjectJSON {
        val now = DateTime()
        val statement = transaction {
            ProjectTable.insert {
                // TODO change to use the user id of which logged in user
                it[ProjectTable.userId] = 1
                it[ProjectTable.name] = project.name
                it[ProjectTable.description] = project.description
                it[ProjectTable.createdAt] = now
                it[ProjectTable.updatedAt] = now
            }
        }
        val id = statement.generatedKey?.toInt() ?: throw IllegalStateException("Generated id is null")
        return ProjectJSON(id, project.name, project.description)
    }
}