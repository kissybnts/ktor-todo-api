package kissybnts.ktor_todo.app.service

import kissybnts.ktor_todo.app.DefaultMessages
import kissybnts.ktor_todo.app.model.ProjectModel
import kissybnts.ktor_todo.app.model.TaskModel
import kissybnts.ktor_todo.app.repository.ProjectRepository
import kissybnts.ktor_todo.app.repository.ProjectRepositoryInterface
import kissybnts.ktor_todo.app.request.CreateProjectRequest
import kissybnts.ktor_todo.app.response.ProjectResponse
import kissybnts.ktor_todo.exception.ResourceNotFoundException
import kotlinx.coroutines.experimental.async

class ProjectService(private val projectRepository: ProjectRepositoryInterface = ProjectRepository,
                     private val taskService: TaskService = TaskService()) {
    companion object {
        fun resourceNotFoundException(projectId: Int) = ResourceNotFoundException(DefaultMessages.Error.resourceNotFound("Project", projectId))
    }

    suspend fun selectAll(userId: Int): List<ProjectResponse> {
        val projects = async { projectRepository.selectAll(userId) }
        val tasks = async { taskService.selectAll(userId) }

        return projects.await().map { p ->
            ProjectResponse(p, tasks.await().filter { it.projectId == p.id })
        }
    }

    suspend fun select(projectId: Int, userId: Int): ProjectResponse {
        val project = async { projectRepository.select(projectId, userId)?: throw resourceNotFoundException(projectId) }
        val tasks = async { taskService.selectAll(userId, projectId) }
        return ProjectResponse(project.await(), tasks.await())
    }

    fun selectTasks(projectId: Int, userId: Int): List<TaskModel> {
        // TODO consider the case when the project is not user's one. whether throw the resource not found exception or not.
        return taskService.selectAll(userId, projectId)
    }

    fun create(request: CreateProjectRequest, userId: Int): ProjectModel {
        return projectRepository.insert(request, userId)
    }

    private fun resourceNotFoundException(projectId: Int) = Companion.resourceNotFoundException(projectId)

    /**
     * Check whether the specified project is user's one or not.
     *
     * @throws ResourceNotFoundException in case of the project is not user's one
     */
    fun checkIsUsersProject(projectId: Int, userId: Int) {
        if (projectRepository.count(projectId, userId) == 0) {
            throw ProjectService.resourceNotFoundException(projectId)
        }
    }
}