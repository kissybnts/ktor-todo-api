package com.kissybnts.repository

import com.kissybnts.table.ProjectTable
import com.kissybnts.table.TaskTable
import com.mysql.cj.api.xdevapi.UpdateStatement
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime

data class Task(val id: Int, val projectId: Int, val name: String, val description: String, val dueDate: String, val isCompleted: Boolean = false) {
    constructor(resultRow: ResultRow): this(
            resultRow[TaskTable.id],
            resultRow[TaskTable.projectId],
            resultRow[TaskTable.name],
            resultRow[TaskTable.description],
            resultRow[TaskTable.dueDate].toString("yyyy/MM/dd"),
            resultRow[TaskTable.isCompleted]
    )
}

data class NewTask(val projectId: Int, val name: String, val description: String, val dueDate: DateTime)

object TaskRepository {
    fun selectAll(userId: Int): List<Task> {
        // transaction{}.map{}をするとNo transactionでエラーを吐く
        return transaction {
            (TaskTable innerJoin ProjectTable).slice(TaskTable.columns).select { ProjectTable.userId.eq(userId) }.map { Task(it) }
        }
    }

    fun selectAllBelongProject(projectId: Int): List<Task> {
        return transaction {
            TaskTable.select { TaskTable.projectId.eq(projectId) }.map { Task(it) }
        }
    }

    fun select(id: Int): Task? = transaction { TaskTable.select { TaskTable.id.eq(id) }.firstOrNull()?.let { Task(it) } }

    fun insert(newTask: NewTask): Task = transaction { insertWithoutTransaction(newTask) }

    private fun insertWithoutTransaction(newTask: NewTask): Task {
        val statement = TaskTable.insert {
            it[TaskTable.projectId] = newTask.projectId
            it[TaskTable.name] = newTask.name
            it[TaskTable.description] = newTask.description
            it[TaskTable.dueDate] = newTask.dueDate
        }
        val id = statement.generatedKey?.toInt() ?: throw IllegalStateException("Generated id is null")
        return Task(id, newTask.projectId, newTask.name, newTask.description, newTask.dueDate.toString("yyyy/MM/dd"))
    }

    fun complete(id: Int): Int = transaction { completeWithoutTransaction(id) }

    private fun completeWithoutTransaction(id: Int): Int {
        return TaskTable.update({ TaskTable.id.eq(id) }) {
            it[isCompleted] = true
            it[updatedAt] = DateTime()
        }
    }

    fun complete(ids: List<Int>) = transaction { completeWithoutTransaction(ids) }

    private fun completeWithoutTransaction(ids: List<Int>): Int {
        val now = DateTime()
        return TaskTable.update({ TaskTable.id.inList(ids) }) {
            it[TaskTable.isCompleted] = true
            it[TaskTable.updatedAt] = now
        }
    }
}