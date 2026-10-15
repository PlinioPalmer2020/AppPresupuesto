package com.pliniopalmer.presupuesto.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {
    companion object {
        private val KEY_ACTIVE_USER_ID = longPreferencesKey("active_user_id")
    }

    val activeUserIdFlow: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[KEY_ACTIVE_USER_ID]
    }

    suspend fun setActiveUserId(userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACTIVE_USER_ID] = userId
        }
    }

    suspend fun clearActiveUserId() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_ACTIVE_USER_ID)
        }
    }
}
