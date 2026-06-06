package com.example.kotlin_fundamentals.security

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val Context.dataStore by preferencesDataStore(name = "app_datastore")

class AndroidSecurityStorage(private val context: Context) : SecurityStorage {
    
    private fun getMasterKey(): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }
    
    private fun getEncryptedPreferences(): android.content.SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            "encrypted_prefs",
            getMasterKey(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override suspend fun saveSecret(key: String, value: String, mechanism: StorageMechanism) {
        when (mechanism) {
            StorageMechanism.SHARED_PREFERENCES -> {
                withContext(Dispatchers.IO) {
                    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString(key, value).apply()
                }
            }
            StorageMechanism.DATASTORE -> {
                val preferenceKey = stringPreferencesKey(key)
                context.dataStore.edit { preferences ->
                    preferences[preferenceKey] = value
                }
            }
            StorageMechanism.ENCRYPTED_PREFS -> {
                withContext(Dispatchers.IO) {
                    val encryptedPrefs = getEncryptedPreferences()
                    encryptedPrefs.edit().putString(key, value).apply()
                }
            }
        }
    }

    override suspend fun getSecret(key: String, mechanism: StorageMechanism): String? {
        return when (mechanism) {
            StorageMechanism.SHARED_PREFERENCES -> {
                withContext(Dispatchers.IO) {
                    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    prefs.getString(key, null)
                }
            }
            StorageMechanism.DATASTORE -> {
                val preferenceKey = stringPreferencesKey(key)
                context.dataStore.data.first()[preferenceKey]
            }
            StorageMechanism.ENCRYPTED_PREFS -> {
                withContext(Dispatchers.IO) {
                    val encryptedPrefs = getEncryptedPreferences()
                    encryptedPrefs.getString(key, null)
                }
            }
        }
    }
}

