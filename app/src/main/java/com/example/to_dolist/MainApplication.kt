package com.example.to_dolist

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import com.example.to_dolist.db.TaskDatabase

class MainApplication : Application() {

    private var notificationManager : NotificationManager? = null

    companion object
    {
        lateinit var instance: MainApplication
        lateinit var taskDatabase: TaskDatabase

    }

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        instance = this
        taskDatabase = Room.databaseBuilder(
            applicationContext,
            TaskDatabase::class.java,
            "task_data_table"
        ).build()

        createNotificationChannel(this)
    }

    private fun createNotificationChannel(context: Context) {
        val name = "Task Reminder Channel"
        val descriptionText = "Channel for task reminders"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("task_channel", name, importance).apply {
            description = descriptionText
        }

        notificationManager?.createNotificationChannel(channel)

    }


}