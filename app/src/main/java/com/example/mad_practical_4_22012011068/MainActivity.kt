package com.example.mad_practical_4_22012011068

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mad_practical_4_22012011068.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var selectedAlarmTime: Long = 0
    private var reminderTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btn1.setOnClickListener {
            showTimerDialog()
        }

        binding.btn2.setOnClickListener {
            cancelAlarm()
        }

        binding.card2.visibility = View.GONE
        binding.reminderTime.visibility = View.GONE

        val timePicker = binding.reminderTime
        timePicker.hour = getHour()
        timePicker.minute = getMinute()
    }

    private fun getHour(): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = reminderTime
        return cal[Calendar.HOUR_OF_DAY]
    }

    private fun getMinute(): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = selectedAlarmTime
        return cal[Calendar.MINUTE]
    }

    private fun showTimerDialog() {
        val cldr = Calendar.getInstance()
        val hour: Int = cldr.get(Calendar.HOUR_OF_DAY)
        val minutes: Int = cldr.get(Calendar.MINUTE)
        val picker = TimePickerDialog(
            this,
            { _, sHour, sMinute -> sendDialogDataToActivity(sHour, sMinute) },
            hour,
            minutes,
            false
        )
        picker.show()
    }

    private fun sendDialogDataToActivity(hour: Int, minute: Int) {
        val alarmCalendar = Calendar.getInstance()
        val year: Int = alarmCalendar.get(Calendar.YEAR)
        val month: Int = alarmCalendar.get(Calendar.MONTH)
        val day: Int = alarmCalendar.get(Calendar.DATE)
        alarmCalendar.set(year, month, day, hour, minute, 0)

        binding.card2.visibility = View.VISIBLE
        binding.alarmTimeText.text = SimpleDateFormat("hh:mm ss a dd MMM yyyy").format(alarmCalendar.time)
        selectedAlarmTime = alarmCalendar.timeInMillis
        setAlarm(selectedAlarmTime, "Start")

        Toast.makeText(this, "Time: hours: $hour, minutes: $minute", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setAlarm(millisTime: Long, action: String) {
        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        intent.putExtra("Service1", action)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            2407,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        if (action == "Start") {
            binding.reminderTime.visibility = View.VISIBLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ check for exact alarm permission
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        millisTime,
                        pendingIntent
                    )
                    Toast.makeText(this, "Alarm set", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Exact alarms not permitted on this device", Toast.LENGTH_SHORT).show()
                }
            } else {
                // For devices below Android 12
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    millisTime,
                    pendingIntent
                )
                Toast.makeText(this, "Alarm set", Toast.LENGTH_SHORT).show()
            }
        } else if (action == "Stop") {
            alarmManager.cancel(pendingIntent)
            binding.card2.visibility = View.GONE
        }
    }

    private fun cancelAlarm() {
        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        intent.putExtra("Service1", "Stop")
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            2407,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        binding.card2.visibility = View.GONE
        Toast.makeText(this, "Alarm canceled", Toast.LENGTH_SHORT).show()
    }
}
