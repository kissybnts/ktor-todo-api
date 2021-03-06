package kissybnts.ktor_todo.app.repository

import kissybnts.ktor_todo.extension.toJavaLocalDateTime
import kissybnts.ktor_todo.extension.toJodaDate
import kissybnts.ktor_todo.app.model.TaskModel
import kissybnts.ktor_todo.app.request.CreateTaskRequest
import kissybnts.ktor_todo.app.request.UpdateTaskRequest
import kissybnts.ktor_todo.app.table.ProjectTable
import kissybnts.ktor_todo.app.table.TaskTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

interface TaskRepositoryInterface {
    // Select
    fun selectAll(userId: Int): List<TaskModel>
    fun selectAll(userId: Int, projectId: Int): List<TaskModel>
    fun select(id: Int): TaskModel?
    // Insert
    fun insert(request: CreateTaskRequest): TaskModel
    // Update
    fun update(id: Int, request: UpdateTaskRequest, userId: Int): Int
    fun complete(id: Int, userId: Int): Int
    fun complete(ids: List<Int>, userId: Int): Int
}

object TaskRepository : TaskRepositoryInterface {
    // ---------------
    // Select
    // ---------------
    override fun selectAll(userId: Int): List<TaskModel> {
        // transaction{}.map{}をするとNo transactionでエラーを吐く
        return transaction {
            (TaskTable innerJoin ProjectTable).slice(TaskTable.columns).select { ProjectTable.userId.eq(userId) }.orderBy(TaskTable.id, true).map { TaskModel(it) }
        }
    }

    override fun selectAll(userId: Int, projectId: Int): List<TaskModel> {
        return transaction { joinOnlyProjectId(userId).select { TaskTable.projectId.eq(projectId) }.orderBy(TaskTable.id, true).map { TaskModel(it) } }
    }

    override fun select(id: Int): TaskModel? = transaction { TaskTable.select { TaskTable.id.eq(id) }.firstOrNull()?.let { TaskModel(it) } }

    // ---------------
    // Insert
    // ---------------
    override fun insert(request: CreateTaskRequest): TaskModel = transaction { insertWithoutTransaction(request) }

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
    override fun update(id: Int, request: UpdateTaskRequest, userId: Int): Int = transaction { updateWithoutTransaction(id, request, userId) }

    private fun updateWithoutTransaction(id: Int, request: UpdateTaskRequest, userId: Int): Int {
        return joinOnlyProjectId(userId).update({ TaskTable.id.eq(id) }) {
            it[TaskTable.name] = request.name
            it[TaskTable.description] = request.description
            it[TaskTable.dueDate] = request.dueDate.toJodaDate()
            it[TaskTable.updatedAt] = DateTime()
        }
    }

    // ---------------
    // Complete
    // ---------------
    override fun complete(id: Int, userId: Int): Int = transaction { completeWithoutTransaction(id, userId) }

    private fun completeWithoutTransaction(id: Int, userId: Int): Int {
        return joinOnlyProjectId(userId).update({ TaskTable.id.eq(id) }) {
            it[TaskTable.isCompleted] = true
            it[TaskTable.updatedAt] = DateTime()
        }
    }

    override fun complete(ids: List<Int>, userId: Int) = transaction { completeWithoutTransaction(ids, userId) }

    private fun completeWithoutTransaction(ids: List<Int>, userId: Int): Int {
        val now = DateTime()
        return joinOnlyProjectId(userId).update({ TaskTable.id.inList(ids) and ProjectTable.userId.eq(userId) }) {
            it[TaskTable.isCompleted] = true
            it[TaskTable.updatedAt] = now
        }
    }

    /**
     * make `tasks INNER JOIN (SELECT projects.id FROM projects WHERE projects.user_id = ${user_id}) q0 ON (q0.id = tasks.project_id)`.
     *
     * TODO investigate a solution for this.
     */
    private fun joinOnlyProjectId(userId: Int): Join {
        return TaskTable.joinQuery(on = { it[ProjectTable.id].eq(TaskTable.projectId) }, joinPart = { ProjectTable.slice(ProjectTable.id).select { ProjectTable.userId.eq(userId) } })
    }
}