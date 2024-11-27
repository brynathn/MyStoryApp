package com.example.mystoryapp.ui.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.mystoryapp.R
import com.example.mystoryapp.data.Repository
import com.example.mystoryapp.response.StoryItem
import kotlinx.coroutines.runBlocking
import com.example.mystoryapp.Result
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class StackRemoteViewsFactory(
    private val context: Context,
    private val repository: Repository
) : RemoteViewsService.RemoteViewsFactory {

    private val storyItems = mutableListOf<StoryItem>()

    override fun onCreate() {
        // Initialization
    }

    override fun onDataSetChanged() {
        storyItems.clear()

        val token = runBlocking { repository.getUserToken() }
        if (!token.isNullOrEmpty()) {
            val result = runBlocking { repository.getAllStories(token) }
            if (result is Result.Success) {
                Log.d("Widget", "Fetched ${result.data.size} stories")
                result.data.forEach { story ->
                    Log.d("Widget", "Story: ${story.name}, Photo URL: ${story.photoUrl}")
                    val bitmap = downloadImageAsBitmap(story.photoUrl)
                    if (bitmap != null) {
                        storyItems.add(
                            story.copy(photoUrl = story.photoUrl, bitmap = bitmap)
                        )
                    } else {
                        Log.w("Widget", "Failed to download image for story: ${story.name}")
                    }
                }
            } else {
                Log.e("Widget", "Failed to fetch stories")
            }
        } else {
            Log.d("Widget", "User not logged in")
        }
    }


    private fun downloadImageAsBitmap(imageUrl: String): Bitmap? {
        return try {
            val url = URL(imageUrl)
            Log.d("Widget", "Downloading image from: $imageUrl")
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream = connection.inputStream

            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            Log.d("Widget", "Image downloaded successfully.")
            bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Widget", "Failed to download image: ${e.message}")
            null
        }
    }

    override fun onDestroy() {
        storyItems.clear()
    }

    override fun getCount(): Int = storyItems.size

    override fun getViewAt(position: Int): RemoteViews {
        val story = storyItems[position]
        val remoteViews = RemoteViews(context.packageName, R.layout.story_item_widget)

        if (story.bitmap != null) {
            remoteViews.setImageViewBitmap(R.id.image_story, story.bitmap)
            Log.d("Widget", "Set bitmap for story: ${story.name}")
        } else {
            Log.w("Widget", "Bitmap is null. Using placeholder.")
            remoteViews.setImageViewResource(R.id.image_story, R.drawable.ic_place_holder)
        }

        val fillInIntent = Intent().apply {
            putExtra(MyStoryWidget.EXTRA_ITEM, position)
        }
        remoteViews.setOnClickFillInIntent(R.id.image_story, fillInIntent)

        return remoteViews
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}

