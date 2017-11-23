package com.kissybnts.request

import com.fasterxml.jackson.annotation.JsonFormat
import org.joda.time.DateTime

data class CreateTaskRequest(val projectId: Int,
                             val name: String,
                             val description: String,
                             @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
                             val dueDate: DateTime)

data class UpdateTaskRequest(val name: String,
                             val description: String,
                             @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
                             val dueDate: DateTime)