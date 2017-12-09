package kissybnts.ktor_todo.app.service

import kissybnts.ktor_todo.app.DefaultMessages
import kissybnts.ktor_todo.app.model.TaskModel
import kissybnts.ktor_todo.app.repository.TaskRepository
import kissybnts.ktor_todo.app.repository.TaskRepositoryInterface
import kissybnts.ktor_todo.app.request.CreateTaskRequest
import kissybnts.ktor_todo.app.request.UpdateTaskRequest
import kissybnts.ktor_todo.exception.ResourceNotFoundException

class TaskService(private val taskRepository: TaskRepositoryInterface = TaskRepository) {
    companion object {
        fun resourceNotFoundException(projectId: Int) = ResourceNotFoundException(DefaultMessages.Error.resourceNotFound("Task", projectId))
    }

    fun selectAll(userId: Int): List<TaskModel> {
        return taskRepository.selectAll(userId)
    }

    fun selectAll(userId: Int, projectId: Int): List<TaskModel> {
        return taskRepository.selectAll(userId, projectId)
    }

    fun create(request: CreateTaskRequest, userId: Int): TaskModel {
        return taskRepository.insert(request)
    }

    fun update(taskId: Int, request: UpdateTaskRequest, userId: Int) {
        val updated = taskRepository.update(taskId, request, userId)
        if (updated != 1) {
            throw resourceNotFoundException(taskId)
        }
    }

    fun complete(taskId: Int, userId: Int) {
        val completed = taskRepository.complete(taskId, userId)
        if (completed != 1) {
            throw resourceNotFoundException(taskId)
        }
    }

    private fun resourceNotFoundException(taskId: Int) = Companion.resourceNotFoundException(taskId)
}