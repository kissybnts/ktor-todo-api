package com.kissybnts.table

import org.jetbrains.exposed.dao.IntIdTable
import org.joda.time.DateTime

object ColumnNames {
    const val id = "id"
    const val name = "name"
    const val description = "description"
    const val createdAt = "created_at"
    const val updatedAt = "updated_at"
}

enum class AuthProvider {
    GitHub
}

object UserTable : IntIdTable("users") {
    val name = varchar(ColumnNames.name, 255)
    val imageUrl = varchar("image_url", 255)
    val providerType = enumerationByName("provider_type", 10, AuthProvider::class.java)
    val providerCode = varchar("provider_code", 255)
    val providerId = integer("provider_id")
    val createdAt = datetime(ColumnNames.createdAt).default(DateTime())
    val updatedAt = datetime(ColumnNames.updatedAt).default(DateTime())
    const val foreignKey = "user_id"
}

object ProjectTable : IntIdTable("projects") {
    val userId = reference(UserTable.foreignKey, UserTable)
    val name = varchar(ColumnNames.name, 255)
    val description = text(ColumnNames.description)
    val createdAt = datetime(ColumnNames.createdAt).default(DateTime())
    val updatedAt = datetime(ColumnNames.updatedAt).default(DateTime())
    const val foreignKey = "project_id"
}

object TaskTable : IntIdTable("tasks") {
    val projectId = reference(ProjectTable.foreignKey, ProjectTable)
    val name = varchar(ColumnNames.name, 255)
    val description = text(ColumnNames.description)
    //    val isRepeat = bool("is_repeat").default(false)
    val dueDate = date("due_date").default(DateTime())//.nullable()
    //    val hasSubTasks = bool("has_sub_tasks").default(false)
    val isCompleted = bool("is_completed").default(false)
    val createdAt = datetime(ColumnNames.createdAt).default(DateTime())
    val updatedAt = datetime(ColumnNames.updatedAt).default(DateTime())
    const val foreignKey = "task_id"
}

//object TaskScheduleTable: Table("task_schedules") {
//    val taskId = integer(TaskTable.foreignKey).references(TaskTable.id).uniqueIndex().primaryKey()
//    val isDaily = bool("is_daily").default(false)
//    val createdAt = datetime(ColumnNames.createdAt).default(DateTime())
//    val updatedAt = datetime(ColumnNames.updatedAt).default(DateTime())
//}
//
//object SubTaskRelationTable: Table("sub_tasks") {
//    val parentTaskId = integer("parent_task_id").references(TaskTable.id)
//    val subTaskId = integer("sub_task_id").references((TaskTable.id))
//}