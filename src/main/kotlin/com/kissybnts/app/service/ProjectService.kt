package com.kissybnts.app.service

import com.kissybnts.app.model.ProjectModel
import com.kissybnts.app.model.TaskModel
import com.kissybnts.app.repository.ProjectRepository
import com.kissybnts.app.repository.TaskRepository
import com.kissybnts.app.request.CreateProjectRequest
import com.kissybnts.exception.ResourceNotFoundException

class ProjectService {
    private val projectRepository: ProjectRepository = ProjectRepository
    private val taskRepository: TaskRepository = TaskRepository

    fun selectAll(userId: Int): List<ProjectModel> {
        return projectRepository.selectAll(userId)
    }

    fun select(id: Int, userId: Int): ProjectModel {
        return projectRepository.select(id, userId) ?: throw ResourceNotFoundException("Project of which id is $id has not been found.")
    }

    fun selectTasks(projectId: Int, userId: Int): List<TaskModel> {
        // TODO consider the case when the project is not user's one. whether throw the resource not found exception or not.
        return taskRepository.selectAllBelongProject(projectId, userId)
    }

    fun create(request: CreateProjectRequest, userId: Int): ProjectModel {
        return projectRepository.insert(request, userId)
    }
}