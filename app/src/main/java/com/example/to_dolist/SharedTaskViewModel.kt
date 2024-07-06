package com.example.to_dolist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.to_dolist.db.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SharedTaskViewModel(mainApplication: MainApplication) : ViewModel() {

    val searchText = MutableLiveData<String>()

    init {
        alarmTimeInMills.value = -1L
        searchText.value = ""
    }

    companion object{
        var isSortByTimeOfCreation : Boolean = false  // flag

        var isListItemClickedLiveData = MutableLiveData<Boolean>()
        var selectedTaskLiveData = MutableLiveData<Task?>()
        var alarmTimeInMills = MutableLiveData<Long>()

        init {
            isListItemClickedLiveData.value = false
            selectedTaskLiveData.value = null
            alarmTimeInMills.value = -1L

        }

        fun selectedTaskLiveFun(task : Task?)
        {
            selectedTaskLiveData.value = task
        }

        fun getAlarmTime(alarmTime : Long)
        {
            alarmTimeInMills.value = alarmTime
        }

        // Converts Mills to date
        fun formatAlarmTime(alarmTimeInMills: Long): String {
            // Check if alarmTimeInMills is set
            if (alarmTimeInMills != -1L) {
                // Create a Date object from the milliseconds
                val date = Date(alarmTimeInMills)

                // Define the desired date-time format
                val format = SimpleDateFormat("MMM dd yyyy, hh:mm a", Locale.getDefault())

                // Format the date to a string
                return format.format(date)
            } else {
                // If alarmTimeInMills is not set, return an empty string or a default message
                return "No alarm set"
            }
        }


    }


    private val taskRepository : TaskRepository
    val allTasks : LiveData<List<Task>>

    init {
        val taskDao = MainApplication.taskDatabase.getTaskDao()
        taskRepository = TaskRepository(taskDao)
        allTasks = taskRepository.allTasks
    }

    fun insertTask(task: Task) = viewModelScope.launch {
        taskRepository.insertTask(task)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        taskRepository.updateTask(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        taskRepository.deleteTask(task)
    }

    suspend fun getAllTaskDirect() : List<Task>
    {
        return taskRepository.getAllTaskDirect()

    }

    fun getTasksSortedByDateCreated(): LiveData<List<Task>> {
        return taskRepository.getTasksSortedByDateCreated()
    }

    fun getTasksSortedByDateModified(): LiveData<List<Task>> {
        return taskRepository.getTasksSortedByDateModified()
    }


   suspend fun getLastInsertedTaskId(): Long {
        return taskRepository.getLastInsertedTaskId()
    }

    fun getTaskById(taskId: Long): LiveData<Task> {
        return taskRepository.getTaskById(taskId)
    }

    fun updateAlarmTime(noteId: Long, alarmTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.updateAlarmTime(noteId, alarmTime)
        }
    }

    suspend fun getLastInsertedTask(): Task? {

        return taskRepository.getLastInsertedTask()
    }

    suspend fun getTaskByIdDirect(taskId :Long) : Task
    {
        return taskRepository.getTaskByIdDirect(taskId)
    }

    suspend fun getAlarmTimeByTaskId(taskID : Long) : Long
    {
        return taskRepository.getAlarmTimeByTaskId(taskID)
    }

}