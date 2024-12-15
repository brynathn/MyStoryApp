package com.example.mystoryapp.ui.main

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.mystoryapp.response.StoryItem
import com.example.mystoryapp.retrofit.ApiService

class StoryPagingSource(
    private val apiService: ApiService,
    private val token: String
) : PagingSource<Int, StoryItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryItem> {
        val page = params.key ?: 1
        val size = params.loadSize

        return try {
            val response = apiService.getAllStories("Bearer $token", page, size)
            val stories = response.listStory ?: emptyList()

            LoadResult.Page(
                data = stories,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (stories.isEmpty()) null else page + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, StoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
