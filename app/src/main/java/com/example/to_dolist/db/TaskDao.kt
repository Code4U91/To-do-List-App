package com.example.to_dolist.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask( task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM task_data_table ORDER BY task_id DESC")
     fun getAllTask() : LiveData<List<Task>>

    @Query("SELECT * FROM task_data_table ORDER BY sort_timestamp DESC")
     fun getTasksSortedByDateModified(): LiveData<List<Task>>

    @Query("SELECT * FROM task_data_table ORDER BY sort_timestamp DESC")
     fun getTasksSortedByDateCreated(): LiveData<List<Task>>

    @Query("SELECT task_id FROM task_data_table ORDER BY task_id DESC LIMIT 1")
    suspend fun getLastInsertedTaskId(): Long

    @Query("SELECT * FROM task_data_table WHERE task_id = :taskId LIMIT 1")
    fun getTaskById(taskId: Long): LiveData<Task>

    @Query("UPDATE task_data_table SET task_alarm_time = :alarmTime WHERE task_id = :taskId")
    suspend fun updateAlarmTime(taskId: Long, alarmTime: Long)

    @Query("SELECT * FROM task_data_table ORDER BY task_id DESC LIMIT 1")
    suspend fun getLastInsertedTask(): Task?

    @Query("SELECT task_title FROM task_data_table WHERE task_id = :taskId LIMIT 1")
    fun getTitleById(taskId : Long) : String

    @Query("SELECT * FROM task_data_table WHERE task_id = :taskId LIMIT 1")
    suspend fun getTaskByIdDirect(taskId : Long) : Task

    @Query("SELECT * FROM task_data_table ORDER BY task_id DESC")
    suspend fun getAllTaskDirect(): List<Task>

    @Query("SELECT task_alarm_time FROM task_data_table WHERE task_id = :taskID")
    suspend fun getAlarmTimeByTaskId(taskID : Long) : Long

}