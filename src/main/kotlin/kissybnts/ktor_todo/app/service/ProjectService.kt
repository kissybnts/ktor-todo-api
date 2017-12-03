package kissybnts.ktor_todo.app.service

import kissybnts.ktor_todo.app.DefaultMessages
import kissybnts.ktor_todo.app.model.ProjectModel
import kissybnts.ktor_todo.app.model.TaskModel
import kissybnts.ktor_todo.app.repository.ProjectRepository
import kissybnts.ktor_todo.app.repository.TaskRepository
import kissybnts.ktor_todo.app.request.CreateProjectRequest
import kissybnts.ktor_todo.app.response.ProjectResponse
import kissybnts.ktor_todo.exception.ResourceNotFoundException

class ProjectService(private val projectRepository: ProjectRepository = ProjectRepository,
                     private val taskRepository: TaskRepository = TaskRepository,
                     private val taskService: TaskService = TaskService()) {
    companion object {
        fun resourceNotFoundException(projectId: Int) = ResourceNotFoundException(DefaultMessages.Error.resourceNotFound("Project", projectId))
    }

    fun selectAll(userId: Int): List<ProjectResponse> {
        val projects = projectRepository.selectAll(userId)
        val tasks = taskService.selectAll(userId)

        return projects.map { p ->
            ProjectResponse(p, tasks.filter { it.projectId == p.id })
        }
    }

    fun select(projectId: Int, userId: Int): ProjectResponse {
        val project = projectRepository.select(projectId, userId)?: throw resourceNotFoundException(projectId)
        val tasks = taskService.selectAll(userId, projectId)
        return ProjectResponse(project, tasks)
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