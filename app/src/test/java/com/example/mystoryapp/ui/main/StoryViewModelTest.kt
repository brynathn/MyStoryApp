package com.example.mystoryapp.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.mystoryapp.DataDummy
import com.example.mystoryapp.MainDispatcherRule
import com.example.mystoryapp.data.Repository
import com.example.mystoryapp.getOrAwaitValue
import com.example.mystoryapp.response.StoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: Repository

    @Test
    fun `when Get Stories Should Not Null and Return Data`() = runTest {
        val token = "mocked_token"
        val dummyStoryList = DataDummy.generateDummyStoryList()
        val pagingData = QuotePagingSource.snapshot(dummyStoryList)

        Mockito.`when`(storyRepository.getUserToken()).thenReturn(token)
        Mockito.`when`(storyRepository.getStoryCount()).thenReturn(0)
        val expectedLiveData = MutableLiveData<PagingData<StoryItem>>()
        expectedLiveData.value = pagingData
        Mockito.`when`(storyRepository.getStoriesPagingData(token)).thenReturn(expectedLiveData)

        val mainViewModel = StoryViewModel(storyRepository)
        val actualData = mainViewModel.stories.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualData)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStoryList.size, differ.snapshot().size)
        Assert.assertEquals(dummyStoryList[0], differ.snapshot()[0])
    }


    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val token = "mocked_token"
        Mockito.`when`(storyRepository.getUserToken()).thenReturn(token)
        Mockito.`when`(storyRepository.getStoryCount()).thenReturn(0)

        val data: PagingData<StoryItem> = PagingData.from(emptyList())
        val expectedLiveData = MutableLiveData<PagingData<StoryItem>>()
        expectedLiveData.value = data
        Mockito.`when`(storyRepository.getStoriesPagingData(token)).thenReturn(expectedLiveData)

        val mainViewModel = StoryViewModel(storyRepository)

        val actualData: PagingData<StoryItem> = mainViewModel.stories.getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualData)

        Assert.assertEquals(0, differ.snapshot().size)
    }

}

class QuotePagingSource : PagingSource<Int, StoryItem>() {
    companion object {
        fun snapshot(items: List<StoryItem>): PagingData<StoryItem> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, StoryItem>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryItem> {
        return LoadResult.Page(emptyList(), null, null)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}






