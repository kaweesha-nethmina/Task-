package com.example.task

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReminderActivity : AppCompatActivity() {

    private lateinit var calendarView: CalendarView
    private lateinit var timePicker: TimePicker
    private lateinit var reminderText: EditText
    private lateinit var setReminderButton: Button
    private lateinit var reminderRecyclerView: RecyclerView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var reminderAdapter: ReminderAdapter
    private val remindersList = mutableListOf<Reminder>()
    private var currentReminder: Reminder? = null
    private lateinit var vibrator: Vibrator
    private var selectedDate: String? = null
    private var selectedTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        // Initialize views
        calendarView = findViewById(R.id.calendarView)
        timePicker = findViewById(R.id.timePicker)
        reminderText = findViewById(R.id.reminderText)
        setReminderButton = findViewById(R.id.setReminderButton)
        reminderRecyclerView = findViewById(R.id.reminderRecyclerView)

        // Initialize vibrator service
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)

        // Setup RecyclerView
        reminderAdapter = ReminderAdapter(remindersList, this::editReminder, this::deleteReminder)
        reminderRecyclerView.layoutManager = LinearLayoutManager(this)
        reminderRecyclerView.adapter = reminderAdapter

        // Handle date selection
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
            Toast.makeText(this, "Selected date: $selectedDate", Toast.LENGTH_SHORT).show()
        }

        // Handle time selection
        timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
        }

        // Set reminder button click listener
        setReminderButton.setOnClickListener {
            val reminderTextValue = reminderText.text.toString()
            if (!reminderTextValue.isBlank() && !selectedDate.isNullOrEmpty() && !selectedTime.isNullOrEmpty()) {
                val reminderDateTime = "$selectedDate $selectedTime"
                currentReminder?.let {
                    updateReminder(it.id, reminderTextValue, reminderDateTime)
                } ?: addReminder(reminderTextValue, reminderDateTime)
            } else {
                Toast.makeText(this, "Please select a date, time, and enter reminder text", Toast.LENGTH_SHORT).show()
            }
        }

        // Load reminders
        loadReminders()

        // Bottom Navigation setup
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNavigationView.selectedItemId = R.id.nav_reminder

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_reminder -> true
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_timer -> {
                    startActivity(Intent(this, TimerActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun addReminder(reminderTextContent: String, dateTime: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val id = getNextReminderId()
                val reminder = Reminder(id, reminderTextContent, dateTime)
                saveReminderToPreferences(reminder)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReminderActivity, "Reminder added successfully", Toast.LENGTH_SHORT).show()
                    resetFields()
                    loadReminders()
                }
            } catch (e: Exception) {
                Log.e("ReminderActivity", "Error adding reminder: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReminderActivity, "Error adding reminder. Please check logs for details.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateReminder(id: Int, text: String, dateTime: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminders = loadRemindersFromPreferences().toMutableList()
                val index = reminders.indexOfFirst { it.id == id }
                if (index != -1) {
                    reminders[index] = Reminder(id, text, dateTime)
                    saveAllRemindersToPreferences(reminders)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ReminderActivity, "Reminder updated successfully", Toast.LENGTH_SHORT).show()
                        resetFields()
                        loadReminders()
                    }
                }
            } catch (e: Exception) {
                Log.e("ReminderActivity", "Error updating reminder: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReminderActivity, "Error updating reminder. Please check logs for details.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadReminders() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminders = loadRemindersFromPreferences()
                remindersList.clear()
                remindersList.addAll(reminders)
                withContext(Dispatchers.Main) {
                    reminderAdapter.notifyDataSetChanged()

                    // Check if any reminders are due (for demonstration purposes)
                    remindersList.forEach { reminder ->
                        if (isReminderDue(reminder)) {
                            triggerReminderVibration(reminder)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ReminderActivity", "Error loading reminders: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReminderActivity, "Error loading reminders: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isReminderDue(reminder: Reminder): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val reminderTime = sdf.parse(reminder.dateTime)?.time ?: return false
        val currentTime = System.currentTimeMillis()
        return currentTime >= reminderTime
    }

    private fun editReminder(reminder: Reminder) {
        currentReminder = reminder
        reminderText.setText(reminder.text)

        val dateTimeParts = reminder.dateTime.split(" ")
        if (dateTimeParts.size == 2) {
            val dateParts = dateTimeParts[0].split("-")
            val timeParts = dateTimeParts[1].split(":")

            if (dateParts.size == 3 && timeParts.size == 2) {
                val year = dateParts[0].toInt()
                val month = dateParts[1].toInt() - 1 // Calendar months are 0-based
                val day = dateParts[2].toInt()
                val hour = timeParts[0].toInt()
                val minute = timeParts[1].toInt()

                calendarView.date = Calendar.getInstance().apply {
                    set(year, month, day)
                }.timeInMillis

                timePicker.hour = hour
                timePicker.minute = minute
            }
        }
    }

    private fun deleteReminder(reminder: Reminder) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Reminder")
        builder.setMessage("Are you sure you want to delete this reminder?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val reminders = loadRemindersFromPreferences()
                    val updatedReminders = reminders.filter { it.id != reminder.id }
                    saveAllRemindersToPreferences(updatedReminders)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ReminderActivity, "Reminder deleted successfully", Toast.LENGTH_SHORT).show()
                        loadReminders() // Refresh the reminder list
                    }
                } catch (e: Exception) {
                    Log.e("ReminderActivity", "Error deleting reminder: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ReminderActivity, "Error deleting reminder. Please check logs for details.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun triggerReminderVibration(reminder: Reminder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
        // Optionally, show a notification or dialog for the reminder
        AlertDialog.Builder(this)
            .setTitle("Reminder")
            .setMessage(reminder.text)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun saveReminderToPreferences(reminder: Reminder) {
        val editor = sharedPreferences.edit()
        editor.putString("reminder_${reminder.id}", "${reminder.text}|${reminder.dateTime}")
        editor.apply()
    }

    private fun loadRemindersFromPreferences(): List<Reminder> {
        val reminders = mutableListOf<Reminder>()
        val allEntries = sharedPreferences.all
        for ((key, value) in allEntries) {
            if (key.startsWith("reminder_")) {
                val reminderData = value as? String
                reminderData?.let {
                    val parts = it.split("|")
                    if (parts.size == 2) {
                        val id = key.removePrefix("reminder_").toInt()
                        val text = parts[0]
                        val dateTime = parts[1]
                        reminders.add(Reminder(id, text, dateTime))
                    }
                }
            }
        }
        return reminders
    }

    private fun saveAllRemindersToPreferences(reminders: List<Reminder>) {
        val editor = sharedPreferences.edit()
        editor.clear()
        for (reminder in reminders) {
            editor.putString("reminder_${reminder.id}", "${reminder.text}|${reminder.dateTime}")
        }
        editor.apply()
    }

    private fun getNextReminderId(): Int {
        val reminders = loadRemindersFromPreferences()
        return if (reminders.isEmpty()) 1 else reminders.maxOf { it.id } + 1
    }

    private fun resetFields() {
        reminderText.text.clear()
        selectedDate = null
        selectedTime = null
        calendarView.date = System.currentTimeMillis()
        timePicker.hour = 0
        timePicker.minute = 0
        currentReminder = null
    }
}
