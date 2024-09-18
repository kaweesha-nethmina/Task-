package com.example.task

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var searchBar: EditText
    private val PREFS_NAME = "tasks_prefs"
    private val TASKS_KEY = "tasks"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        taskAdapter = TaskAdapter(
            onEditClick = { task ->
                val intent = Intent(this, AddTaskActivity::class.java).apply {
                    putExtra("task", task)
                }
                startActivity(intent)
            },
            onDeleteClick = { task ->
                showDeleteConfirmation(task)
            }
        )
        recyclerView.adapter = taskAdapter

        searchBar = findViewById(R.id.search_bar)
        searchBar.addTextChangedListener { text ->
            val query = text.toString()
            taskAdapter.filter(query)
        }

        val fabAddTask: FloatingActionButton = findViewById(R.id.fab_add_task)
        fabAddTask.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivity(intent)
        }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNavigationView.selectedItemId = R.id.nav_home // Ensure home is selected on start

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_timer -> {
                    startActivity(Intent(this, TimerActivity::class.java))
                    true
                }
                R.id.nav_reminder -> {
                    startActivity(Intent(this, ReminderActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    private fun loadTasks() {
        GlobalScope.launch {
            try {
                val tasks = loadTasksFromSharedPreferences()
                runOnUiThread {
                    taskAdapter.setTasks(tasks)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error loading tasks: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadTasksFromSharedPreferences(): List<Task> {
        val json = sharedPreferences.getString(TASKS_KEY, "[]") ?: "[]"
        return Gson().fromJson(json, Array<Task>::class.java).toList()
    }

    private fun showDeleteConfirmation(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Yes") { _, _ -> deleteTask(task) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteTask(task: Task) {
        GlobalScope.launch {
            try {
                val tasks = loadTasksFromSharedPreferences().toMutableList()
                tasks.removeIf { it.id == task.id }
                saveTasksToSharedPreferences(tasks)
                runOnUiThread {
                    taskAdapter.setTasks(tasks)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error deleting task: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveTasksToSharedPreferences(tasks: List<Task>) {
        val json = Gson().toJson(tasks)
        sharedPreferences.edit().putString(TASKS_KEY, json).apply()
    }
}
