package com.example.kotlin_fundamentals

import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.kotlin_fundamentals.db.AppDatabase

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

@Composable
actual fun ShowToast(message: String) {
    val context = LocalContext.current
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@Composable
actual fun createAppDatabase(): AppDatabase {
    val context = LocalContext.current
    val driver = AndroidSqliteDriver(AppDatabase.Schema, context, "tasks.db")
    return AppDatabase(driver)
}
