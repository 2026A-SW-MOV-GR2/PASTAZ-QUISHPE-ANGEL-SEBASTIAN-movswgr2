package com.example.kotlin_fundamentals.viewmodel

import com.example.kotlin_fundamentals.model.Post
import com.example.kotlin_fundamentals.network.PostApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface PostUiState {
    object Idle : PostUiState
    object Loading : PostUiState
    data class Success(val post: Post) : PostUiState
    data class Error(val message: String) : PostUiState
}

class PostViewModel(private val apiClient: PostApiClient) {
    private val _uiState = MutableStateFlow<PostUiState>(PostUiState.Idle)
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    suspend fun fetchPost(id: Int) {
        _uiState.value = PostUiState.Loading
        try {
            val post = apiClient.getPost(id)
            _uiState.value = PostUiState.Success(post)
        } catch (e: Exception) {
            _uiState.value = PostUiState.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun updatePost(id: Int, post: Post) {
        _uiState.value = PostUiState.Loading
        try {
            val updatedPost = apiClient.updatePost(id, post)
            _uiState.value = PostUiState.Success(updatedPost)
        } catch (e: Exception) {
            _uiState.value = PostUiState.Error(e.message ?: "Unknown error")
        }
    }
}

