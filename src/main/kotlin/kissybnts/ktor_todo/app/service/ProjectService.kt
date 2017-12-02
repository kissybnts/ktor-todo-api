package kissybnts.ktor_todo.app.service

import kissybnts.ktor_todo.app.DefaultMessages
import kissybnts.ktor_todo.app.model.ProjectModel
import kissybnts.ktor_todo.app.model.TaskModel
import kissybnts.ktor_todo.app.repository.ProjectRepository
import kissybnts.ktor_todo.app.repository.TaskRepository
import kissybnts.ktor_todo.app.request.CreateProjectRequest
import kissybnts.ktor_todo.exception.ResourceNotFoundException

class ProjectService(private val projectRepository: ProjectRepository = ProjectRepository,
                     private val taskRepository: TaskRepository = TaskRepository) {
    companion object {
        fun resourceNotFoundException(projectId: Int) = ResourceNotFoundException(DefaultMessages.Error.resourceNotFound("Project", projectId))
    }

    fun selectAll(userId: Int): List<ProjectModel> {
        return projectRepository.selectAll(userId)
    }

    fun select(projectId: Int, userId: Int): ProjectModel {
        return projectRepository.select(projectId, userId) ?: throw resourceNotFoundException(projectId)
    }

    fun selectTasks(projectId: Int, userId: Int): List<TaskModel> {
        // TODO consider the case when the project is not user's one. whether throw the resource not found exception or not.
        return taskRepository.selectAllBelongProject(projectId, userId)
    }

    fun create(request: CreateProjectRequest, userId: Int): ProjectModel {
        return projectRepository.insert(request, userId)
    }

    private fun resourceNotFoundException(projectId: Int) = Companion.resourceNotFoundException(projectId)
}