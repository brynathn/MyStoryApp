package com.example.mystoryapp

import com.example.mystoryapp.response.StoryItem

object DataDummy {
    fun generateDummyStoryList(): List<StoryItem> {
        val stories = mutableListOf<StoryItem>()
        for (i in 1..10) {
            stories.add(
                StoryItem(
                    id = "story-$i",
                    name = "Name $i",
                    description = "Description $i",
                    photoUrl = "https://dummyimage.com/600x400/000/fff&text=Story+$i",
                    createdAt = "2024-01-01T00:00:00Z",
                    lat = -6.2 + i,
                    lon = 106.8 + i
                )
            )
        }
        return stories
    }
}
