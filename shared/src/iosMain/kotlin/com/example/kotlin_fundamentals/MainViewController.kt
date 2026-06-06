package com.example.kotlin_fundamentals

import androidx.compose.ui.window.ComposeUIViewController
import com.example.kotlin_fundamentals.security.IosSecurityStorage

fun MainViewController() = ComposeUIViewController { App(securityStorage = IosSecurityStorage()) }
