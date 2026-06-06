package com.example.kotlin_fundamentals

import androidx.compose.runtime.Composable
import com.example.kotlin_fundamentals.db.AppDatabase

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun ShowToast(message: String)

@Composable
expect fun createAppDatabase(): AppDatabase
