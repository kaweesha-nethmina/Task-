package com.example.task

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReminderAdapter(
    private val reminders: MutableList<Reminder>,
    private val onEditClick: (Reminder) -> Unit,
    private val onDeleteClick: (Reminder) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reminderTextView: TextView = itemView.findViewById(R.id.reminderTextView)
        val editButton: ImageView = itemView.findViewById(R.id.editButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reminder_item, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.reminderTextView.text = reminder.text

        holder.editButton.setOnClickListener { onEditClick(reminder) }
        holder.deleteButton.setOnClickListener { onDeleteClick(reminder) }
    }

    override fun getItemCount(): Int = reminders.size
}
