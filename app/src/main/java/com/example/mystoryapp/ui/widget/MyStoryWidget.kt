package com.example.mystoryapp.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.net.toUri
import com.example.mystoryapp.R
import com.example.mystoryapp.di.Injection
import kotlinx.coroutines.runBlocking


class MyStoryWidget : AppWidgetProvider() {

    companion object {
        private const val TOAST_ACTION = "com.dicoding.mystory.TOAST_ACTION"
        const val EXTRA_ITEM = "com.dicoding.mystory.EXTRA_ITEM"
        const val ACTION_REFRESH_WIDGET = "com.dicoding.mystory.ACTION_REFRESH_WIDGET"

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val repository = Injection.provideRepository(context)

            val token = runBlocking { repository.getUserToken() }

            val intent = Intent(context, StackWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = toUri(Intent.URI_INTENT_SCHEME).toUri()
            }

            val views = RemoteViews(context.packageName, R.layout.my_story_widget).apply {
                setRemoteAdapter(R.id.stack_view, intent)
                setEmptyView(R.id.stack_view, R.id.empty_view)

                setViewVisibility(R.id.widget_text, if (token.isNullOrEmpty()) View.VISIBLE else View.GONE)
                setViewVisibility(R.id.stack_view, if (token.isNullOrEmpty()) View.GONE else View.VISIBLE)
            }

            val toastIntent = Intent(context, MyStoryWidget::class.java).apply {
                action = TOAST_ACTION
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val toastPendingIntent = PendingIntent.getBroadcast(
                context, 0, toastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.stack_view, toastPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.stack_view)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val appWidgetManager = AppWidgetManager.getInstance(context)

        when (intent.action) {
            ACTION_REFRESH_WIDGET -> {
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, MyStoryWidget::class.java)
                )
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.stack_view)
            }
            TOAST_ACTION -> {
                val viewIndex = intent.getIntExtra(EXTRA_ITEM, 0)
                Toast.makeText(context, "Item ke-$viewIndex diklik", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun refreshWidget(context: Context) {
        val intent = Intent(context, MyStoryWidget::class.java).apply {
            action = ACTION_REFRESH_WIDGET
        }
        context.sendBroadcast(intent)
    }

}


