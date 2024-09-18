package com.example.task

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.google.gson.Gson

class TaskRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private val sharedPreferences = context.getSharedPreferences("tasks_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private var tasks = listOf<Task>()

    override fun onCreate() {
        // Initialize tasks data if needed
        loadTasks()
    }

    override fun onDataSetChanged() {
        loadTasks()
    }

    override fun onDestroy() {}

    private fun loadTasks() {
        val json = sharedPreferences.getString("tasks", "[]") ?: "[]"
        tasks = gson.fromJson(json, Array<Task>::class.java).toList()
    }

    override fun getCount(): Int = tasks.size

    override fun getViewAt(position: Int): RemoteViews {
        val task = tasks[position]
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_list_item)
        remoteViews.setTextViewText(R.id.taskTitle, task.name)

        val fillInIntent = Intent().apply {
            putExtra("TASK_ID", task.id)
        }
        remoteViews.setOnClickFillInIntent(R.id.taskTitle, fillInIntent)

        return remoteViews
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}
