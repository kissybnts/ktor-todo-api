package com.kissybnts.request

import com.fasterxml.jackson.annotation.JsonFormat
import org.joda.time.DateTime

data class CreateProjectRequest(val name: String, val description: String)
data class CreateTaskRequest(val name: String,
                             val description: String,
                             @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm:ss")
                             val dueDate: DateTime)