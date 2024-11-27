package com.example.mystoryapp.ui.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.example.mystoryapp.di.Injection

class StackWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        val repository = Injection.provideRepository(applicationContext)
        return StackRemoteViewsFactory(applicationContext, repository)
    }
}
