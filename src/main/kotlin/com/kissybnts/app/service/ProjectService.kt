package com.kissybnts.app.service

import com.kissybnts.app.DefaultMessages
import com.kissybnts.app.model.ProjectModel
import com.kissybnts.app.model.TaskModel
import com.kissybnts.app.repository.ProjectRepository
import com.kissybnts.app.repository.TaskRepository
import com.kissybnts.app.request.CreateProjectRequest
import com.kissybnts.exception.ResourceNotFoundException

class ProjectService {
    companion object {
        fun resourceNotFoundException(projectId: Int) = ResourceNotFoundException(DefaultMessages.Error.resourceNotFound("Project", projectId))
    }

    private val projectRepository: ProjectRepository = ProjectRepository
    private val taskRepository: TaskRepository = TaskRepository

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

    private fun resourceNotFoundException(projectId: Int) = ProjectService.resourceNotFoundException(projectId)
}