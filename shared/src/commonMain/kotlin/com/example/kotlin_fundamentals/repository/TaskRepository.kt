package com.example.kotlin_fundamentals.repository

import com.example.kotlin_fundamentals.Task
import kotlinx.coroutines.flow.StateFlow

interface TaskRepository {
    val currentDatabase: StateFlow<DatabaseType>
    suspend fun switchDatabase()
    suspend fun addTask(title: String, description: String)
    suspend fun deleteTask(task: Task)
    suspend fun toggleTask(task: Task)
    suspend fun updateTask(id: Int, title: String, description: String)
    suspend fun getAllTasks(): List<Task>
}

