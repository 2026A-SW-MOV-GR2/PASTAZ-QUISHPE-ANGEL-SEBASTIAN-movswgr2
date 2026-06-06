package com.example.kotlin_fundamentals.repository

import com.example.kotlin_fundamentals.Task
import com.example.kotlin_fundamentals.db.AppDatabase
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path

class DualTaskRepository(private val sqlDb: AppDatabase, private val internalPath: String? = null) : TaskRepository {

    // Inicialización segura de la ruta del archivo JSON en el espacio privado de la app
    private val noSqlStore: KStore<List<Task>> = storeOf(
        file = if (internalPath != null) Path("$internalPath/tasks_nosql.json") else Path("tasks_nosql.json")
    )

    private val _currentDatabase = MutableStateFlow(DatabaseType.SQL)
    override val currentDatabase: StateFlow<DatabaseType> = _currentDatabase

    private fun logAuditoria(level: String, message: String) {
        println("[$level] AUDIT: $message")
    }

    override suspend fun switchDatabase() {
        val newValue = if (_currentDatabase.value == DatabaseType.SQL) {
            DatabaseType.NOSQL
        } else {
            DatabaseType.SQL
        }
        _currentDatabase.value = newValue
        logAuditoria("INFO", "Conmutación de motor de datos a: $newValue")
    }

    override suspend fun addTask(title: String, description: String) = withContext(Dispatchers.IO) {
        try {
            when (_currentDatabase.value) {
                DatabaseType.SQL -> {
                    sqlDb.appDatabaseQueries.insertTask(title, description, 0L)
                    logAuditoria("DEBUG", "Inserción relacional (SQL) exitosa")
                }
                DatabaseType.NOSQL -> {
                    noSqlStore.update { currentTasks ->
                        val nextId = (currentTasks?.maxOfOrNull { it.id } ?: 0) + 1
                        // Validamos que use la propiedad correcta de tu clase de dominio (subtitle)
                        val nuevaTarea = Task(id = nextId, title = title, subtitle = description, isCompleted = false)
                        (currentTasks ?: emptyList()) + nuevaTarea
                    }
                    logAuditoria("DEBUG", "Inserción basada en documentos (NoSQL) exitosa")
                }
            }
        } catch (e: Exception) {
            logAuditoria("ERROR", "Fallo en operación addTask: ${e.message}")
        }
    }

    override suspend fun deleteTask(task: Task) = withContext(Dispatchers.IO) {
        try {
            when (_currentDatabase.value) {
                DatabaseType.SQL -> {
                    sqlDb.appDatabaseQueries.deleteTask(task.id.toLong())
                    logAuditoria("DEBUG", "Eliminación relacional (SQL) exitosa")
                }
                DatabaseType.NOSQL -> {
                    noSqlStore.update { currentTasks ->
                        currentTasks?.filterNot { it.id == task.id }
                    }
                    logAuditoria("DEBUG", "Eliminación basada en documentos (NoSQL) exitosa")
                }
            }
        } catch (e: Exception) {
            logAuditoria("ERROR", "Fallo en operación deleteTask: ${e.message}")
        }
    }

    override suspend fun toggleTask(task: Task) = withContext(Dispatchers.IO) {
        try {
            when (_currentDatabase.value) {
                DatabaseType.SQL -> {
                    sqlDb.appDatabaseQueries.updateTaskStatus(if (task.isCompleted) 0L else 1L, task.id.toLong())
                    logAuditoria("DEBUG", "Actualización relacional (SQL) exitosa")
                }
                DatabaseType.NOSQL -> {
                    noSqlStore.update { currentTasks ->
                        currentTasks?.map {
                            if (it.id == task.id) it.copy(isCompleted = !it.isCompleted) else it
                        }
                    }
                    logAuditoria("DEBUG", "Actualización basada en documentos (NoSQL) exitosa")
                }
            }
        } catch (e: Exception) {
            logAuditoria("ERROR", "Fallo en operación toggleTask: ${e.message}")
        }
    }

    override suspend fun updateTask(id: Int, title: String, description: String) = withContext(Dispatchers.IO) {
        try {
            when (_currentDatabase.value) {
                DatabaseType.SQL -> {
                    sqlDb.appDatabaseQueries.updateTask(title, description, id.toLong())
                    logAuditoria("DEBUG", "Actualización relacional (SQL) exitosa")
                }
                DatabaseType.NOSQL -> {
                    noSqlStore.update { currentTasks ->
                        currentTasks?.map {
                            // Se corrige la asignación uniforme a 'subtitle' para que concuerde con tu modelo
                            if (it.id == id) it.copy(title = title, subtitle = description) else it
                        }
                    }
                    logAuditoria("DEBUG", "Actualización basada en documentos (NoSQL) exitosa")
                }
            }
        } catch (e: Exception) {
            logAuditoria("ERROR", "Fallo en operación updateTask: ${e.message}")
        }
    }

    override suspend fun getAllTasks(): List<Task> = withContext(Dispatchers.IO) {
        return@withContext try {
            when (_currentDatabase.value) {
                DatabaseType.SQL -> {
                    sqlDb.appDatabaseQueries.selectAll().executeAsList().map {
                        Task(
                            id = it.id.toInt(),
                            title = it.title,
                            subtitle = it.description, // Mapeo correcto de nombres de la DB a UI
                            isCompleted = it.isCompleted != 0L
                        )
                    }
                }
                DatabaseType.NOSQL -> {
                    noSqlStore.get() ?: emptyList()
                }
            }
        } catch (e: Exception) {
            logAuditoria("ERROR", "Fallo en operación getAllTasks: ${e.message}")
            emptyList()
        }
    }
}
