package com.example.task

import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class TaskWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TaskRemoteViewsFactory(this.applicationContext)
    }
}
