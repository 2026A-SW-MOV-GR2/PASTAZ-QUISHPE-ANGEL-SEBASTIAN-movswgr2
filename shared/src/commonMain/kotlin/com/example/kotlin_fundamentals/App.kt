package com.example.kotlin_fundamentals

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.kotlin_fundamentals.network.PostApiClient
import kotlinx.serialization.Serializable
import com.example.kotlin_fundamentals.repository.DualTaskRepository
import com.example.kotlin_fundamentals.repository.DatabaseType
import com.example.kotlin_fundamentals.security.SecurityStorage
import com.example.kotlin_fundamentals.ui.RestScreen
import com.example.kotlin_fundamentals.ui.SecretsScreen
import com.example.kotlin_fundamentals.viewmodel.PostViewModel
import kotlinx.coroutines.launch

@Serializable
data class Task(
    val id: Int,
    val title: String,
    val subtitle: String,
    val isCompleted: Boolean = false
)

@Composable
fun App(securityStorage: SecurityStorage, internalPath: String? = null) { // <-- Agregamos el parámetro
    MaterialTheme {
        val appDatabase = createAppDatabase()
        val repository = remember { DualTaskRepository(appDatabase, internalPath) }
        val apiClient = remember { PostApiClient() }
        val postViewModel = remember { PostViewModel(apiClient) }
        
        var currentScreen by remember { mutableStateOf("CRUD") }
        
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { currentScreen = "CRUD" }, modifier = Modifier.weight(1f).padding(end = 4.dp)) { Text("Módulo CRUD") }
                Button(onClick = { currentScreen = "REST" }, modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) { Text("Módulo REST") }
                Button(onClick = { currentScreen = "SEGURIDAD" }, modifier = Modifier.weight(1f).padding(start = 4.dp)) { Text("Módulo Seguridad") }
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    "CRUD" -> {
                        var tasks by remember { mutableStateOf(emptyList<Task>()) }
                        val scope = rememberCoroutineScope()
                        val currentDbType by repository.currentDatabase.collectAsState()

                        fun recargarTareas() {
                            scope.launch { tasks = repository.getAllTasks() }
                        }

                        LaunchedEffect(currentDbType) {
                            recargarTareas()
                        }
                        
                        var selectedTaskId by remember { mutableStateOf<Int?>(null) }

                        if (selectedTaskId == null) {
                            ListScreen(
                                tasks = tasks,
                                repository = repository,
                                onNavigateToCreate = { selectedTaskId = -1 },
                                onNavigateToEdit = { id -> selectedTaskId = id },
                                onToggleTask = { task -> 
                                    scope.launch {
                                        repository.toggleTask(task)
                                        recargarTareas()
                                    }
                                },
                                onDelete = { task -> 
                                    scope.launch {
                                        repository.deleteTask(task)
                                        recargarTareas()
                                    }
                                },
                                onRefresh = { recargarTareas() }
                            )
                        } else {
                            FormScreen(
                                taskId = selectedTaskId ?: -1,
                                tasks = tasks,
                                onSave = { title, desc ->
                                    scope.launch {
                                        if (selectedTaskId == -1) {
                                            repository.addTask(title, desc)
                                        } else {
                                            repository.updateTask(selectedTaskId!!, title, desc)
                                        }
                                        selectedTaskId = null
                                        recargarTareas()
                                    }
                                },
                                onBack = { selectedTaskId = null }
                            )
                        }
                    }
                    "REST" -> RestScreen(viewModel = postViewModel)
                    "SEGURIDAD" -> SecretsScreen(securityStorage = securityStorage)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListScreen(
    tasks: List<Task>,
    repository: DualTaskRepository,
    onNavigateToCreate: () -> Unit,
    onNavigateToEdit: (Int) -> Unit,
    onToggleTask: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onRefresh: () -> Unit
) {
    var taskToDelete by remember { mutableStateOf<Task?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val currentDbType by repository.currentDatabase.collectAsState()
    val scope = rememberCoroutineScope()

    toastMessage?.let { ShowToast(it); toastMessage = null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("KMP CRUD") },
                actions = {
                    Switch(
                        checked = currentDbType == DatabaseType.NOSQL,
                        onCheckedChange = {
                            scope.launch {
                                repository.switchDatabase()
                                onRefresh()
                            }
                        }
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(if (currentDbType == DatabaseType.NOSQL) "NoSQL" else "SQLite") },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) { Text("+") }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(tasks, key = { it.id }) { task ->
                ListItem(
                    headlineContent = { Text(task.title) },
                    supportingContent = { Text(task.subtitle) },
                    leadingContent = { 
                        Switch(checked = task.isCompleted, onCheckedChange = { onToggleTask(task) }) 
                    },
                    modifier = Modifier.combinedClickable(
                        onClick = { onNavigateToEdit(task.id) },
                        onLongClick = { taskToDelete = task }
                    )
                )
                HorizontalDivider()
            }
        }

        taskToDelete?.let { task ->
            AlertDialog(
                onDismissRequest = { taskToDelete = null },
                confirmButton = {
                    Button(onClick = { onDelete(task); taskToDelete = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Eliminar") }
                },
                dismissButton = { TextButton(onClick = { taskToDelete = null }) { Text("Cancelar") } },
                title = { Text("¿Eliminar?") },
                text = { Text("Esta acción es permanente.") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    taskId: Int, 
    tasks: List<Task>,
    onSave: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val existingTask = tasks.find { it.id == taskId }
    var title by remember { mutableStateOf(existingTask?.title ?: "") }
    var subtitle by remember { mutableStateOf(existingTask?.subtitle ?: "") }

    Scaffold(
        topBar = { TopAppBar(title = { Text(if (taskId == -1) "Nueva Tarea" else "Editar") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = subtitle, onValueChange = { subtitle = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { onSave(title, subtitle) }, modifier = Modifier.fillMaxWidth()) { Text("Guardar") }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}
