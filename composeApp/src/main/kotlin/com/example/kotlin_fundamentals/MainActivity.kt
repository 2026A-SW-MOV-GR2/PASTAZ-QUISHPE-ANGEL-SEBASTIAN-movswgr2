package com.example.kotlin_fundamentals

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.kotlin_fundamentals.security.AndroidSecurityStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val securityStorage = AndroidSecurityStorage(this)
        
        setContent {
            App(
                securityStorage = securityStorage,
                internalPath = applicationContext.filesDir.absolutePath
            )
        }

    }
}
