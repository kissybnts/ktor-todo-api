package kissybnts.ktor_todo.app.response

import kissybnts.ktor_todo.app.model.ProjectModel
import kissybnts.ktor_todo.app.model.TaskModel

data class ProjectResponse(val id: Int, val name: String, val description: String, val tasks: List<TaskModel>) {
    constructor(projectModel: ProjectModel, tasks: List<TaskModel>): this(projectModel.id, projectModel.name, projectModel.description, tasks)
}

data class ProjectListResponse(val projects: List<ProjectResponse>)