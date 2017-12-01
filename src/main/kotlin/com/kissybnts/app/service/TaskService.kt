package com.kissybnts.app.service

import com.kissybnts.app.model.TaskModel
import com.kissybnts.app.repository.ProjectRepository
import com.kissybnts.app.repository.TaskRepository
import com.kissybnts.app.request.CreateTaskRequest
import com.kissybnts.app.request.UpdateTaskRequest
import com.kissybnts.exception.ResourceNotFoundException

class TaskService {
    private val taskRepository: TaskRepository = TaskRepository
    private val projectRepository: ProjectRepository = ProjectRepository

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
            throw ResourceNotFoundException("Task of which id is $taskId has not been found.")
        }
    }

    fun complete(taskId: Int, userId: Int) {
        val completed = taskRepository.complete(taskId, userId)
        if (completed != 1) {
            throw ResourceNotFoundException("Task of which id is $taskId has not been found.")
        }
    }

    /**
     * Check whether the specified project is user's one or not.
     *
     * @throws ResourceNotFoundException in case of the project is not user's one
     */
    private fun checkIsUsersProject(projectId: Int, userId: Int) {
        projectRepository.select(projectId, userId) ?: throw ResourceNotFoundException("Project of which id is $projectId has not been found.")
    }
}