package com.example.task

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Window
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class TimerActivity : AppCompatActivity() {

    private lateinit var timerDisplay: TextView
    private lateinit var progressBar: ProgressBar
    private var timer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var totalTimeInMillis: Long = 0
    private lateinit var vibrator: Vibrator

    private val CHANNEL_ID = "timer_alarm_channel"
    private val NOTIFICATION_ID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        timerDisplay = findViewById(R.id.timerDisplay)
        progressBar = findViewById(R.id.progressBar)
        val startButton: Button = findViewById(R.id.startButton)
        val stopButton: Button = findViewById(R.id.stopButton)
        val resetButton: Button = findViewById(R.id.resetButton)
        val selectTimeButton: Button = findViewById(R.id.selectTimeButton)

        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        createNotificationChannel()

        startButton.setOnClickListener { startTimer() }
        stopButton.setOnClickListener { stopTimer() }
        resetButton.setOnClickListener { resetTimer() }
        selectTimeButton.setOnClickListener { openTimePicker() }

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNavigationView.selectedItemId = R.id.nav_timer

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_timer -> true
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_reminder -> {
                    startActivity(Intent(this, ReminderActivity::class.java))
                    true
                }
                else -> false
            }
        }
        changeStatusBarColor()
    }

    private fun openTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            totalTimeInMillis = (selectedHour * 3600 + selectedMinute * 60) * 1000L
            timeLeftInMillis = totalTimeInMillis
            updateTimer()
            progressBar.max = 100
            progressBar.progress = 0
            progressBar.visibility = ProgressBar.VISIBLE
        }, hour, minute, true).show()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimer()
                updateProgressBar()
            }

            override fun onFinish() {
                timerDisplay.text = "Done!"
                progressBar.visibility = ProgressBar.GONE
                showTimerDoneNotification()
                startVibration()
            }
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
        stopVibration()
    }

    private fun resetTimer() {
        timeLeftInMillis = totalTimeInMillis
        updateTimer()
        progressBar.progress = 0
        progressBar.visibility = ProgressBar.GONE
        stopVibration()
    }

    private fun updateTimer() {
        val hours = (timeLeftInMillis / 1000) / 3600
        val minutes = (timeLeftInMillis / 1000 % 3600) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        timerDisplay.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun updateProgressBar() {
        val progress = ((totalTimeInMillis - timeLeftInMillis).toFloat() / totalTimeInMillis * 100).toInt()
        progressBar.progress = progress
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Timer Alarm"
            val descriptionText = "Channel for timer alarm notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 500)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showTimerDoneNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.timer)
            .setContentTitle("Time's Up!")
            .setContentText("Your timer has finished.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun startVibration() {
        val vibrationPattern = longArrayOf(0, 500, 500)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, 0))
        } else {
            vibrator.vibrate(vibrationPattern, 0)
        }
    }

    private fun stopVibration() {
        vibrator.cancel()
    }
    private fun changeStatusBarColor() {
        // Get the window of the current activity
        val window: Window = window
        // Set the status bar color
        window.statusBarColor = getColor(R.color.colorSecondary)
    }
}
