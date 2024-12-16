package com.example.mystoryapp.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mystoryapp.response.StoryItem

@Dao
interface StoryDao {

    @Query("SELECT * FROM story")
    fun getStories(): PagingSource<Int, StoryItem>

    @Query("SELECT COUNT(*) FROM story")
    suspend fun getStoryCount(): Int

    @Query("SELECT * FROM story WHERE id = :storyId")
    suspend fun getStoryById(storyId: String): StoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stories: List<StoryItem>)

    @Query("DELETE FROM story")
    suspend fun clearAllStories()
}
