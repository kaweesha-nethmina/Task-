package com.example.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var tasks: List<Task> = listOf()
    private var filteredTasks: List<Task> = listOf()

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)

        fun bind(task: Task) {
            taskTitle.text = task.name
            itemView.setOnClickListener { onEditClick(task) }
            deleteIcon.setOnClickListener { onDeleteClick(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(filteredTasks[position])
    }

    override fun getItemCount(): Int = filteredTasks.size

    fun setTasks(tasks: List<Task>) {
        this.tasks = tasks
        this.filteredTasks = tasks
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredTasks = if (query.isEmpty()) {
            tasks
        } else {
            tasks.filter { it.name.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }
}
