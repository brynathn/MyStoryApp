package com.example.mystoryapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.mystoryapp.UserPreferences
import com.example.mystoryapp.data.Repository
import com.example.mystoryapp.database.StoryDatabase
import com.example.mystoryapp.retrofit.ApiConfig

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")
object Injection {
    fun provideRepository(context: Context): Repository {
        val dataStore = context.dataStore
        val database = StoryDatabase.getDatabase(context)
        val userPreferences = UserPreferences.getInstance(dataStore)
        val apiService = ApiConfig.getApiService()
        return Repository(apiService, userPreferences, context, database)
    }
}



