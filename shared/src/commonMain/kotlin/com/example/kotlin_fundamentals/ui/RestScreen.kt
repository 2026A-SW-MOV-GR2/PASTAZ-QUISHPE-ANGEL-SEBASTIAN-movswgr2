package com.example.kotlin_fundamentals.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.kotlin_fundamentals.model.Post
import com.example.kotlin_fundamentals.viewmodel.PostUiState
import com.example.kotlin_fundamentals.viewmodel.PostViewModel
import kotlinx.coroutines.launch

@Composable
fun RestScreen(viewModel: PostViewModel) {
    val state by viewModel.uiState.collectAsState()
    val (idInput, setIdInput) = remember { mutableStateOf("") }
    val (titleInput, setTitleInput) = remember { mutableStateOf("") }
    val (bodyInput, setBodyInput) = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state) {
        if (state is PostUiState.Success) {
            val post = (state as PostUiState.Success).post
            setTitleInput(post.title)
            setBodyInput(post.body)
        }
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        OutlinedTextField(
            value = idInput,
            onValueChange = setIdInput,
            label = { Text("ID") },
            enabled = state !is PostUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                scope.launch {
                    viewModel.fetchPost(idInput.toIntOrNull() ?: 1)
                }
            },
            enabled = state !is PostUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Obtener")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = titleInput,
            onValueChange = setTitleInput,
            label = { Text("Título") },
            enabled = state !is PostUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = bodyInput,
            onValueChange = setBodyInput,
            label = { Text("Body") },
            enabled = state !is PostUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                scope.launch {
                    viewModel.updatePost(
                        idInput.toIntOrNull() ?: 1,
                        Post(
                            id = idInput.toIntOrNull() ?: 1,
                            title = titleInput,
                            body = bodyInput
                        )
                    )
                }
            },
            enabled = state !is PostUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Actualizar")
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (state is PostUiState.Error) {
            Text(
                text = (state as PostUiState.Error).message,
                color = Color.Red
            )
        }
    }
}

