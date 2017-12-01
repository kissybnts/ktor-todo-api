package com.kissybnts.repository

import com.kissybnts.extension.toJavaLocalDateTime
import com.kissybnts.extension.toJodaDate
import com.kissybnts.model.TaskModel
import com.kissybnts.request.CreateTaskRequest
import com.kissybnts.request.UpdateTaskRequest
import com.kissybnts.table.ProjectTable
import com.kissybnts.table.TaskTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.DateTime

object TaskRepository {
    // ---------------
    // Select
    // ---------------
    fun selectAll(userId: Int): List<TaskModel> {
        // transaction{}.map{}をするとNo transactionでエラーを吐く
        return transaction {
            (TaskTable innerJoin ProjectTable).slice(TaskTable.columns).select { ProjectTable.userId.eq(userId) }.map { TaskModel(it) }
        }
    }

    fun selectAllBelongProject(projectId: Int): List<TaskModel> {
        return transaction {
            TaskTable.select { TaskTable.projectId.eq(projectId) }.map { TaskModel(it) }
        }
    }

    fun select(id: Int): TaskModel? = transaction { TaskTable.select { TaskTable.id.eq(id) }.firstOrNull()?.let { TaskModel(it) } }

    // ---------------
    // Insert
    // ---------------
    fun insert(request: CreateTaskRequest): TaskModel = transaction { insertWithoutTransaction(request) }

    private fun insertWithoutTransaction(request: CreateTaskRequest): TaskModel {
        val now = DateTime()
        val statement = TaskTable.insert {
            it[TaskTable.projectId] = request.projectId
            it[TaskTable.name] = request.name
            it[TaskTable.description] = request.description
            it[TaskTable.dueDate] = request.dueDate.toJodaDate()
            it[TaskTable.createdAt] = now
            it[TaskTable.updatedAt] = now
        }
        val id = statement.generatedKey?.toInt()?: throw IllegalStateException("Generated key is null.")
        return TaskModel(id, request.projectId, request.name, request.description, request.dueDate, false, now.toJavaLocalDateTime(), now.toJavaLocalDateTime())
    }

    // ---------------
    // Update
    // ---------------
    /**
     * Update the specified task according to the request using transaction.
     */
    fun update(id: Int, request: UpdateTaskRequest): Int = transaction { updateWithoutTransaction(id, request) }

    private fun updateWithoutTransaction(id: Int, request: UpdateTaskRequest): Int {
        return TaskTable.update({TaskTable.id.eq(id)}) {
            it[name] = request.name
            it[description] = request.description
            it[dueDate] = request.dueDate.toJodaDate()
            it[updatedAt] = DateTime()
        }
    }

    // ---------------
    // Complete
    // ---------------
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