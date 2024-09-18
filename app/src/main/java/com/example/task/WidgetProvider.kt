package com.example.task

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class WidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        for (appWidgetId in appWidgetIds) {
            val widgetLayout = RemoteViews(context.packageName, R.layout.widget_layout)

            // Create intent to open AddTaskActivity
            val intent = Intent(context, AddTaskActivity::class.java).apply {
                action = "ADD_TASK"
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            widgetLayout.setOnClickPendingIntent(R.id.widgetButton, pendingIntent)

            // Set up the RemoteViewsFactory to provide data to the widget
            val intentService = Intent(context, TaskWidgetService::class.java)
            widgetLayout.setRemoteAdapter(R.id.widgetList, intentService)

            appWidgetManager.updateAppWidget(appWidgetId, widgetLayout)
        }
    }
}
