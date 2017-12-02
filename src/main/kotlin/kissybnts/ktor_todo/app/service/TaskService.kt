package kissybnts.ktor_todo.app.service

import kissybnts.ktor_todo.app.DefaultMessages
import kissybnts.ktor_todo.app.model.TaskModel
import kissybnts.ktor_todo.app.repository.ProjectRepository
import kissybnts.ktor_todo.app.repository.TaskRepository
import kissybnts.ktor_todo.app.request.CreateTaskRequest
import kissybnts.ktor_todo.app.request.UpdateTaskRequest
import kissybnts.ktor_todo.exception.ResourceNotFoundException

class TaskService(private val taskRepository: TaskRepository = TaskRepository,
                  private val projectRepository: ProjectRepository = ProjectRepository) {
    companion object {
        fun resourceNotFoundException(projectId: Int) = ResourceNotFoundException(DefaultMessages.Error.resourceNotFound("Task", projectId))
    }

    fun selectAll(userId: Int): List<TaskModel> {
        return taskRepository.selectAll(userId)
    }

    fun create(request: CreateTaskRequest, userId: Int): TaskModel {
        checkIsUsersProject(request.projectId, userId)

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

    /**
     * Check whether the specified project is user's one or not.
     *
     * @throws ResourceNotFoundException in case of the project is not user's one
     */
    private fun checkIsUsersProject(projectId: Int, userId: Int) {
        projectRepository.select(projectId, userId) ?: ProjectService.resourceNotFoundException(projectId)
    }


    private fun resourceNotFoundException(taskId: Int) = Companion.resourceNotFoundException(taskId)
}