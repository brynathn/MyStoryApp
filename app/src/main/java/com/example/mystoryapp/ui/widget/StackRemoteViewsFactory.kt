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
import kotlinx.coroutines.runBlocking
import com.example.mystoryapp.AppResult
import com.example.mystoryapp.response.WidgetStoryItem
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class StackRemoteViewsFactory(
    private val context: Context,
    private val repository: Repository
) : RemoteViewsService.RemoteViewsFactory {

    private val widgetStoryItems = mutableListOf<WidgetStoryItem>()

    override fun onCreate() {
        // Initialization
    }

    override fun onDataSetChanged() {
        widgetStoryItems.clear()

        val token = runBlocking { repository.getUserToken() }
        if (!token.isNullOrEmpty()) {
            val result = runBlocking { repository.getAllStories(token) }
            if (result is AppResult.Success) {
                Log.d("Widget", "Fetched ${result.data.size} stories")
                result.data.forEach { story ->
                    Log.d("Widget", "Story: ${story.name}, Photo URL: ${story.photoUrl}")
                    val bitmap = downloadImageAsBitmap(story.photoUrl)
                    widgetStoryItems.add(
                        WidgetStoryItem(
                            id = story.id,
                            name = story.name,
                            description = story.description,
                            photoUrl = story.photoUrl,
                            bitmap = bitmap,
                            createdAt = story.createdAt
                        )
                    )
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
        widgetStoryItems.clear()
    }

    override fun getCount(): Int = widgetStoryItems.size

    override fun getViewAt(position: Int): RemoteViews {
        val widgetStory = widgetStoryItems[position]
        val remoteViews = RemoteViews(context.packageName, R.layout.story_item_widget)

        if (widgetStory.bitmap != null) {
            remoteViews.setImageViewBitmap(R.id.image_story, widgetStory.bitmap)
            Log.d("Widget", "Set bitmap for story: ${widgetStory.name}")
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


