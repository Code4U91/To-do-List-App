package com.example.to_dolist.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Task::class], version = 2, exportSchema = false)
abstract class TaskDatabase  : RoomDatabase(){

    abstract  fun getTaskDao() : TaskDao


}