package com.example.kotlin_fundamentals.security

// Implementación stub para iOS (puede implementarse completamente después)
class IosSecurityStorage : SecurityStorage {
    override suspend fun saveSecret(key: String, value: String, mechanism: StorageMechanism) {
        // Implementación para iOS - puede usar UserDefaults
        println("IOS: Saved secret $key")
    }

    override suspend fun getSecret(key: String, mechanism: StorageMechanism): String? {
        // Implementación para iOS - puede usar UserDefaults
        println("IOS: Retrieved secret $key")
        return null
    }
}

