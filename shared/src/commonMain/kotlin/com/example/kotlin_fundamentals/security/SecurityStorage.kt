package com.example.kotlin_fundamentals.security

interface SecurityStorage {
    suspend fun saveSecret(key: String, value: String, mechanism: StorageMechanism)
    suspend fun getSecret(key: String, mechanism: StorageMechanism): String?
}

