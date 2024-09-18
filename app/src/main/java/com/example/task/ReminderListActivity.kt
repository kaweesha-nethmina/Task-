import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.task.R
import com.example.task.Reminder
import com.example.task.ReminderAdapter

class ReminderListActivity : AppCompatActivity() {

    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var recyclerView: RecyclerView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_list)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.reminderRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sample data - ensure this is a MutableList
        val reminders: MutableList<Reminder> = mutableListOf() // Replace with actual data source

        // Initialize the adapter with necessary parameters
        reminderAdapter = ReminderAdapter(
            reminders = reminders,
            onEditClick = { reminder ->
                // Handle edit click
            },
            onDeleteClick = { reminder ->
                // Handle delete click
            }
        )
        recyclerView.adapter = reminderAdapter
    }
}
