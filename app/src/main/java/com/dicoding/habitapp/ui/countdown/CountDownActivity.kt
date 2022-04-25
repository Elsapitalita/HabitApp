package com.dicoding.habitapp.ui.countdown

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.notification.NotificationWorker
import com.dicoding.habitapp.utils.HABIT
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HABIT_TITLE

class CountDownActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_down)
        supportActionBar?.title = "Count Down"

        val habit = intent.getParcelableExtra<Habit>(HABIT) as Habit

        findViewById<TextView>(R.id.tv_count_down_title).text = habit.title
        val countDown = findViewById<TextView>(R.id.tv_count_down)

        val viewModel = ViewModelProvider(this).get(CountDownViewModel::class.java)

        //TODO 10 : Set initial time and observe current time. Update button state when countdown is finished
        viewModel.setInitialTime(habit.minutesFocus)
        viewModel.currentTimeString.observe(this, {
            currentTime -> findViewById<TextView>(R.id.tv_count_down).text = currentTime
        })

        viewModel.eventCountDownFinish.observe(this, {
            isFinishing -> updateButtonState(!isFinishing)
        })
        //TODO 13 : Start and cancel One Time Request WorkManager to notify when time is up.


        findViewById<Button>(R.id.btn_start).setOnClickListener {
            viewModel.currentTimeString.observe(this, {
                    time -> if (time == "00:00"){
                        val workManager = WorkManager.getInstance(this)
                        val inputData = Data.Builder()
                            .putInt(HABIT_ID, habit.id)
                            .putString(HABIT_TITLE, habit.title)
                            .build()
                        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                            .setInputData(inputData)
                            .build()
                    workManager.enqueue(oneTimeWorkRequest)
                    }
            })
            viewModel.startTimer()
            updateButtonState(true)
        }

        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            viewModel.resetTimer()
            updateButtonState(false)
            WorkManager.getInstance(this).cancelAllWork()
        }
    }

    private fun updateButtonState(isRunning: Boolean) {
        findViewById<Button>(R.id.btn_start).isEnabled = !isRunning
        findViewById<Button>(R.id.btn_stop).isEnabled = isRunning
    }
}