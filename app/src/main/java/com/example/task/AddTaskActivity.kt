package com.example.task

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddTaskActivity : AppCompatActivity() {
    private lateinit var nameEditText: TextInputEditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var saveButton: MaterialButton
    private lateinit var sharedPreferences: SharedPreferences
    private var taskToEdit: Task? = null
    private val PREFS_NAME = "tasks_prefs"
    private val TASKS_KEY = "tasks"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        nameEditText = findViewById(R.id.nameEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        saveButton = findViewById(R.id.saveButton)

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        if (intent.action == "ADD_TASK") {
            // New task, no need to populate fields
        } else {
            taskToEdit = intent.getParcelableExtra("task")
            taskToEdit?.let {
                nameEditText.setText(it.name)
                descriptionEditText.setText(it.description)
            }
        }

        saveButton.setOnClickListener {
            saveTask()
        }
        changeStatusBarColor()
    }


    private fun saveTask() {
        val name = nameEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()

        if (name.isNotEmpty()) {
            val taskId = taskToEdit?.id ?: System.currentTimeMillis().toInt()
            val task = Task(
                id = taskId,
                name = name,
                description = description
            )

            GlobalScope.launch {
                try {
                    val tasks = loadTasksFromSharedPreferences().toMutableList()
                    if (taskToEdit != null) {
                        val index = tasks.indexOfFirst { it.id == taskToEdit!!.id }
                        if (index != -1) {
                            tasks[index] = task
                        }
                    } else {
                        tasks.add(task)
                    }
                    saveTasksToSharedPreferences(tasks)
                    setResult(Activity.RESULT_OK)
                    finish()
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@AddTaskActivity, "Error saving task: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            nameEditText.error = "Task name cannot be blank"
        }
    }

    private fun loadTasksFromSharedPreferences(): List<Task> {
        val json = sharedPreferences.getString(TASKS_KEY, "[]") ?: "[]"
        return Gson().fromJson(json, Array<Task>::class.java).toList()
    }

    private fun saveTasksToSharedPreferences(tasks: List<Task>) {
        val json = Gson().toJson(tasks)
        sharedPreferences.edit().putString(TASKS_KEY, json).apply()
    }
    private fun changeStatusBarColor() {
        // Get the window of the current activity
        val window: Window = window
        // Set the status bar color
        window.statusBarColor = getColor(R.color.colorSecondary)
    }
}
