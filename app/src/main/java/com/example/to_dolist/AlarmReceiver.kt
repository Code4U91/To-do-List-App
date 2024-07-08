package com.example.to_dolist

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.to_dolist.db.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver(){

    private var notificationManager :NotificationManager? = null
    private var taskId : Long? = -1L


    override fun onReceive(context: Context?, intent: Intent?) {


        taskId = intent?.getLongExtra("taskId", -1L)

        Log.d("AlarmReceiver", "Alarm received for task ID: $taskId")

        //Initializing the database

        if (taskId != -1L)
        {
            var task : Task
             notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            CoroutineScope(Dispatchers.IO).launch {

                task = MainApplication.taskDatabase.getTaskDao().getTaskByIdDirect(taskId!!)

                // Switch to main thread to update LiveData
                android.os.Handler(Looper.getMainLooper()).post{
                    SharedTaskViewModel.getAlarmTime(task.alarmTimeInMills)
                    showNotification(context, task.title, task.alarmTimeInMills)
                }

                // Update the alarmTime in the database after the alarm ranges
                val repository = TaskRepository(MainApplication.taskDatabase.getTaskDao())

                repository.updateAlarmTime(taskId!!, -1L)


            }
        }
        else{

            Log.e("AlarmReceiver", "taskId ==  $taskId")
        }
    }

    

    private fun showNotification(context: Context, title: String, alarmTimeInMills: Long) {



        val notificationId = taskId?.toInt() // Request Id

        // for handling click on the notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("taskId", taskId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId!!,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification =  NotificationCompat.Builder(context,"task_channel")
            .setSmallIcon(R.drawable.baseline_alarm_24)
            .setContentTitle(title)
            .setContentText("Reminder of $title at ${SharedTaskViewModel.formatAlarmTime(alarmTimeInMills)}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()



        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Log or handle the case where the permission is not granted
                Log.e("AlarmReceiver", "POST_NOTIFICATIONS permission not granted")
                return
            }
            notify(notificationId, notification)
        }
    }



    }
