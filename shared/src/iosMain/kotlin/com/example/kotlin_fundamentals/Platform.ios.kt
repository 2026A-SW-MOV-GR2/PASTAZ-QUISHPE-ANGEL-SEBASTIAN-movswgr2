package com.example.kotlin_fundamentals

import androidx.compose.runtime.Composable
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.example.kotlin_fundamentals.db.AppDatabase
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@Composable
actual fun ShowToast(message: String) {
    println("iOS Toast: $message")
}

@Composable
actual fun createAppDatabase(): AppDatabase {
    val driver = NativeSqliteDriver(AppDatabase.Schema, "tasks.db")
    return AppDatabase(driver)
}
