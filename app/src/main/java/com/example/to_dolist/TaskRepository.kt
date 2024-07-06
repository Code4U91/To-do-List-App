package com.example.to_dolist

import androidx.lifecycle.LiveData
import com.example.to_dolist.db.Task
import com.example.to_dolist.db.TaskDao

class TaskRepository( private val taskDao: TaskDao) {

    val allTasks: LiveData<List<Task>> = taskDao.getAllTask() // Room handles it on the background thread

    suspend fun insertTask(task: Task) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    fun getTasksSortedByDateCreated() : LiveData<List<Task>>
    {
        return taskDao.getTasksSortedByDateCreated()
    }

   fun getTasksSortedByDateModified() : LiveData<List<Task>>
    {
        return taskDao.getTasksSortedByDateModified()
    }

   suspend fun getAllTaskDirect() : List<Task>
    {
        return  taskDao.getAllTaskDirect()

    }

    suspend fun getLastInsertedTaskId(): Long {
        return taskDao.getLastInsertedTaskId()
    }

    fun getTaskById(taskId: Long): LiveData<Task> {
        return taskDao.getTaskById(taskId)
    }

    suspend fun updateAlarmTime(noteId: Long, alarmTime: Long) {
        taskDao.updateAlarmTime(noteId, alarmTime)
    }
    suspend fun getLastInsertedTask(): Task? {

        return taskDao.getLastInsertedTask()
    }

    suspend fun getTaskByIdDirect(taskId : Long) : Task
    {
        return taskDao.getTaskByIdDirect(taskId)
    }

    suspend fun getAlarmTimeByTaskId(taskID : Long) : Long
    {
        return taskDao.getAlarmTimeByTaskId(taskID)
    }


}