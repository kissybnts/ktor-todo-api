package com.kissybnts.table

import kotlinx.coroutines.experimental.selects.select
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object ColumnNames {
    val id = "id"
    val name = "name"
    val description = "description"
    val createdAt = "created_at"
    val updatedAt = "updated_at"
}

//enum class AuthProvider {
//    GitHub
//}
//
//object UserTable: Table("users") {
//    val id = integer(ColumnNames.id).primaryKey().autoIncrement()
//    val name = varchar(ColumnNames.name, 255)
//    val authProvider = enumeration("auth_provider", AuthProvider::class.java)
//    val token = text("token")
//}

object ProjectTable: Table("projects") {
    val id = integer("id").primaryKey().autoIncrement()
//    val userId = integer("user_id").references(UserTable.id)
    val name = varchar(ColumnNames.name, 255)
    val description = text(ColumnNames.description)
    val foreignKey = "project_id"
}

object TaskTable: Table("tasks") {
    val id = integer(ColumnNames.id).primaryKey().autoIncrement()
    val projectId = integer(ProjectTable.foreignKey).references(ProjectTable.id)
    val name = varchar(ColumnNames.name, 255)
    val description = text(ColumnNames.description)
//    val isRepeat = bool("is_repeat").default(false)
    val dueDate = date("due_date").default(DateTime())//.nullable()
//    val hasSubTasks = bool("has_sub_tasks").default(false)
    val isCompleted = bool("is_completed").default(false)
    val createdAt = datetime(ColumnNames.createdAt).default(DateTime())
    val updatedAt = datetime(ColumnNames.updatedAt).default(DateTime())
    val foreignKey = "task_id"
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

fun sample() {
    transaction {
        TaskTable.select { TaskTable.projectId.eq(1) }.forEach { println(it[TaskTable.name]) }
    }
}