package com.example.todoapp.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesManager"

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context,
    ) {

    private val dataStore = context.createDataStore("user_preferences")

    val preferencesFlow = dataStore.data
        .catch {ex ->
            if(ex is IOException){
                emit(emptyPreferences()) // will use the default value of the elvis of below
            }
            else {
                throw ex
            }
            Log.e(TAG, "Error retrieving preferences: ", ex.cause )
        }
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKey.SORT_ORDER] ?: SortOrder.BY_DATE.name
            )
            val hideCompleted = preferences[PreferencesKey.HIDE_COMPLETED] ?: false

            // this is returned
            FilterPreferences(sortOrder, hideCompleted)
        }

    private object PreferencesKey {
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val HIDE_COMPLETED = preferencesKey<Boolean>("hide_completed")
    }

    suspend fun updateSortOrder(sortOrder: SortOrder){
        dataStore.edit {preferences ->
            preferences[PreferencesKey.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateHideCompleted(hideCompleted: Boolean){
        dataStore.edit {preferences ->
            preferences[PreferencesKey.HIDE_COMPLETED] = hideCompleted
        }
    }

}


enum class SortOrder {
    BY_NAME,
    BY_DATE
}